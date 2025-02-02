package com.event.rest.emailnotification.handler;

import com.event.core.ProductCreatedEvent;
import com.event.core.exception.NonRetryableException;
import com.event.core.exception.RetryableException;
import com.event.rest.emailnotification.model.entity.ProcessedEventEntity;
import com.event.rest.emailnotification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * The type Product created event handler.
 */
@Component
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler
{
    private final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    /**
     * The Rest template.
     */
    RestTemplate  restTemplate;

    /**
     * The Processed event repository.
     */
    ProcessedEventRepository processedEventRepository;

    /**
     * Instantiates a new Product created event handler.
     *
     * @param restTemplate             the rest template
     * @param processedEventRepository the processed event repository
     */
    public ProductCreatedEventHandler(RestTemplate restTemplate, ProcessedEventRepository processedEventRepository)
    {
        this.restTemplate= restTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Handle.
     *
     * @param productCreatedEvent the product created event
     * @param messageid           the messageid
     * @param messageKey          the message key
     */
    @Transactional
    @KafkaHandler
    public void handle(@Payload ProductCreatedEvent productCreatedEvent, @Header("message_id") String messageid, @Header(KafkaHeaders.RECEIVED_KEY) String messageKey)
    {

        LOGGER.info("Received an event :" + productCreatedEvent.getTitle());

        //Lookup ind DB if message exists
        ProcessedEventEntity messagaEntity = processedEventRepository.findByMessageId(messageid);
        if(messagaEntity != null)
        {
            LOGGER.info("Duplicate Message : {} ",   messagaEntity.getMessageId());
            return;
        }

        String requestUrl = "http://localhost:8082/mock/success";
        try
        {
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().is2xxSuccessful())
            {
                LOGGER.info("Response from other service" + response.getBody());
            }
        }
        catch(ResourceAccessException e)
        {
            LOGGER.error(e.getMessage(), e);
            throw new RetryableException(e);
        }
        catch(HttpServerErrorException e)
        {
            LOGGER.error(e.getMessage(), e);
            throw new NonRetryableException(e);
        }
        catch(Exception e)
        {
            LOGGER.error(e.getMessage(), e);
            throw new NonRetryableException(e);
        }

        //Persist in DB th message id

        try
        {
              processedEventRepository.save(new ProcessedEventEntity(messageid, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException e)
        {
                LOGGER.error("Data exists in DB");
                throw new NonRetryableException(e);
        }
    }
}

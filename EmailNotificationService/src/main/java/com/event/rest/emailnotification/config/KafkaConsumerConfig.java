package com.event.rest.emailnotification.config;


import com.event.core.exception.NonRetryableException;
import com.event.core.exception.RetryableException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Kafka consumer config.
 */
@Configuration
public class KafkaConsumerConfig
{
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootStrapServer;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trusted;

    /**
     * Consumer factory consumer factory.
     *
     * @return the consumer factory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory()
    {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, trusted);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);


        return new DefaultKafkaConsumerFactory<>(config);

    }

    /**
     * Producer factory producer factory.
     *
     * @return the producer factory
     */
    @Bean
    ProducerFactory<String, Object> producerFactory()
    {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka template kafka template.
     *
     * @param producerFactory the producer factory
     * @return the kafka template
     */
    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory)
    {
        return new KafkaTemplate<String, Object>(producerFactory);
    }

    /**
     * Kafka listener container factory concurrent kafka listener container factory.
     *
     * @param consumerFactory the consumer factory
     * @param kafkaTemplate   the kafka template
     * @return the concurrent kafka listener container factory
     */
    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate)
    {
        // Retrying to consume message - before publishing the error
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(5000,3));
        defaultErrorHandler.addNotRetryableExceptions(NonRetryableException.class);
        defaultErrorHandler.addRetryableExceptions(RetryableException.class);
        ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        concurrentKafkaListenerContainerFactory.setCommonErrorHandler(defaultErrorHandler);
        return concurrentKafkaListenerContainerFactory;
    }

    /**
     * Rest template rest template.
     *
     * @return the rest template
     */
    @Bean
    RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}

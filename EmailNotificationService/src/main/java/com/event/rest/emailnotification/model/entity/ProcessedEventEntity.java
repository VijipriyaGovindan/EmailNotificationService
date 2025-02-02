package com.event.rest.emailnotification.model.entity;


import java.io.Serializable;

import jakarta.persistence.*;

/**
 * The type Processed event entity.
 */
@Entity
@Table(name="processedevents")
public class ProcessedEventEntity implements Serializable
{
    /**
     * Instantiates a new Processed event entity.
     */
    public ProcessedEventEntity()
    {
    }

    /**
     * Instantiates a new Processed event entity.
     *
     * @param messageId the message id
     * @param productId the product id
     */
    public ProcessedEventEntity(String messageId, String productId)
    {

        this.messageId = messageId;
        this.productId = productId;
    }

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String productId;

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * Gets message id.
     *
     * @return the message id
     */
    public String getMessageId()
    {
        return messageId;
    }

    /**
     * Sets message id.
     *
     * @param messageId the message id
     */
    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    /**
     * Gets product id.
     *
     * @return the product id
     */
    public String getProductId()
    {
        return productId;
    }

    /**
     * Sets product id.
     *
     * @param productId the product id
     */
    public void setProductId(String productId)
    {
        this.productId = productId;
    }
}

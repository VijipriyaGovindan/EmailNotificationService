package com.event.rest.emailnotification.repository;

import com.event.rest.emailnotification.model.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Processed event repository.
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long>
{
 /**
  * Find by message id processed event entity.
  *
  * @param messageId the message id
  * @return the processed event entity
  */
 ProcessedEventEntity findByMessageId(String messageId);
}

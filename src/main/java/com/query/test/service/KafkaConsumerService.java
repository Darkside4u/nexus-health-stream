package com.query.test.service;
import com.query.test.dto.PatientEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service for Patient Events
 *
 * This service listens to Kafka topics and processes incoming patient events.
 * Each method is annotated with @KafkaListener to consume messages from specific topics.
 *
 * Key Features:
 * - Multiple consumer methods for different event types
 * - Manual acknowledgment for reliable message processing
 * - Error handling and logging
 * - Access to message metadata (partition, offset, timestamp)
 *
 * Consumer Group: patient-service-group
 * - All consumers with the same group ID share the workload
 * - Each message is processed by only one consumer in the group
 *
 * Acknowledgment Mode: MANUAL
 * - Messages are acknowledged only after successful processing
 * - Prevents message loss in case of processing failures
 */
@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Consumes patient created events
     *
     * This method listens to the patient.created topic and processes
     * new patient registration events.
     *
     * @param event The patient event data
     * @param partition The partition from which the message was consumed
     * @param offset The offset of the message
     * @param timestamp The timestamp when the message was produced
     * @param acknowledgment Manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${kafka.topic.patient-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePatientCreatedEvent(
            @Payload PatientEventDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        try {
            log.info("=========================================");
            log.info("Consumed PATIENT CREATED event:");
            log.info("Patient ID: {}", event.getPatientId());
            log.info("Name: {}", event.getName());
            log.info("Email: {}", event.getEmail());
            log.info("Blood Group: {}", event.getBloodGroup());
            log.info("Triggered By: {}", event.getTriggeredBy());
            log.info("Event Timestamp: {}", event.getTimestamp());
            log.info("Kafka Metadata - Partition: {}, Offset: {}, Timestamp: {}",
                    partition, offset, timestamp);
            log.info("=========================================");

            // Process the event here
            // Examples:
            // - Send welcome email to patient
            // - Update analytics/reporting database
            // - Notify other microservices
            // - Create audit log entry
            processPatientCreatedEvent(event);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed and acknowledged patient created event for ID: {}",
                    event.getPatientId());

        } catch (Exception e) {
            log.error("Error processing patient created event: {}", e.getMessage(), e);
            // Don't acknowledge - message will be reprocessed
            // Consider implementing a dead letter queue for failed messages
        }
    }

    /**
     * Consumes patient updated events
     *
     * @param event The patient event data
     * @param partition The partition from which the message was consumed
     * @param offset The offset of the message
     * @param acknowledgment Manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${kafka.topic.patient-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePatientUpdatedEvent(
            @Payload PatientEventDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("=========================================");
            log.info("Consumed PATIENT UPDATED event:");
            log.info("Patient ID: {}", event.getPatientId());
            log.info("Name: {}", event.getName());
            log.info("Email: {}", event.getEmail());
            log.info("Triggered By: {}", event.getTriggeredBy());
            log.info("Kafka Metadata - Partition: {}, Offset: {}", partition, offset);
            log.info("=========================================");

            // Process the event
            processPatientUpdatedEvent(event);

            acknowledgment.acknowledge();
            log.info("Successfully processed patient updated event for ID: {}",
                    event.getPatientId());

        } catch (Exception e) {
            log.error("Error processing patient updated event: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes patient deleted events
     *
     * @param event The patient event data
     * @param partition The partition from which the message was consumed
     * @param offset The offset of the message
     * @param acknowledgment Manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${kafka.topic.patient-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePatientDeletedEvent(
            @Payload PatientEventDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("=========================================");
            log.info("Consumed PATIENT DELETED event:");
            log.info("Patient ID: {}", event.getPatientId());
            log.info("Name: {}", event.getName());
            log.info("Triggered By: {}", event.getTriggeredBy());
            log.info("Kafka Metadata - Partition: {}, Offset: {}", partition, offset);
            log.info("=========================================");

            // Process the event
            processPatientDeletedEvent(event);

            acknowledgment.acknowledge();
            log.info("Successfully processed patient deleted event for ID: {}",
                    event.getPatientId());

        } catch (Exception e) {
            log.error("Error processing patient deleted event: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes all patient events from the general events topic
     *
     * This consumer receives all types of patient events (created, updated, deleted)
     * and can be used for consolidated processing like analytics or auditing.
     *
     * @param event The patient event data
     * @param partition The partition from which the message was consumed
     * @param offset The offset of the message
     * @param acknowledgment Manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${kafka.topic.patient-events}",
            groupId = "${spring.kafka.consumer.group-id}-all-events"
    )
    public void consumeAllPatientEvents(
            @Payload PatientEventDto event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("=========================================");
            log.info("Consumed PATIENT EVENT from general topic:");
            log.info("Event Type: {}", event.getEventType());
            log.info("Patient ID: {}", event.getPatientId());
            log.info("Name: {}", event.getName());
            log.info("Triggered By: {}", event.getTriggeredBy());
            log.info("Kafka Metadata - Partition: {}, Offset: {}", partition, offset);
            log.info("=========================================");

            // Process all events for analytics/audit purposes
            processAllEvents(event);

            acknowledgment.acknowledge();
            log.info("Successfully processed general patient event: {} for ID: {}",
                    event.getEventType(), event.getPatientId());

        } catch (Exception e) {
            log.error("Error processing general patient event: {}", e.getMessage(), e);
        }
    }

    // ==================== Business Logic Methods ====================

    /**
     * Process patient created event
     * Add your business logic here
     */
    private void processPatientCreatedEvent(PatientEventDto event) {
        // Example: Send welcome email
        log.info("Business Logic: Sending welcome email to {}", event.getEmail());

        // Example: Update reporting database
        log.info("Business Logic: Updating analytics for new patient");

        // Example: Notify external systems
        log.info("Business Logic: Notifying external systems about new patient");
    }

    /**
     * Process patient updated event
     * Add your business logic here
     */
    private void processPatientUpdatedEvent(PatientEventDto event) {
        // Example: Send update notification
        log.info("Business Logic: Processing patient update for ID: {}", event.getPatientId());

        // Example: Sync with external systems
        log.info("Business Logic: Syncing updated patient data with external systems");
    }

    /**
     * Process patient deleted event
     * Add your business logic here
     */
    private void processPatientDeletedEvent(PatientEventDto event) {
        // Example: Archive patient data
        log.info("Business Logic: Archiving patient data for ID: {}", event.getPatientId());

        // Example: Clean up related resources
        log.info("Business Logic: Cleaning up resources for deleted patient");
    }

    /**
     * Process all events for consolidated operations
     * Add your business logic here
     */
    private void processAllEvents(PatientEventDto event) {
        // Example: Update audit log
        log.info("Business Logic: Updating audit log with event type: {}", event.getEventType());

        // Example: Update analytics dashboard
        log.info("Business Logic: Updating analytics for event: {}", event.getEventType());
    }
}

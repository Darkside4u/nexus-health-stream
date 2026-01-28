package com.query.test.service;

import com.query.test.dto.PatientEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service for Patient Events
 *
 * This service is responsible for publishing patient-related events to Kafka topics.
 * It uses KafkaTemplate to send messages asynchronously and provides callback handling
 * for success and failure scenarios.
 *
 * Key Features:
 * - Asynchronous message publishing
 * - Success and failure callback handling
 * - Logging for monitoring and debugging
 * - Multiple topic support for different event types
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, PatientEventDto> kafkaTemplate;

    @Value("${kafka.topic.patient-created}")
    private String patientCreatedTopic;

    @Value("${kafka.topic.patient-updated}")
    private String patientUpdatedTopic;

    @Value("${kafka.topic.patient-deleted}")
    private String patientDeletedTopic;

    @Value("${kafka.topic.patient-events}")
    private String patientEventsTopic;

    /**
     * Send patient created event to Kafka
     *
     * @param event The patient event DTO containing patient information
     */
    public void sendPatientCreatedEvent(PatientEventDto event) {
        log.info("Sending patient created event to topic: {} for patientId: {}",
                patientCreatedTopic, event.getPatientId());

        sendEvent(patientCreatedTopic, event.getPatientId().toString(), event);
        // Also send to general events topic
        sendEvent(patientEventsTopic, event.getPatientId().toString(), event);
    }

    /**
     * Send patient updated event to Kafka
     *
     * @param event The patient event DTO containing updated patient information
     */
    public void sendPatientUpdatedEvent(PatientEventDto event) {
        log.info("Sending patient updated event to topic: {} for patientId: {}",
                patientUpdatedTopic, event.getPatientId());

        sendEvent(patientUpdatedTopic, event.getPatientId().toString(), event);
        // Also send to general events topic
        sendEvent(patientEventsTopic, event.getPatientId().toString(), event);
    }

    /**
     * Send patient deleted event to Kafka
     *
     * @param event The patient event DTO containing deleted patient information
     */
    public void sendPatientDeletedEvent(PatientEventDto event) {
        log.info("Sending patient deleted event to topic: {} for patientId: {}",
                patientDeletedTopic, event.getPatientId());

        sendEvent(patientDeletedTopic, event.getPatientId().toString(), event);
        // Also send to general events topic
        sendEvent(patientEventsTopic, event.getPatientId().toString(), event);
    }

    /**
     * Generic method to send an event to a specific topic
     *
     * The key is used for partitioning - messages with the same key
     * will always go to the same partition, ensuring ordering for that key.
     *
     * @param topic The Kafka topic to send the message to
     * @param key The message key (used for partitioning)
     * @param event The patient event DTO
     */
    private void sendEvent(String topic, String key, PatientEventDto event) {
        try {
            // Send message asynchronously
            CompletableFuture<SendResult<String, PatientEventDto>> future =
                    kafkaTemplate.send(topic, key, event);

            // Add callback for success/failure handling
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent message to topic: {} with key: {} | Partition: {} | Offset: {}",
                            topic,
                            key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to topic: {} with key: {} | Error: {}",
                            topic, key, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error while sending message to Kafka topic: {} | Error: {}",
                    topic, e.getMessage(), e);
        }
    }

    /**
     * Send a custom event to any topic
     * Useful for testing or custom scenarios
     *
     * @param topic The Kafka topic
     * @param key The message key
     * @param event The patient event DTO
     */
    public void sendCustomEvent(String topic, String key, PatientEventDto event) {
        log.info("Sending custom event to topic: {} with key: {}", topic, key);
        sendEvent(topic, key, event);
    }
}

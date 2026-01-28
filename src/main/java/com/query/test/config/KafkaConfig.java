package com.query.test.config;

import com.query.test.dto.PatientEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration Class
 *
 * This class configures:
 * 1. Kafka topics for the Patient Service
 * 2. KafkaTemplate for producing messages
 * 3. Consumer factory and listener container
 *
 * Topics are automatically created if they don't exist in the Kafka cluster.
 *
 * Each topic is configured with:
 * - Partitions: Number of partitions for parallel processing
 * - Replicas: Number of replicas for fault tolerance
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.patient-created}")
    private String patientCreatedTopic;

    @Value("${kafka.topic.patient-updated}")
    private String patientUpdatedTopic;

    @Value("${kafka.topic.patient-deleted}")
    private String patientDeletedTopic;

    @Value("${kafka.topic.patient-events}")
    private String patientEventsTopic;

    // ==================== PRODUCER CONFIGURATION ====================

    /**
     * Producer Factory configuration
     * Creates producers for sending messages to Kafka
     */
    @Bean
    public ProducerFactory<String, PatientEventDto> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate bean for sending messages
     * This is what gets injected into KafkaProducerService
     */
    @Bean
    public KafkaTemplate<String, PatientEventDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==================== CONSUMER CONFIGURATION ====================

    /**
     * Consumer Factory configuration
     * Creates consumers for receiving messages from Kafka
     */
    @Bean
    public ConsumerFactory<String, PatientEventDto> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "patient-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PatientEventDto.class.getName());

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka Listener Container Factory
     * Configures how Kafka listeners process messages
     * - Manual acknowledgment mode for reliable processing
     * - Batch processing disabled for immediate processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PatientEventDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PatientEventDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    // ==================== TOPIC CONFIGURATION ====================

    /**
     * Topic for patient creation events
     * Partitions: 3 (allows parallel processing by multiple consumers)
     * Replicas: 1 (increase for production environments)
     */
    @Bean
    public NewTopic patientCreatedTopic() {
        return TopicBuilder.name(patientCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic for patient update events
     */
    @Bean
    public NewTopic patientUpdatedTopic() {
        return TopicBuilder.name(patientUpdatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic for patient deletion events
     */
    @Bean
    public NewTopic patientDeletedTopic() {
        return TopicBuilder.name(patientDeletedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * General topic for all patient events
     * This is a consolidated topic that receives all types of patient events
     */
    @Bean
    public NewTopic patientEventsTopic() {
        return TopicBuilder.name(patientEventsTopic)
                .partitions(5)
                .replicas(1)
                .build();
    }
}

package com.query.test.service;

import com.query.test.dto.PatientEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaProducerServiceTest {
    @Mock
    private KafkaTemplate<String, PatientEventDto> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kafkaProducerService = new KafkaProducerService(
                kafkaTemplate,
                "patient-events",
                "patient-events",
                "patient-events",
                "patient-events"
        );
    }

    @Test
    void sendPatientCreatedEvent_shouldSendToKafka() {
        PatientEventDto event = PatientEventDto.builder().patientId(1L).eventType("CREATED").build();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
        kafkaProducerService.sendPatientCreatedEvent(event);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), eq(event));
    }

    @Test
    void sendPatientUpdatedEvent_shouldSendToKafka() {
        PatientEventDto event = PatientEventDto.builder().patientId(1L).eventType("UPDATED").build();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
        kafkaProducerService.sendPatientUpdatedEvent(event);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), eq(event));
    }

    @Test
    void sendPatientDeletedEvent_shouldSendToKafka() {
        PatientEventDto event = PatientEventDto.builder().patientId(1L).eventType("DELETED").build();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
        kafkaProducerService.sendPatientDeletedEvent(event);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), eq(event));
    }
}


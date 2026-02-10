package com.query.test.service;

import com.query.test.dto.PatientRequestDto;
import com.query.test.dto.PatientResponseDto;
import com.query.test.entity.Patient;
import com.query.test.entity.PatientDiagnosis;
import com.query.test.exception.PatientNotFoundException;
import com.query.test.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientServiceTest {
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private KafkaProducerService kafkaProducerService;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientService = new PatientService(patientRepository, kafkaProducerService);
    }

    @Test
    void createPatient_shouldSaveAndReturnResponseDto() {
        PatientRequestDto dto = new PatientRequestDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setBloodGroup(null);
        dto.setPatientDiagnosis("Hypertension");
        dto.setDiagnosisDate(LocalDate.now());

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setActive(true);

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));

        PatientResponseDto response = patientService.createPatient(dto);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        verify(kafkaProducerService, times(1)).sendPatientCreatedEvent(any());
    }

    @Test
    void getPatientById_shouldReturnResponseDto() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        PatientResponseDto response = patientService.getPatientById(1L);
        assertEquals("John Doe", response.getName());
    }

    @Test
    void getPatientById_shouldThrowIfNotFound() {
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> patientService.getPatientById(2L));
    }

    @Test
    void getAllPatients_shouldReturnList() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setName("John Doe");
        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("Jane Doe");
        when(patientRepository.findAll()).thenReturn(List.of(patient1, patient2));
        List<PatientResponseDto> responses = patientService.getAllPatients();
        assertEquals(2, responses.size());
        assertEquals("John Doe", responses.get(0).getName());
        assertEquals("Jane Doe", responses.get(1).getName());
    }

    @Test
    void updatePatient_shouldUpdateAndReturnResponseDto() {
        PatientRequestDto dto = new PatientRequestDto();
        dto.setName("Updated Name");
        dto.setEmail("updated@example.com");
        dto.setBloodGroup(null);
        dto.setPatientDiagnosis("Updated Diagnosis");
        dto.setDiagnosisDate(LocalDate.now());

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("Old Name");
        patient.setEmail("old@example.com");
        patient.setBloodGroup(null);
        patient.setActive(true);
        patient.setDiagnoses(List.of(new PatientDiagnosis()));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponseDto response = patientService.updatePatient(1L, dto);
        assertEquals("Updated Name", response.getName());
        assertEquals("updated@example.com", response.getEmail());
        verify(kafkaProducerService, times(1)).sendPatientUpdatedEvent(any());
    }

    @Test
    void deletePatient_shouldDeleteAndPublishEvent() {
        Patient patient = new Patient();
        patient.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).deleteById(1L);
        patientService.deletePatient(1L);
        verify(kafkaProducerService, times(1)).sendPatientDeletedEvent(any());
        verify(patientRepository, times(1)).deleteById(1L);
    }
}

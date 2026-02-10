package com.query.test.controller;

import com.query.test.dto.PatientRequestDto;
import com.query.test.dto.PatientResponseDto;
import com.query.test.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientControllerTest {
    @Mock
    private PatientService patientService;
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientController = new PatientController(patientService);
    }

    @Test
    void create_shouldReturnCreatedPatient() {
        PatientRequestDto dto = new PatientRequestDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setBloodGroup(null);
        dto.setPatientDiagnosis("Hypertension");
        dto.setDiagnosisDate(LocalDate.now());

        PatientResponseDto responseDto = new PatientResponseDto();
        responseDto.setId(1L);
        responseDto.setName("John Doe");
        responseDto.setEmail("john@example.com");

        when(patientService.createPatient(any())).thenReturn(responseDto);
        ResponseEntity<PatientResponseDto> response = patientController.create(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getById_shouldReturnPatient() {
        PatientResponseDto responseDto = new PatientResponseDto();
        responseDto.setId(1L);
        responseDto.setName("John Doe");
        when(patientService.getPatientById(1L)).thenReturn(responseDto);
        ResponseEntity<PatientResponseDto> response = patientController.getById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getByIdPost_shouldReturnPatient() {
        PatientResponseDto responseDto = new PatientResponseDto();
        responseDto.setId(1L);
        responseDto.setName("John Doe");
        when(patientService.getPatientById(1L)).thenReturn(responseDto);
        ResponseEntity<PatientResponseDto> response = patientController.getByIdPost(Map.of("id", 1L));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getAll_shouldReturnList() {
        PatientResponseDto dto1 = new PatientResponseDto();
        dto1.setId(1L);
        dto1.setName("John Doe");
        PatientResponseDto dto2 = new PatientResponseDto();
        dto2.setId(2L);
        dto2.setName("Jane Doe");
        when(patientService.getAllPatients()).thenReturn(List.of(dto1, dto2));
        ResponseEntity<List<PatientResponseDto>> response = patientController.getAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void update_shouldReturnUpdatedPatient() {
        PatientRequestDto dto = new PatientRequestDto();
        dto.setName("Updated Name");
        dto.setEmail("updated@example.com");
        dto.setBloodGroup(null);
        dto.setPatientDiagnosis("Updated Diagnosis");
        dto.setDiagnosisDate(LocalDate.now());

        PatientResponseDto responseDto = new PatientResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Name");
        responseDto.setEmail("updated@example.com");

        when(patientService.updatePatient(eq(1L), any())).thenReturn(responseDto);
        ResponseEntity<PatientResponseDto> response = patientController.update(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void delete_shouldCallService() {
        doNothing().when(patientService).deletePatient(1L);
        patientController.delete(1L);
        verify(patientService, times(1)).deletePatient(1L);
    }
}

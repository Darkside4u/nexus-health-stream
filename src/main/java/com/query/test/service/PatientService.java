package com.query.test.service;

import com.query.test.dto.PatientEventDto;
import com.query.test.dto.PatientRequestDto;
import com.query.test.dto.PatientResponseDto;
import com.query.test.entity.Patient;
import com.query.test.entity.PatientDiagnosis;
import com.query.test.exception.PatientNotFoundException;
import com.query.test.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final KafkaProducerService kafkaProducerService;

    // ---------------- CREATE ----------------

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto dto) {
        log.info("Creating patient with email={}", dto.getEmail());

        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setActive(true);
        Patient savedPatient = patientRepository.save(patient);

        PatientDiagnosis diagnosis = new PatientDiagnosis();
        diagnosis.setDiagnosisDetails(dto.getPatientDiagnosis());
        diagnosis.setDiagnosisDate(dto.getDiagnosisDate());
        diagnosis.setPatient(savedPatient);

        savedPatient.addDiagnosis(diagnosis);

        Patient saved = patientRepository.save(savedPatient);

        // Publish Kafka event for patient creation
        publishPatientCreatedEvent(saved, diagnosis);

        return toResponseDto(saved, diagnosis);
    }

    // ---------------- READ ----------------

    public PatientResponseDto getPatientById(Long id) {
        log.info("Fetching patient id={}", id);

        Patient patient = patientRepository.findByIdWithDiagnoses(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return toResponseDto(patient);
    }

    /**
     * Get all patients (optimized with JOIN FETCH to avoid N+1 queries)
     * Note: For very large datasets, consider using the paginated version
     */
    public List<PatientResponseDto> getAllPatients() {
        log.info("Fetching all patients with diagnoses");

        return patientRepository.findAllWithDiagnoses()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get all patients with pagination support
     * This is the recommended method for production use with large datasets
     */
    public Page<PatientResponseDto> getAllPatientsPaginated(Pageable pageable) {
        log.info("Fetching patients with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        return patientRepository.findAll(pageable)
                .map(this::toResponseDto);
    }

    // ---------------- UPDATE ----------------

    @Transactional
    public PatientResponseDto updatePatient(Long id, PatientRequestDto dto) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setBloodGroup(dto.getBloodGroup());

        PatientDiagnosis diagnosis;

        if (patient.getDiagnoses().isEmpty()) {
            // create ONLY if none exists
            diagnosis = new PatientDiagnosis();
            diagnosis.setPatient(patient);
            patient.getDiagnoses().add(diagnosis);
        } else {
            // UPDATE existing diagnosis
            diagnosis = patient.getDiagnoses().get(0);
        }

        diagnosis.setDiagnosisDetails(dto.getPatientDiagnosis());
        diagnosis.setDiagnosisDate(dto.getDiagnosisDate());

        Patient updatedPatient = patientRepository.save(patient);

        // Publish Kafka event for patient update
        publishPatientUpdatedEvent(updatedPatient, diagnosis);

        return toResponseDto(updatedPatient, diagnosis);
    }



    // ---------------- DELETE ----------------

    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient id={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        // Publish Kafka event before deletion
        publishPatientDeletedEvent(patient);

        patientRepository.deleteById(id);
    }

    // ---------------- KAFKA EVENT PUBLISHERS ----------------

    /**
     * Publishes a patient created event to Kafka
     */
    private void publishPatientCreatedEvent(Patient patient, PatientDiagnosis diagnosis) {
        try {
            PatientEventDto event = PatientEventDto.builder()
                    .eventType("CREATED")
                    .timestamp(LocalDateTime.now())
                    .patientId(patient.getId())
                    .name(patient.getName())
                    .email(patient.getEmail())
                    .bloodGroup(patient.getBloodGroup())
                    .diagnosisDetails(diagnosis != null ? diagnosis.getDiagnosisDetails() : null)
                    .diagnosisDate(diagnosis != null ? diagnosis.getDiagnosisDate() : null)
                    .triggeredBy(getCurrentUsername())
                    .active(patient.isActive())
                    .build();

            kafkaProducerService.sendPatientCreatedEvent(event);
            log.info("Published CREATED event to Kafka for patient ID: {}", patient.getId());
        } catch (Exception e) {
            log.error("Failed to publish patient created event to Kafka: {}", e.getMessage(), e);
            // Don't fail the transaction if Kafka publishing fails
        }
    }

    /**
     * Publishes a patient updated event to Kafka
     */
    private void publishPatientUpdatedEvent(Patient patient, PatientDiagnosis diagnosis) {
        try {
            PatientEventDto event = PatientEventDto.builder()
                    .eventType("UPDATED")
                    .timestamp(LocalDateTime.now())
                    .patientId(patient.getId())
                    .name(patient.getName())
                    .email(patient.getEmail())
                    .bloodGroup(patient.getBloodGroup())
                    .diagnosisDetails(diagnosis != null ? diagnosis.getDiagnosisDetails() : null)
                    .diagnosisDate(diagnosis != null ? diagnosis.getDiagnosisDate() : null)
                    .triggeredBy(getCurrentUsername())
                    .active(patient.isActive())
                    .build();

            kafkaProducerService.sendPatientUpdatedEvent(event);
            log.info("Published UPDATED event to Kafka for patient ID: {}", patient.getId());
        } catch (Exception e) {
            log.error("Failed to publish patient updated event to Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Publishes a patient deleted event to Kafka
     */
    private void publishPatientDeletedEvent(Patient patient) {
        try {
            PatientDiagnosis latestDiagnosis = patient.getDiagnoses().isEmpty()
                    ? null
                    : patient.getDiagnoses().get(0);

            PatientEventDto event = PatientEventDto.builder()
                    .eventType("DELETED")
                    .timestamp(LocalDateTime.now())
                    .patientId(patient.getId())
                    .name(patient.getName())
                    .email(patient.getEmail())
                    .bloodGroup(patient.getBloodGroup())
                    .diagnosisDetails(latestDiagnosis != null ? latestDiagnosis.getDiagnosisDetails() : null)
                    .diagnosisDate(latestDiagnosis != null ? latestDiagnosis.getDiagnosisDate() : null)
                    .triggeredBy(getCurrentUsername())
                    .active(patient.isActive())
                    .build();

            kafkaProducerService.sendPatientDeletedEvent(event);
            log.info("Published DELETED event to Kafka for patient ID: {}", patient.getId());
        } catch (Exception e) {
            log.error("Failed to publish patient deleted event to Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets the username of the currently authenticated user from JWT token
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Could not extract username from security context: {}", e.getMessage());
        }
        return "system";
    }

    // ---------------- MAPPING ----------------

    private PatientResponseDto toResponseDto(Patient patient) {
        PatientDiagnosis latestDiagnosis =
                patient.getDiagnoses().isEmpty()
                        ? null
                        : patient.getDiagnoses().get(0); // ordered DESC

        return toResponseDto(patient, latestDiagnosis);
    }

    private PatientResponseDto toResponseDto(
            Patient patient,
            PatientDiagnosis diagnosis
    ) {
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setBloodGroup(patient.getBloodGroup());

        if (diagnosis != null) {
            dto.setPatientDiagnosis(diagnosis.getDiagnosisDetails());
            dto.setDiagnosisDate(diagnosis.getDiagnosisDate());
        }

        return dto;
    }
}

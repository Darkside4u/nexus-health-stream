package com.query.test.dto;

import com.query.test.entity.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Patient Events sent to Kafka
 *
 * This DTO represents patient-related events that are published to Kafka topics.
 * It includes all relevant patient information along with event metadata.
 *
 * Event Types:
 * - CREATED: When a new patient is registered
 * - UPDATED: When patient information is modified
 * - DELETED: When a patient record is removed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientEventDto {

    /**
     * Type of event (CREATED, UPDATED, DELETED)
     */
    private String eventType;

    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;

    /**
     * Unique identifier of the patient
     */
    private Long patientId;

    /**
     * Patient's name
     */
    private String name;

    /**
     * Patient's email address
     */
    private String email;

    /**
     * Patient's blood group
     */
    private BloodGroup bloodGroup;

    /**
     * Diagnosis details
     */
    private String diagnosisDetails;

    /**
     * Date of diagnosis
     */
    private LocalDate diagnosisDate;

    /**
     * User who triggered the event (from JWT token)
     */
    private String triggeredBy;

    /**
     * Whether the patient is active
     */
    private boolean active;
}

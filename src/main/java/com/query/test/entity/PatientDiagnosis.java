package com.query.test.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_diagnoses", indexes = {
    @Index(name = "idx_patient_id", columnList = "patient_id")
})
@Getter
@Setter
public class PatientDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String diagnosisDetails;

    @Column(nullable = false)
    private LocalDate diagnosisDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}

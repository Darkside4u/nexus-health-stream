package com.query.test.entity;

import com.query.test.entity.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient")
@Getter
@Setter
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("diagnosisDate DESC")
    private List<PatientDiagnosis> diagnoses = new ArrayList<>();

    public void addDiagnosis(PatientDiagnosis diagnosis) {
        diagnoses.add(diagnosis);
        diagnosis.setPatient(this);
    }
}

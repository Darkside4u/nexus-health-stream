package com.query.test.repository;

import com.query.test.entity.PatientDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientDiagnosisRepository extends JpaRepository<PatientDiagnosis, Long> {

    Optional<PatientDiagnosis>
    findTopByPatientIdOrderByDiagnosisDateDesc(Long patientId);
}

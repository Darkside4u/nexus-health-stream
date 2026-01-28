package com.query.test.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.query.test.entity.PatientDiagnosis;
import com.query.test.entity.enums.BloodGroup;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonPropertyOrder({ "id", "name", "email", "bloodGroup", "patientDiagnosis", "diagnosisDate" })
public class PatientResponseDto {
    private Long id;
    private String name;
    private String email;
    private BloodGroup bloodGroup;
    private String patientDiagnosis;
    private LocalDate diagnosisDate;

}

package com.query.test.dto;

import com.query.test.entity.enums.BloodGroup;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PatientRequestDto {
    private String name;
    private String email;
    private BloodGroup bloodGroup;
    private String patientDiagnosis;
    private LocalDate diagnosisDate;
}

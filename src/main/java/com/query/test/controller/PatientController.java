package com.query.test.controller;

import com.query.test.dto.PatientRequestDto;
import com.query.test.dto.PatientResponseDto;
import com.query.test.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ---------------- CREATE ----------------

    @PostMapping("/new_patients")
    public ResponseEntity<PatientResponseDto> create(
            @Valid @RequestBody PatientRequestDto dto
    ) {
        PatientResponseDto response = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------- READ ----------------

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientResponseDto> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PostMapping("/patients/details")
    public ResponseEntity<PatientResponseDto> getByIdPost(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponseDto>> getAll() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // ---------------- UPDATE ----------------

    @PutMapping("/up_patients/{id}")
    public ResponseEntity<PatientResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDto dto
    ) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    // ---------------- DELETE ----------------

    @DeleteMapping("/del_patients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        patientService.deletePatient(id);
    }
}

package com.query.test.controller;

import com.query.test.dto.PatientRequestDto;
import com.query.test.dto.PatientResponseDto;
import com.query.test.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * Get all patients with pagination support
     * Recommended for production use with large datasets
     *
     * @param page Page number (0-based), default is 0
     * @param size Page size, default is 20, max is 100
     * @param sortBy Field to sort by, default is "id"
     * @param sortDir Sort direction (asc/desc), default is "asc"
     */
    @GetMapping("/patients/paginated")
    public ResponseEntity<Page<PatientResponseDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        // Limit page size to prevent excessive memory usage
        size = Math.min(size, 100);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(patientService.getAllPatientsPaginated(pageable));
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

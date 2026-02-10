package com.query.test.repository;

import com.query.test.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Fetch patient with diagnoses using JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.diagnoses WHERE p.id = :id")
    Optional<Patient> findByIdWithDiagnoses(Long id);
    
    /**
     * Fetch all patients with diagnoses using JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.diagnoses")
    List<Patient> findAllWithDiagnoses();
    
    /**
     * Fetch patients with pagination and diagnoses using JOIN FETCH to avoid N+1 query problem
     * Note: Pagination with JOIN FETCH requires a count query
     */
    @Query(value = "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.diagnoses",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Patient p")
    Page<Patient> findAllWithDiagnoses(Pageable pageable);
}

package com.query.test.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientNotFoundExceptionTest {
    @Test
    void constructor_shouldSetMessage() {
        PatientNotFoundException ex = new PatientNotFoundException(1L);
        assertEquals("Patient not found with id: 1", ex.getMessage());
    }
}

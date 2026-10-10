package com.mediconnect.dto;

import com.mediconnect.entity.AdmissionStatus;
import com.mediconnect.entity.BedStatus;
import java.time.Instant;
import java.time.LocalDate;

public final class CareResponses {
    private CareResponses() { }

    public record Patient(Long id, String name, String email) { }

    public record LabResult(
            Long id, Long patientId, String patientName, String testName, String resultValue,
            String unit, String referenceRange, LocalDate testedOn, String notes,
            String enteredBy, Instant createdAt) { }

    public record LabReportFile(
            Long id, Long patientId, String patientName, String originalFilename,
            String contentType, long fileSize, String uploadedBy, Instant uploadedAt) { }

    public record Bed(Long id, String bedCode, String ward, String bedType, BedStatus status) { }

    public record Admission(
            Long id, Long patientId, String patientName, String patientEmail,
            String requestedWard, String reason, AdmissionStatus status, Instant requestedAt,
            Instant reviewedAt, String reviewedBy, Bed bed) { }
}

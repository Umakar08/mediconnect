package com.mediconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class CareRequests {
    private CareRequests() { }

    public record LabResultEntry(
            @NotBlank @Size(max = 120) String testName,
            @NotBlank @Size(max = 120) String resultValue,
            @Size(max = 40) String unit,
            @Size(max = 120) String referenceRange,
            @jakarta.validation.constraints.NotNull @PastOrPresent LocalDate testedOn,
            @Size(max = 1000) String notes) { }

    public record Admission(
            @NotBlank @Size(max = 100) String requestedWard,
            @Size(max = 1000) String reason) { }

    public record Bed(
            @NotBlank @Size(max = 60) String bedCode,
            @NotBlank @Size(max = 100) String ward,
            @NotBlank @Size(max = 60) String bedType) { }

    public record BedAssignment(@jakarta.validation.constraints.NotNull Long bedId) { }

    public record StaffAccount(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @jakarta.validation.constraints.Email @Size(max = 255) String email,
            @NotBlank @Size(min = 12, max = 72) String password) { }
}

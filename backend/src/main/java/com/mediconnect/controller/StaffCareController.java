package com.mediconnect.controller;

import com.mediconnect.dto.CareRequests;
import com.mediconnect.dto.CareResponses;
import com.mediconnect.entity.User;
import com.mediconnect.entity.UserRole;
import com.mediconnect.service.AdmissionService;
import com.mediconnect.service.LabReportService;
import com.mediconnect.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174",
        "https://frontend-three-rho-24.vercel.app" })
public class StaffCareController {
    private final UserRepository userRepository;
    private final LabReportService labReportService;
    private final AdmissionService admissionService;

    public StaffCareController(UserRepository userRepository, LabReportService labReportService,
            AdmissionService admissionService) {
        this.userRepository = userRepository;
        this.labReportService = labReportService;
        this.admissionService = admissionService;
    }

    @GetMapping("/patients")
    public List<CareResponses.Patient> patients(@RequestParam(defaultValue = "") String search) {
        String normalized = search.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByRoleOrderByNameAsc(UserRole.PATIENT).stream()
                .filter(patient -> normalized.isEmpty()
                        || patient.getName().toLowerCase(Locale.ROOT).contains(normalized)
                        || patient.getEmail().toLowerCase(Locale.ROOT).contains(normalized))
                .limit(100)
                .map(patient -> new CareResponses.Patient(patient.getId(), patient.getName(), patient.getEmail()))
                .toList();
    }

    @GetMapping("/patients/{patientId}/lab-results")
    public List<CareResponses.LabResult> labResults(@PathVariable Long patientId) {
        return labReportService.resultsFor(patientId);
    }

    @PostMapping("/patients/{patientId}/lab-results")
    public CareResponses.LabResult addLabResult(@PathVariable Long patientId,
            @AuthenticationPrincipal User staff, @Valid @RequestBody CareRequests.LabResultEntry entry) {
        return labReportService.addResult(patientId, staff, entry);
    }

    @GetMapping("/patients/{patientId}/lab-reports")
    public List<CareResponses.LabReportFile> labReports(@PathVariable Long patientId) {
        return labReportService.filesFor(patientId);
    }

    @GetMapping("/lab-reports/{reportId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long reportId) {
        var report = labReportService.staffDownload(reportId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(report.filename(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body(report.resource());
    }

    @GetMapping("/admissions")
    public List<CareResponses.Admission> admissions() {
        return admissionService.staffAdmissions();
    }

    @GetMapping("/beds")
    public List<CareResponses.Bed> beds(@RequestParam(defaultValue = "false") boolean availableOnly) {
        return admissionService.beds(availableOnly);
    }

    @PostMapping("/beds")
    public CareResponses.Bed createBed(@Valid @RequestBody CareRequests.Bed bed) {
        return admissionService.createBed(bed);
    }

    @PostMapping("/admissions/{requestId}/assign")
    public CareResponses.Admission assignBed(@PathVariable Long requestId,
            @AuthenticationPrincipal User staff, @Valid @RequestBody CareRequests.BedAssignment assignment) {
        return admissionService.assignBed(requestId, assignment, staff);
    }

    @PostMapping("/admissions/{requestId}/reject")
    public CareResponses.Admission reject(@PathVariable Long requestId, @AuthenticationPrincipal User staff) {
        return admissionService.reject(requestId, staff);
    }

    @PostMapping("/admissions/{requestId}/discharge")
    public CareResponses.Admission discharge(@PathVariable Long requestId, @AuthenticationPrincipal User staff) {
        return admissionService.discharge(requestId, staff);
    }
}

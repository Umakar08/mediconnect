package com.mediconnect.controller;

import com.mediconnect.dto.CareRequests;
import com.mediconnect.dto.CareResponses;
import com.mediconnect.entity.User;
import com.mediconnect.service.AdmissionService;
import com.mediconnect.service.LabReportService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174",
        "https://frontend-three-rho-24.vercel.app" })
public class PatientCareController {
    private final LabReportService labReportService;
    private final AdmissionService admissionService;

    public PatientCareController(LabReportService labReportService, AdmissionService admissionService) {
        this.labReportService = labReportService;
        this.admissionService = admissionService;
    }

    @GetMapping("/lab-results")
    public List<CareResponses.LabResult> results(@AuthenticationPrincipal User patient) {
        return labReportService.patientResults(patient);
    }

    @GetMapping("/lab-reports")
    public List<CareResponses.LabReportFile> reports(@AuthenticationPrincipal User patient) {
        return labReportService.patientFiles(patient);
    }

    @PostMapping(value = "/lab-reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CareResponses.LabReportFile upload(
            @AuthenticationPrincipal User patient, @RequestPart("file") MultipartFile file) {
        return labReportService.upload(patient, file);
    }

    @GetMapping("/lab-reports/{reportId}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal User patient, @PathVariable Long reportId) {
        var report = labReportService.patientDownload(patient, reportId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(report.filename(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body(report.resource());
    }

    @GetMapping("/admissions")
    public List<CareResponses.Admission> admissions(@AuthenticationPrincipal User patient) {
        return admissionService.patientAdmissions(patient);
    }

    @PostMapping("/admissions")
    public CareResponses.Admission requestAdmission(
            @AuthenticationPrincipal User patient, @Valid @RequestBody CareRequests.Admission admission) {
        return admissionService.requestAdmission(patient, admission);
    }
}

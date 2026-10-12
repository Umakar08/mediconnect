package com.mediconnect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mediconnect.entity.LabReportFile;
import com.mediconnect.entity.User;
import com.mediconnect.entity.UserRole;
import com.mediconnect.repository.LabReportFileRepository;
import com.mediconnect.repository.LabResultRepository;
import com.mediconnect.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LabReportServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void acceptsPdfSignatureAndStoresItOutsideTheDatabase() throws Exception {
        LabReportFileRepository fileRepository = mock(LabReportFileRepository.class);
        User patient = mock(User.class);
        User staff = mock(User.class);
        when(patient.getId()).thenReturn(4L);
        when(patient.getName()).thenReturn("Taylor Patient");
        when(patient.getRole()).thenReturn(UserRole.PATIENT);
        when(staff.getName()).thenReturn("Sam Staff");
        when(staff.getRole()).thenReturn(UserRole.STAFF);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(4L)).thenReturn(Optional.of(patient));
        when(fileRepository.save(any(LabReportFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LabReportService service = new LabReportService(mock(LabResultRepository.class), fileRepository,
                userRepository, tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile("file", "blood.pdf", "application/pdf",
                "%PDF-1.7 report".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var result = service.uploadForPatient(4L, staff, file);

        assertEquals("blood.pdf", result.originalFilename());
        assertEquals("application/pdf", result.contentType());
        var captor = org.mockito.ArgumentCaptor.forClass(LabReportFile.class);
        verify(fileRepository).save(captor.capture());
        assertTrue(Files.exists(tempDirectory.resolve(captor.getValue().getStorageKey())));
        assertEquals(staff, captor.getValue().getUploadedBy());
    }

    @Test
    void rejectsFilesWithoutARecognizedFileSignature() {
        LabReportFileRepository fileRepository = mock(LabReportFileRepository.class);
        User patient = mock(User.class);
        when(patient.getId()).thenReturn(4L);
        when(patient.getRole()).thenReturn(UserRole.PATIENT);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(4L)).thenReturn(Optional.of(patient));
        LabReportService service = new LabReportService(mock(LabResultRepository.class),
                fileRepository, userRepository, tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf",
                "not a PDF".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.uploadForPatient(4L, mock(User.class), file));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
        verify(fileRepository, never()).save(any(LabReportFile.class));
    }

    @Test
    void patientsOnlySeeAndDownloadReportsUploadedByCareStaff() {
        LabReportFileRepository fileRepository = mock(LabReportFileRepository.class);
        User patient = mock(User.class);
        User patientUploader = mock(User.class);
        User staffUploader = mock(User.class);
        when(patient.getId()).thenReturn(4L);
        when(patient.getName()).thenReturn("Taylor Patient");
        when(patientUploader.getRole()).thenReturn(UserRole.PATIENT);
        when(patientUploader.getName()).thenReturn("Taylor Patient");
        when(staffUploader.getRole()).thenReturn(UserRole.STAFF);
        when(staffUploader.getName()).thenReturn("Sam Staff");
        LabReportFile patientReport = report(patient, patientUploader, "patient-report.pdf", 1L);
        LabReportFile staffReport = report(patient, staffUploader, "staff-report.pdf", 2L);
        when(fileRepository.findByPatientIdOrderByUploadedAtDesc(4L))
                .thenReturn(List.of(patientReport, staffReport));
        when(fileRepository.findByIdAndPatientId(1L, 4L)).thenReturn(Optional.of(patientReport));
        LabReportService service = new LabReportService(mock(LabResultRepository.class), fileRepository,
                mock(UserRepository.class), tempDirectory.toString());

        var reports = service.patientFiles(patient);

        assertEquals(1, reports.size());
        assertEquals("staff-report.pdf", reports.getFirst().originalFilename());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.patientDownload(patient, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private LabReportFile report(User patient, User uploadedBy, String filename, long id) {
        LabReportFile report = mock(LabReportFile.class);
        when(report.getId()).thenReturn(id);
        when(report.getPatient()).thenReturn(patient);
        when(report.getUploadedBy()).thenReturn(uploadedBy);
        when(report.getOriginalFilename()).thenReturn(filename);
        when(report.getContentType()).thenReturn("application/pdf");
        when(report.getFileSize()).thenReturn(100L);
        when(report.getUploadedAt()).thenReturn(Instant.now());
        return report;
    }
}

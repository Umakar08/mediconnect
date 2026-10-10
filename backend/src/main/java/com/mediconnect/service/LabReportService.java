package com.mediconnect.service;

import com.mediconnect.dto.CareRequests;
import com.mediconnect.dto.CareResponses;
import com.mediconnect.entity.LabReportFile;
import com.mediconnect.entity.LabResult;
import com.mediconnect.entity.User;
import com.mediconnect.repository.LabReportFileRepository;
import com.mediconnect.repository.LabResultRepository;
import com.mediconnect.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LabReportService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final LabResultRepository labResultRepository;
    private final LabReportFileRepository labReportFileRepository;
    private final UserRepository userRepository;
    private final Path storageDirectory;

    public LabReportService(LabResultRepository labResultRepository,
            LabReportFileRepository labReportFileRepository,
            UserRepository userRepository,
            @Value("${app.storage.lab-report-directory:./data/lab-reports}") String storageDirectory) {
        this.labResultRepository = labResultRepository;
        this.labReportFileRepository = labReportFileRepository;
        this.userRepository = userRepository;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<CareResponses.LabResult> patientResults(User patient) {
        return resultsFor(patient.getId());
    }

    @Transactional(readOnly = true)
    public List<CareResponses.LabResult> resultsFor(Long patientId) {
        return labResultRepository.findByPatientIdOrderByTestedOnDesc(patientId).stream()
                .map(this::toLabResult).toList();
    }

    @Transactional
    public CareResponses.LabResult addResult(Long patientId, User staff, CareRequests.LabResultEntry entry) {
        User patient = patient(patientId);
        if (entry.testedOn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test date is required");
        }
        LabResult result = labResultRepository.save(new LabResult(patient, staff,
                entry.testName().trim(), entry.resultValue().trim(), clean(entry.unit()),
                clean(entry.referenceRange()), entry.testedOn(), clean(entry.notes())));
        return toLabResult(result);
    }

    @Transactional(readOnly = true)
    public List<CareResponses.LabReportFile> patientFiles(User patient) {
        return filesFor(patient.getId());
    }

    @Transactional(readOnly = true)
    public List<CareResponses.LabReportFile> filesFor(Long patientId) {
        return labReportFileRepository.findByPatientIdOrderByUploadedAtDesc(patientId).stream()
                .map(this::toLabReportFile).toList();
    }

    @Transactional
    public CareResponses.LabReportFile upload(User patient, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a report file to upload");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Report files must be 10 MB or smaller");
        }

        byte[] contents;
        try {
            contents = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the uploaded report", exception);
        }
        FileType fileType = FileType.detect(contents);
        if (fileType == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only genuine PDF, PNG, and JPEG report files are accepted");
        }

        String storageKey = UUID.randomUUID() + fileType.extension;
        try {
            Files.createDirectories(storageDirectory);
            Files.write(storageDirectory.resolve(storageKey), contents, StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "The report could not be stored", exception);
        }

        String filename = safeFilename(file.getOriginalFilename());
        LabReportFile saved = labReportFileRepository.save(new LabReportFile(
                patient, patient, filename, storageKey, fileType.contentType, contents.length));
        return toLabReportFile(saved);
    }

    @Transactional(readOnly = true)
    public DownloadableReport patientDownload(User patient, Long reportId) {
        LabReportFile report = labReportFileRepository.findByIdAndPatientId(reportId, patient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        return downloadable(report);
    }

    @Transactional(readOnly = true)
    public DownloadableReport staffDownload(Long reportId) {
        LabReportFile report = labReportFileRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        return downloadable(report);
    }

    private DownloadableReport downloadable(LabReportFile report) {
        Path path = storageDirectory.resolve(report.getStorageKey()).normalize();
        if (!path.startsWith(storageDirectory)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid report storage path");
        }
        Resource resource = new FileSystemResource(path);
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report file is no longer available");
        }
        return new DownloadableReport(report.getOriginalFilename(), report.getContentType(), resource);
    }

    private User patient(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (patient.getRole() != com.mediconnect.entity.UserRole.PATIENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected account is not a patient");
        }
        return patient;
    }

    private CareResponses.LabResult toLabResult(LabResult result) {
        return new CareResponses.LabResult(result.getId(), result.getPatient().getId(),
                result.getPatient().getName(), result.getTestName(), result.getResultValue(),
                result.getUnit(), result.getReferenceRange(), result.getTestedOn(), result.getNotes(),
                result.getEnteredBy().getName(), result.getCreatedAt());
    }

    private CareResponses.LabReportFile toLabReportFile(LabReportFile report) {
        return new CareResponses.LabReportFile(report.getId(), report.getPatient().getId(),
                report.getPatient().getName(), report.getOriginalFilename(), report.getContentType(),
                report.getFileSize(), report.getUploadedBy().getName(), report.getUploadedAt());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeFilename(String originalFilename) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename : "lab-report";
        filename = filename.replace('\\', '_').replace('/', '_').replaceAll("[\\p{Cntrl}]", "_").trim();
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }

    public record DownloadableReport(String filename, String contentType, Resource resource) { }

    private enum FileType {
        PDF(".pdf", "application/pdf"),
        PNG(".png", "image/png"),
        JPEG(".jpg", "image/jpeg");

        private final String extension;
        private final String contentType;

        FileType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        private static FileType detect(byte[] data) {
            if (startsWith(data, new byte[] { '%', 'P', 'D', 'F', '-' })) return PDF;
            if (startsWith(data, new byte[] { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 })) return PNG;
            if (startsWith(data, new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff })) return JPEG;
            return null;
        }

        private static boolean startsWith(byte[] data, byte[] signature) {
            if (data.length < signature.length) return false;
            for (int i = 0; i < signature.length; i++) {
                if (data[i] != signature[i]) return false;
            }
            return true;
        }
    }
}

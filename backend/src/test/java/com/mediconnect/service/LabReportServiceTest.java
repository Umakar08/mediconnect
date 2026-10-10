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
import com.mediconnect.repository.LabReportFileRepository;
import com.mediconnect.repository.LabResultRepository;
import com.mediconnect.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
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
        when(patient.getId()).thenReturn(4L);
        when(patient.getName()).thenReturn("Taylor Patient");
        when(fileRepository.save(any(LabReportFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LabReportService service = new LabReportService(mock(LabResultRepository.class), fileRepository,
                mock(UserRepository.class), tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile("file", "blood.pdf", "application/pdf",
                "%PDF-1.7 report".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var result = service.upload(patient, file);

        assertEquals("blood.pdf", result.originalFilename());
        assertEquals("application/pdf", result.contentType());
        var captor = org.mockito.ArgumentCaptor.forClass(LabReportFile.class);
        verify(fileRepository).save(captor.capture());
        assertTrue(Files.exists(tempDirectory.resolve(captor.getValue().getStorageKey())));
    }

    @Test
    void rejectsFilesWithoutARecognizedFileSignature() {
        LabReportFileRepository fileRepository = mock(LabReportFileRepository.class);
        LabReportService service = new LabReportService(mock(LabResultRepository.class),
                fileRepository, mock(UserRepository.class), tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf",
                "not a PDF".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.upload(mock(User.class), file));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
        verify(fileRepository, never()).save(any(LabReportFile.class));
    }
}

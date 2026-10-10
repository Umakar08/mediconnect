package com.mediconnect.repository;

import com.mediconnect.entity.LabReportFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabReportFileRepository extends JpaRepository<LabReportFile, Long> {
    List<LabReportFile> findByPatientIdOrderByUploadedAtDesc(Long patientId);
    Optional<LabReportFile> findByIdAndPatientId(Long id, Long patientId);
}

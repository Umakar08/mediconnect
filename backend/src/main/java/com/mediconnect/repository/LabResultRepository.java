package com.mediconnect.repository;

import com.mediconnect.entity.LabResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByPatientIdOrderByTestedOnDesc(Long patientId);
}

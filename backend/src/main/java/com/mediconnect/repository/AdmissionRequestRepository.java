package com.mediconnect.repository;

import com.mediconnect.entity.AdmissionRequest;
import com.mediconnect.entity.AdmissionStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdmissionRequestRepository extends JpaRepository<AdmissionRequest, Long> {
    List<AdmissionRequest> findByPatientIdOrderByRequestedAtDesc(Long patientId);
    List<AdmissionRequest> findAllByOrderByRequestedAtDesc();
    List<AdmissionRequest> findByStatusOrderByRequestedAtAsc(AdmissionStatus status);
    boolean existsByPatientIdAndStatusIn(Long patientId, Collection<AdmissionStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select admission from AdmissionRequest admission where admission.id = :id")
    Optional<AdmissionRequest> findByIdForUpdate(@Param("id") Long id);
}

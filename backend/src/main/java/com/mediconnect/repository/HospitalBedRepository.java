package com.mediconnect.repository;

import com.mediconnect.entity.BedStatus;
import com.mediconnect.entity.HospitalBed;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalBedRepository extends JpaRepository<HospitalBed, Long> {
    boolean existsByBedCodeIgnoreCase(String bedCode);
    List<HospitalBed> findAllByOrderByWardAscBedCodeAsc();
    List<HospitalBed> findByStatusOrderByWardAscBedCodeAsc(BedStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bed from HospitalBed bed where bed.id = :id")
    Optional<HospitalBed> findByIdForUpdate(@Param("id") Long id);
}

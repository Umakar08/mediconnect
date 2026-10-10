package com.mediconnect.service;

import com.mediconnect.dto.CareRequests;
import com.mediconnect.dto.CareResponses;
import com.mediconnect.entity.AdmissionRequest;
import com.mediconnect.entity.AdmissionStatus;
import com.mediconnect.entity.BedStatus;
import com.mediconnect.entity.HospitalBed;
import com.mediconnect.entity.User;
import com.mediconnect.repository.AdmissionRequestRepository;
import com.mediconnect.repository.HospitalBedRepository;
import com.mediconnect.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdmissionService {
    private final AdmissionRequestRepository admissionRequestRepository;
    private final HospitalBedRepository hospitalBedRepository;
    private final UserRepository userRepository;

    public AdmissionService(AdmissionRequestRepository admissionRequestRepository,
            HospitalBedRepository hospitalBedRepository, UserRepository userRepository) {
        this.admissionRequestRepository = admissionRequestRepository;
        this.hospitalBedRepository = hospitalBedRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CareResponses.Admission> patientAdmissions(User patient) {
        return admissionRequestRepository.findByPatientIdOrderByRequestedAtDesc(patient.getId()).stream()
                .map(this::toAdmission).toList();
    }

    @Transactional
    public CareResponses.Admission requestAdmission(User patient, CareRequests.Admission admission) {
        User lockedPatient = userRepository.findByIdForUpdate(patient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        boolean alreadyActive = admissionRequestRepository.existsByPatientIdAndStatusIn(lockedPatient.getId(),
                List.of(AdmissionStatus.PENDING, AdmissionStatus.ADMITTED));
        if (alreadyActive) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a pending request or active admission");
        }
        AdmissionRequest saved = admissionRequestRepository.save(new AdmissionRequest(
                lockedPatient, admission.requestedWard().trim(), clean(admission.reason())));
        return toAdmission(saved);
    }

    @Transactional(readOnly = true)
    public List<CareResponses.Admission> staffAdmissions() {
        return admissionRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(this::toAdmission).toList();
    }

    @Transactional(readOnly = true)
    public List<CareResponses.Bed> beds(boolean availableOnly) {
        List<HospitalBed> beds = availableOnly
                ? hospitalBedRepository.findByStatusOrderByWardAscBedCodeAsc(BedStatus.AVAILABLE)
                : hospitalBedRepository.findAllByOrderByWardAscBedCodeAsc();
        return beds.stream().map(this::toBed).toList();
    }

    @Transactional
    public CareResponses.Bed createBed(CareRequests.Bed request) {
        String bedCode = request.bedCode().trim();
        if (hospitalBedRepository.existsByBedCodeIgnoreCase(bedCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A bed with that code already exists");
        }
        HospitalBed bed = hospitalBedRepository.save(new HospitalBed(
                bedCode, request.ward().trim(), request.bedType().trim()));
        return toBed(bed);
    }

    @Transactional
    public CareResponses.Admission assignBed(Long requestId, CareRequests.BedAssignment assignment, User staff) {
        AdmissionRequest request = admissionRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admission request not found"));
        if (request.getStatus() != AdmissionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending requests can be assigned a bed");
        }
        HospitalBed bed = hospitalBedRepository.findByIdForUpdate(assignment.bedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bed not found"));
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That bed is no longer available");
        }
        bed.setStatus(BedStatus.OCCUPIED);
        request.assign(bed, staff);
        return toAdmission(request);
    }

    @Transactional
    public CareResponses.Admission reject(Long requestId, User staff) {
        AdmissionRequest request = admissionRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admission request not found"));
        if (request.getStatus() != AdmissionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending requests can be rejected");
        }
        request.reject(staff);
        return toAdmission(request);
    }

    @Transactional
    public CareResponses.Admission discharge(Long requestId, User staff) {
        AdmissionRequest request = admissionRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admission not found"));
        if (request.getStatus() != AdmissionStatus.ADMITTED || request.getBed() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only admitted patients can be discharged");
        }
        HospitalBed bed = hospitalBedRepository.findByIdForUpdate(request.getBed().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned bed not found"));
        bed.setStatus(BedStatus.AVAILABLE);
        request.discharge(staff);
        return toAdmission(request);
    }

    private CareResponses.Admission toAdmission(AdmissionRequest request) {
        return new CareResponses.Admission(request.getId(), request.getPatient().getId(),
                request.getPatient().getName(), request.getPatient().getEmail(), request.getRequestedWard(),
                request.getReason(), request.getStatus(), request.getRequestedAt(), request.getReviewedAt(),
                request.getReviewedBy() == null ? null : request.getReviewedBy().getName(),
                request.getBed() == null ? null : toBed(request.getBed()));
    }

    private CareResponses.Bed toBed(HospitalBed bed) {
        return new CareResponses.Bed(bed.getId(), bed.getBedCode(), bed.getWard(),
                bed.getBedType(), bed.getStatus());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

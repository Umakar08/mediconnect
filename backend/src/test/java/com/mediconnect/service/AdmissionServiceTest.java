package com.mediconnect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AdmissionServiceTest {
    private AdmissionRequestRepository admissionRepository;
    private HospitalBedRepository bedRepository;
    private UserRepository userRepository;
    private AdmissionService service;

    @BeforeEach
    void setUp() {
        admissionRepository = mock(AdmissionRequestRepository.class);
        bedRepository = mock(HospitalBedRepository.class);
        userRepository = mock(UserRepository.class);
        service = new AdmissionService(admissionRepository, bedRepository, userRepository);
    }

    @Test
    void assigningAvailableBedAdmitsPatientAndOccupiesBed() {
        User patient = patient();
        AdmissionRequest request = new AdmissionRequest(patient, "Medical ward", null);
        HospitalBed bed = bed(BedStatus.AVAILABLE);
        when(admissionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(bedRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(bed));

        CareResponses.Admission result = service.assignBed(1L, new CareRequests.BedAssignment(2L), mock(User.class));

        assertEquals(AdmissionStatus.ADMITTED, result.status());
        verify(bed).setStatus(BedStatus.OCCUPIED);
    }

    @Test
    void assigningOccupiedBedDoesNotAdmitPatient() {
        AdmissionRequest request = new AdmissionRequest(patient(), "Medical ward", null);
        HospitalBed bed = bed(BedStatus.OCCUPIED);
        when(admissionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(bedRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(bed));

        assertThrows(ResponseStatusException.class,
                () -> service.assignBed(1L, new CareRequests.BedAssignment(2L), mock(User.class)));

        assertEquals(AdmissionStatus.PENDING, request.getStatus());
    }

    @Test
    void dischargingPatientReleasesAssignedBed() {
        AdmissionRequest request = new AdmissionRequest(patient(), "Medical ward", null);
        HospitalBed bed = bed(BedStatus.OCCUPIED);
        request.assign(bed, mock(User.class));
        when(admissionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(bedRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(bed));

        CareResponses.Admission result = service.discharge(1L, mock(User.class));

        assertEquals(AdmissionStatus.DISCHARGED, result.status());
        verify(bed).setStatus(BedStatus.AVAILABLE);
    }

    @Test
    void rejectingPendingRequestUpdatesItsStatus() {
        AdmissionRequest request = new AdmissionRequest(patient(), "Medical ward", null);
        when(admissionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        CareResponses.Admission result = service.reject(1L, mock(User.class));

        assertEquals(AdmissionStatus.REJECTED, result.status());
    }

    @Test
    void duplicateActiveAdmissionRequestIsRejected() {
        User patient = patient();
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(patient));
        when(admissionRepository.existsByPatientIdAndStatusIn(
                7L, java.util.List.of(AdmissionStatus.PENDING, AdmissionStatus.ADMITTED))).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.requestAdmission(
                patient, new CareRequests.Admission("Medical ward", "Request")));
    }

    private User patient() {
        User patient = mock(User.class);
        when(patient.getId()).thenReturn(7L);
        when(patient.getName()).thenReturn("Taylor Patient");
        when(patient.getEmail()).thenReturn("patient@example.test");
        return patient;
    }

    private HospitalBed bed(BedStatus status) {
        HospitalBed bed = mock(HospitalBed.class);
        when(bed.getId()).thenReturn(2L);
        when(bed.getBedCode()).thenReturn("M-02");
        when(bed.getWard()).thenReturn("Medical ward");
        when(bed.getBedType()).thenReturn("Standard");
        when(bed.getStatus()).thenReturn(status);
        return bed;
    }
}

package com.mediconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admission_requests")
public class AdmissionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private HospitalBed bed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdmissionStatus status = AdmissionStatus.PENDING;

    @Column(name = "requested_ward", nullable = false, length = 100)
    private String requestedWard;

    @Column(length = 1000)
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected AdmissionRequest() { }

    public AdmissionRequest(User patient, String requestedWard, String reason) {
        this.patient = patient;
        this.requestedWard = requestedWard;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public User getPatient() { return patient; }
    public HospitalBed getBed() { return bed; }
    public User getReviewedBy() { return reviewedBy; }
    public AdmissionStatus getStatus() { return status; }
    public String getRequestedWard() { return requestedWard; }
    public String getReason() { return reason; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getReviewedAt() { return reviewedAt; }

    public void assign(HospitalBed bed, User staff) {
        this.bed = bed;
        this.reviewedBy = staff;
        this.status = AdmissionStatus.ADMITTED;
        this.reviewedAt = Instant.now();
    }

    public void reject(User staff) {
        this.reviewedBy = staff;
        this.status = AdmissionStatus.REJECTED;
        this.reviewedAt = Instant.now();
    }

    public void discharge(User staff) {
        this.reviewedBy = staff;
        this.status = AdmissionStatus.DISCHARGED;
        this.reviewedAt = Instant.now();
    }
}

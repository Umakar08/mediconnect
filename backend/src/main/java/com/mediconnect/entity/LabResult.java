package com.mediconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "lab_results")
public class LabResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entered_by_id", nullable = false)
    private User enteredBy;

    @Column(name = "test_name", nullable = false, length = 120)
    private String testName;

    @Column(name = "result_value", nullable = false, length = 120)
    private String resultValue;

    @Column(length = 40)
    private String unit;

    @Column(name = "reference_range", length = 120)
    private String referenceRange;

    @Column(name = "tested_on", nullable = false)
    private LocalDate testedOn;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected LabResult() { }

    public LabResult(User patient, User enteredBy, String testName, String resultValue,
            String unit, String referenceRange, LocalDate testedOn, String notes) {
        this.patient = patient;
        this.enteredBy = enteredBy;
        this.testName = testName;
        this.resultValue = resultValue;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.testedOn = testedOn;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public User getPatient() { return patient; }
    public User getEnteredBy() { return enteredBy; }
    public String getTestName() { return testName; }
    public String getResultValue() { return resultValue; }
    public String getUnit() { return unit; }
    public String getReferenceRange() { return referenceRange; }
    public LocalDate getTestedOn() { return testedOn; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}

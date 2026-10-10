package com.mediconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospital_beds")
public class HospitalBed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bed_code", nullable = false, unique = true, length = 60)
    private String bedCode;

    @Column(nullable = false, length = 100)
    private String ward;

    @Column(name = "bed_type", nullable = false, length = 60)
    private String bedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BedStatus status = BedStatus.AVAILABLE;

    protected HospitalBed() { }

    public HospitalBed(String bedCode, String ward, String bedType) {
        this.bedCode = bedCode;
        this.ward = ward;
        this.bedType = bedType;
    }

    public Long getId() { return id; }
    public String getBedCode() { return bedCode; }
    public String getWard() { return ward; }
    public String getBedType() { return bedType; }
    public BedStatus getStatus() { return status; }
    public void setStatus(BedStatus status) { this.status = status; }
}

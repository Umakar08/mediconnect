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

@Entity
@Table(name = "lab_report_files")
public class LabReportFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_key", nullable = false, unique = true, length = 80)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    protected LabReportFile() { }

    public LabReportFile(User patient, User uploadedBy, String originalFilename,
            String storageKey, String contentType, long fileSize) {
        this.patient = patient;
        this.uploadedBy = uploadedBy;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public Long getId() { return id; }
    public User getPatient() { return patient; }
    public User getUploadedBy() { return uploadedBy; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public Instant getUploadedAt() { return uploadedAt; }
}

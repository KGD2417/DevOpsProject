package com.streetlight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Complaint ID is required")
    @Size(max = 20, message = "Complaint ID must not exceed 20 characters")
    private String complaintId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "street_name", nullable = false, length = 100)
    @NotBlank(message = "Street name is required")
    @Size(max = 100, message = "Street name must not exceed 100 characters")
    private String streetName;

    @Column(name = "ward", nullable = false, length = 50)
    @NotBlank(message = "Ward is required")
    @Size(max = 50, message = "Ward must not exceed 50 characters")
    private String ward;

    @Column(name = "fault_category", nullable = false, length = 20)
    @NotBlank(message = "Fault category is required")
    @Size(max = 20, message = "Fault category must not exceed 20 characters")
    private String faultCategory;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    private String description;

    @Column(name = "priority", nullable = false, length = 10)
    @NotBlank(message = "Priority is required")
    @Size(max = 10, message = "Priority must not exceed 10 characters")
    private String priority;

    @Column(name = "status", nullable = false, length = 20)
    @NotBlank(message = "Status is required")
    private String status;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // Constructor
    public Complaint() {
        this.complaintId = "SL-" + java.time.LocalDate.now().getYear() + "-" + String.format("%03d", 1);
        this.status = "REPORTED";
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public Complaint(String streetName, String ward, String faultCategory,
                     String description, String priority, String complaintId) {
        this();
        this.streetName = streetName;
        this.ward = ward;
        this.faultCategory = faultCategory;
        this.description = description;
        this.priority = priority;
        this.complaintId = complaintId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getFaultCategory() { return faultCategory; }
    public void setFaultCategory(String faultCategory) { this.faultCategory = faultCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if ("RESOLVED".equals(status) && resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", complaintId='" + complaintId + '\'' +
                ", streetName='" + streetName + '\'' +
                ", ward='" + ward + '\'' +
                ", faultCategory='" + faultCategory + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
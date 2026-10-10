package com.streetlight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, foreignKey = @ForeignKey(name = "fk_status_history_complaint"))
    private Complaint complaint;

    @Column(name = "previous_status", nullable = false, length = 20)
    @NotBlank(message = "Previous status is required")
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    @NotBlank(message = "New status is required")
    private String newStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false, foreignKey = @ForeignKey(name = "fk_status_history_changed_by"))
    private User changedBy;

    // Default constructor
    public StatusHistory() {
        this.changedAt = LocalDateTime.now();
    }

    // Constructor
    public StatusHistory(Complaint complaint, String previousStatus, String newStatus, User changedBy) {
        this.complaint = complaint;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    @PreUpdate
    protected void onUpdate() {
        this.changedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "StatusHistory{" +
                "id=" + id +
                ", complaint=" + complaint.getComplaintId() +
                ", previousStatus='" + previousStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
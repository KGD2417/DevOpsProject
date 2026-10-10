package com.streetlight.service;

import com.streetlight.entity.*;
import com.streetlight.enums.Role;
import com.streetlight.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.*;
import java.time.*;
import java.util.*;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private StatusHistoryRepository statusHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Submit a new complaint (Citizen action)
     */
    @Transactional
    public Complaint submitComplaint(Complaint complaint, User reporter) {
        // Set default values
        if (complaint.getComplaintId() == null || complaint.getComplaintId().isEmpty()) {
            String year = String.valueOf(java.time.LocalDate.now().getYear());
            int seq = complaintRepository.count() + 1;
            complaint.setComplaintId("SL-" + year + "-" + String.format("%03d", seq));
        }
        complaint.setReporterId(reporter.getId());
        complaint.setStatus("REPORTED");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setLastUpdated(LocalDateTime.now());
        
        // Save complaint
        Complaint saved = complaintRepository.save(complaint);
        
        // Create initial status history entry
        StatusHistory history = new StatusHistory(complaint, "REPORTED", "REPORTED", reporter);
        statusHistoryRepository.save(history);
        
        return saved;
    }

    /**
     * Get complaint by ID
     */
    @Transactional(readOnly = true)
    public Complaint getById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));
    }

    /**
     * Get complaints by reporter ID
     */
    @Transactional(readOnly = true)
    public List<Complaint> getByReporterId(Long reporterId) {
        return complaintRepository.findByReporterIdOrderByCreatedAtDesc(reporterId);
    }

    /**
     * Get all complaints (Admin) with optional filtering
     */
    @Transactional(readOnly = true)
    public List<Complaint> getAllWithFilters(
            String ward, String category, String status, String priority,
            String search, Integer page, Integer size) {
        
        // Build query based on filters
        // This is a simplified version - in production would use Specifications
        return complaintRepository.findByWardAndStatusAndPriority(
                ward, status, priority);
    }

    /**
     * Update complaint status (Technician or Admin action)
     */
    @Transactional
    public StatusHistory updateStatus(Long complaintId, String newStatus, User currentUser) {
        Complaint complaint = getById(complaintId);
        
        String currentStatus = complaint.getStatus();
        
        // Validate status transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        // Check if resolution notes are required
        if ("RESOLVED".equals(newStatus) && (complaint.getResolutionNotes() == null || complaint.getResolutionNotes().isEmpty())) {
            throw new RuntimeException("Resolution notes are required for RESOLVED status");
        }
        
        // Update complaint status
        complaint.setStatus(newStatus);
        if ("RESOLVED".equals(newStatus)) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        complaintRepository.save(complaint);
        
        // Create status history entry
        StatusHistory history = new StatusHistory(complaint, currentStatus, newStatus, currentUser);
        statusHistoryRepository.save(history);
        
        return history;
    }

    /**
     * Validate status transition
     */
    private boolean isValidTransition(String current, String next) {
        // Define valid transitions
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("REPORTED", Arrays.asList("ACKNOWLEDGED", "ASSIGNED"));
        validTransitions.put("ACKNOWLEDGED", Arrays.asList("ASSIGNED"));
        validTransitions.put("ASSIGNED", Arrays.asList("IN_PROGRESS"));
        validTransitions.put("IN_PROGRESS", Arrays.asList("RESOLVED"));
        validTransitions.put("RESOLVED", Arrays.asList("CLOSED"));
        validTransitions.put("CLOSED", Collections.emptyList());
        
        return validTransitions.getOrDefault(current, Collections.emptyList()).contains(next);
    }

    /**
     * Assign technician to complaint (Admin action)
     */
    @Transactional
    public Complaint assignTechnician(Long complaintId, Long technicianId) {
        Complaint complaint = getById(complaintId);
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
        
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException("User is not a technician");
        }
        
        complaint.setAssignedTo(technicianId);
        complaintRepository.save(complaint);
        
        return complaint;
    }

    /**
     * Reopen resolved complaint (Admin action)
     */
    @Transactional
    public Complaint reopenComplaint(Long complaintId, User adminUser) {
        Complaint complaint = getById(complaintId);
        
        if (!"RESOLVED".equals(complaint.getStatus())) {
            throw new RuntimeException("Complaint is not in RESOLVED status");
        }
        
        complaint.setStatus("IN_PROGRESS");
        complaint.setResolvedAt(null);
        complaint.setResolutionNotes(null);
        complaintRepository.save(complaint);
        
        // Create status history entry
        StatusHistory history = new StatusHistory(complaint, "RESOLVED", "IN_PROGRESS", adminUser);
        statusHistoryRepository.save(history);
        
        return complaint;
    }
}
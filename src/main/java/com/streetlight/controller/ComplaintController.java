package com.streetlight.controller;

import com.streetlight.entity.*;
import com.streetlight.service.ComplaintService;
import com.streetlight.enums.Role;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.*;

import java.util.*;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;
    
    @Autowired
    private HttpSession session;

    // Helper to get current authenticated user
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "search", required = false) String search) {
        
        User user = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        
        List<Complaint> complaints;
        long total;
        
        if (user != null) {
            // Citizen sees only their own complaints
            complaints = complaintService.getByReporterId(user.getId());
            total = complaints.size();
        } else {
            // Unauthenticated or admin/technician sees all with filters
            complaints = complaintService.getAllWithFilters(
                    ward, category, status, priority, search, page, size);
            total = complaintService.countTotal();
        }
        
        response.put("complaints", complaints);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        User user = getCurrentUser();
        if (user != null) {
            model.addAttribute("complaint", new Complaint());
            model.addAttribute("user", user);
            return "complaints/form :: #complaint-form";
        }
        return "auth/login";
    }

    @PostMapping("")
    @Transactional
    public String create(@ModelAttribute Complaint complaint, RedirectAttributes redirectAttrs) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        complaintService.submitComplaint(complaint, user);
        redirectAttrs.addFlashAttribute("successMessage", 
            "Complaint submitted successfully. Complaint ID: " + complaint.getComplaintId());
        return "redirect:/complaints/" + complaint.getComplaintId();
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<Complaint> detail(@PathVariable Long complaintId) {
        Complaint complaint = complaintService.getById(complaintId);
        return ResponseEntity.ok(complaint);
    }

    @PostMapping("/{complaintId}/status")
    @Transactional
    public ResponseEntity<String> updateStatus(
            @PathVariable Long complaintId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String resolutionNotes) {
        
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        
        try {
            complaintService.updateStatus(complaintId, newStatus, user);
            return ResponseEntity.ok("Status updated to " + newStatus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{complaintId}/assign")
    @Transactional
    public ResponseEntity<String> assignTechnician(
            @PathVariable Long complaintId,
            @RequestParam Long technicianId) {
        
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Insufficient permissions: ADMIN role required");
        }
        
        try {
            complaintService.assignTechnician(complaintId, technicianId);
            return ResponseEntity.ok("Technician assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{complaintId}/reopen")
    @Transactional
    public ResponseEntity<String> reopenComplaint(@PathVariable Long complaintId) {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Insufficient permissions: ADMIN role required");
        }
        
        try {
            complaintService.reopenComplaint(complaintId, user);
            return ResponseEntity.ok("Complaint reopened successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
package com.streetlight.repository;

import com.streetlight.entity.User;
import com.streetlight.entity.Complaint;
import com.streetlight.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
}

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByComplaintId(String complaintId);
    List<Complaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<Complaint> findByAssignedToOrderByCreatedAtDesc(Long assignedTo);
    List<Complaint> findByStatusOrderByCreatedAtDesc(String status);
    List<Complaint> findByPriorityOrderByCreatedAtDesc(String priority);
    List<Complaint> findByWardOrderByCreatedAtDesc(String ward);
    List<Complaint> findByStatusAndPriority(String status, String priority);
    List<Complaint> findByReporterIdAndStatus(Long reporterId, String status);
    @Query("SELECT c FROM Complaint c WHERE c.complaintId = :complaintId")
    Optional<Complaint> findByComplaintIdNative(@Param("complaintId") String complaintId);
    
    // Custom query for dashboard metrics
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status != 'CLOSED'")
    Long countByNotClosed();
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.assignedTo IS NOT NULL")
    Long countByAssignedIsNotNull();
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = 'IN_PROGRESS'")
    Long countByStatusInProgress();
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = 'RESOLVED'")
    Long countByStatusResolved();
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.priority IN ('HIGH', 'EMERGENCY')")
    Long countByPriorityInHighOrEmergency();
    
    @Query("SELECT c FROM Complaint c ORDER BY c.createdAt DESC")
    List<Complaint> findRecentComplaints(@Param("limit") int limit);
}

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByComplaintIdOrderByChangedAtDesc(Long complaintId);
    List<StatusHistory> findByChangedByOrderByChangedAtDesc(User changedBy);
    @Query("SELECT sh FROM StatusHistory sh WHERE sh.previousStatus = :prev AND sh.newStatus = :new")
    List<StatusHistory> findByTransition(@Param("prev") String prev, @Param("new") String newStatus);
}
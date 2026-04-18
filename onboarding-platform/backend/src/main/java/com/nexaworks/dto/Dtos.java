package com.nexaworks.dto;

import com.nexaworks.enums.Role;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

// ─── Auth DTOs ─────────────────────────────────────────────────────────────

@Data
class LoginRequest {
    private String email;
    private String password;
    private String role;   // employee | hr | manager
}

@Data
class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String department;
    private String phone;
    private String gender;
    private Integer age;
    private Integer experienceYears;
}

// ─── Auth Response ─────────────────────────────────────────────────────────

record AuthResponse(
    String token,
    String tokenType,
    UserProfileDto user
) {
    AuthResponse(String token, UserProfileDto user) {
        this(token, "Bearer", user);
    }
}

// ─── User Profile DTO ──────────────────────────────────────────────────────

record UserProfileDto(
    Long   id,
    String name,
    String email,
    String role,
    String department,
    String managerName,
    String location,
    String phone,
    String gender,
    Integer age,
    Integer experienceYears,
    Long    salary,
    String  employeeCode,
    LocalDate joiningDate,
    Integer onboardingProgress,
    Boolean onboardingComplete,
    Integer engagementScore,
    Integer taskCompletion,
    Integer loginFrequency,
    Integer riskScore,
    Double  sentimentScore,
    String  lastFeedback,
    String  avatar,
    Map<String,Boolean> documents,
    Map<String,Boolean> tasks
) {}

// ─── Document Update ───────────────────────────────────────────────────────

@Data
class DocumentUpdateRequest {
    private String docType;      // pan | aadhaar | voter_id | etc.
    private Boolean uploaded;
}

// ─── Task Update ───────────────────────────────────────────────────────────

@Data
class TaskUpdateRequest {
    private String taskKey;      // taskItSetup | taskEmailSetup | etc.
    private Boolean completed;
}

// ─── Feedback ──────────────────────────────────────────────────────────────

@Data
class FeedbackRequest {
    private String content;
    private Integer rating;      // 1-5
    private String category;
}

// ─── Meeting ───────────────────────────────────────────────────────────────

@Data
class MeetingRequest {
    private String title;
    private String description;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingType;
    private Long participantId;
    private String meetLink;
}

// ─── Dashboard Analytics ───────────────────────────────────────────────────

record DashboardStats(
    long totalEmployees,
    long onboardingCompleted,
    long onboardingPending,
    double completionRate,
    double avgEngagement,
    double avgTaskCompletion,
    long highRiskCount,
    long criticalRiskCount,
    long totalHr,
    long totalManagers,
    Map<String,Long> byDepartment,
    Map<String,Long> byRiskLevel,
    Map<String,Long> bySentiment
) {}

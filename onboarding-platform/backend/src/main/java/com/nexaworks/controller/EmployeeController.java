package com.nexaworks.controller;

import com.nexaworks.entity.Feedback;
import com.nexaworks.entity.Meeting;
import com.nexaworks.entity.Notification;
import com.nexaworks.entity.User;
import com.nexaworks.repository.*;
import com.nexaworks.service.EmailService;
import com.nexaworks.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final MeetingRepository meetingRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserService userService;
    private final EmailService emailService;

    // ── Get own profile ────────────────────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(userService.toMap(user));
    }

    // ── Upload document (file + flag) ──────────────────────────────────────
    @PostMapping("/document/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("docType") String docType,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication auth) {

        User user = getUser(auth);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A document file is required for upload."));
        }

        try {
            Path userFolder = Paths.get(uploadDir, user.getId().toString());
            Files.createDirectories(userFolder);
            Path target = userFolder.resolve(docType + "_" + file.getOriginalFilename());
            file.transferTo(target.toFile());
            log.info("📄 Uploaded {} for user {}", docType, user.getEmail());
        } catch (IOException e) {
            log.error("File upload error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save document. Please try again."));
        }

        Map<String,Object> result = userService.updateDocument(user.getId(), docType, true);
        return ResponseEntity.ok(Map.of(
            "message", docType.replace("_"," ").toUpperCase() + " uploaded successfully.",
            "progress", result.get("onboardingProgress"),
            "user", result
        ));
    }

    // ── Remove a document ─────────────────────────────────────────────────
    @DeleteMapping("/document/{docType}")
    public ResponseEntity<?> removeDocument(@PathVariable String docType, Authentication auth) {
        User user = getUser(auth);
        Map<String,Object> result = userService.updateDocument(user.getId(), docType, false);
        return ResponseEntity.ok(Map.of("message", "Document removed.", "user", result));
    }

    // ── Update task status ─────────────────────────────────────────────────
    @PutMapping("/task/{taskKey}")
    public ResponseEntity<?> updateTask(@PathVariable String taskKey,
                                         @RequestBody Map<String,Boolean> body,
                                         Authentication auth) {
        User user = getUser(auth);
        boolean completed = body.getOrDefault("completed", false);
        Map<String,Object> result = userService.updateTask(user.getId(), taskKey, completed);
        return ResponseEntity.ok(Map.of(
            "message", "Task updated.",
            "progress", result.get("onboardingProgress"),
            "user", result
        ));
    }

    // ── Submit feedback ────────────────────────────────────────────────────
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = getUser(auth);
        String content  = (String) body.getOrDefault("content", "");
        int rating      = ((Number) body.getOrDefault("rating", 3)).intValue();
        String category = (String) body.getOrDefault("category", "ONBOARDING");

        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Feedback content is required."));
        }

        Feedback fb = Feedback.builder()
            .userId(user.getId())
            .userName(user.getName())
            .content(content)
            .rating(rating)
            .category(category)
            .sentimentScore(0.5)  // AI service will update this
            .sentimentLabel("Neutral")
            .build();
        feedbackRepo.save(fb);

        // Update user's last feedback
        user.setLastFeedback(content);
        userRepo.save(user);

        log.info("💬 Feedback from {}: rating={}", user.getEmail(), rating);
        return ResponseEntity.ok(Map.of("message", "Feedback submitted. Thank you!"));
    }

    // ── Get own notifications ──────────────────────────────────────────────
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(Authentication auth) {
        User user = getUser(auth);
        List<Notification> notifs = notifRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        long unread = notifRepo.countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("notifications", notifs, "unreadCount", unread));
    }

    // ── Mark all notifications read ────────────────────────────────────────
    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        User user = getUser(auth);
        notifRepo.markAllReadForUser(user.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read."));
    }

    // ── Get own meetings ───────────────────────────────────────────────────
    @GetMapping("/meetings")
    public ResponseEntity<?> getMeetings(Authentication auth) {
        User user = getUser(auth);
        List<Meeting> meetings = meetingRepo.findByParticipantIdOrderByScheduledAtAsc(user.getId());
        return ResponseEntity.ok(meetings);
    }

    // ── Get own feedback history ───────────────────────────────────────────
    @GetMapping("/feedback")
    public ResponseEntity<?> getFeedback(Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(feedbackRepo.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    // ── Dashboard stats for employee ───────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        User user = getUser(auth);
        Map<String,Object> profile = userService.toMap(user);

        // Build document completion
        @SuppressWarnings("unchecked")
        Map<String,Boolean> docs = (Map<String,Boolean>) profile.get("documents");
        long docsUploaded = docs.values().stream().filter(v -> v).count();
        long docsTotal    = docs.size();

        @SuppressWarnings("unchecked")
        Map<String,Boolean> tasks = (Map<String,Boolean>) profile.get("tasks");
        long tasksDone  = tasks.values().stream().filter(v -> v).count();
        long tasksTotal = tasks.size();

        List<Notification> notifs = notifRepo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        List<Meeting> upcomingMeetings = meetingRepo.findByParticipantIdOrderByScheduledAtAsc(user.getId())
            .stream().filter(m -> "SCHEDULED".equals(m.getStatus())).toList();

        return ResponseEntity.ok(Map.of(
            "profile", profile,
            "stats", Map.of(
                "docsUploaded", docsUploaded,
                "docsTotal", docsTotal,
                "tasksDone", tasksDone,
                "tasksTotal", tasksTotal,
                "onboardingProgress", user.getOnboardingProgress(),
                "riskScore", user.getRiskScore(),
                "engagementScore", user.getEngagementScore()
            ),
            "unreadNotifications", notifs.size(),
            "upcomingMeetings", upcomingMeetings
        ));
    }

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

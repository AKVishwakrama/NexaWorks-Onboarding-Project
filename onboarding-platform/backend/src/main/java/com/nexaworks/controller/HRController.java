package com.nexaworks.controller;

import com.nexaworks.entity.*;
import com.nexaworks.enums.Role;
import com.nexaworks.repository.*;
import com.nexaworks.service.EmailService;
import com.nexaworks.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR')")
@CrossOrigin(origins = "*")
public class HRController {

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final MeetingRepository meetingRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserService userService;
    private final EmailService emailService;

    // ── HR Dashboard stats ─────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        List<User> employees = userRepo.findByRole(Role.EMPLOYEE);
        long total     = employees.size();
        long completed = employees.stream().filter(u -> Boolean.TRUE.equals(u.getOnboardingComplete())).count();
        long highRisk  = employees.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 50).count();
        long critRisk  = employees.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 75).count();
        long missingDocs = employees.stream().filter(u -> hasMissingDocs(u)).count();

        double avgEngagement = employees.stream()
            .mapToInt(u -> u.getEngagementScore() != null ? u.getEngagementScore() : 0)
            .average().orElse(0);
        double avgTask = employees.stream()
            .mapToInt(u -> u.getTaskCompletion() != null ? u.getTaskCompletion() : 0)
            .average().orElse(0);
        double avgSentiment = employees.stream()
            .mapToDouble(u -> u.getSentimentScore() != null ? u.getSentimentScore() : 0.5)
            .average().orElse(0.5);

        // By department
        Map<String,Long> byDept = employees.stream()
            .collect(Collectors.groupingBy(u -> u.getDepartment() != null ? u.getDepartment() : "Other",
                     Collectors.counting()));

        // Risk distribution
        Map<String,Long> riskDist = new LinkedHashMap<>();
        riskDist.put("Critical", employees.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 75).count());
        riskDist.put("High",     employees.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 50 && u.getRiskScore() < 75).count());
        riskDist.put("Medium",   employees.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 30 && u.getRiskScore() < 50).count());
        riskDist.put("Low",      employees.stream().filter(u -> u.getRiskScore() == null || u.getRiskScore() < 30).count());

        // Monthly joining trend
        Map<String,Long> monthlyJoins = employees.stream()
            .filter(u -> u.getJoiningDate() != null)
            .collect(Collectors.groupingBy(
                u -> u.getJoiningDate().getYear() + "-" + String.format("%02d", u.getJoiningDate().getMonthValue()),
                Collectors.counting()
            ));

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("summary", Map.ofEntries(
                Map.entry("totalEmployees", total),
                Map.entry("onboardingCompleted", completed),
                Map.entry("onboardingPending", total - completed),
                Map.entry("completionRate", total > 0 ? Math.round(completed * 100.0 / total) : 0),
                Map.entry("highRiskCount", highRisk),
                Map.entry("criticalRiskCount", critRisk),
                Map.entry("missingDocsCount", missingDocs),
                Map.entry("avgEngagement", Math.round(avgEngagement)),
                Map.entry("avgTaskCompletion", Math.round(avgTask)),
                Map.entry("avgSentiment", Math.round(avgSentiment * 100)),
                Map.entry("totalHR", userRepo.countByRole(Role.HR)),
                Map.entry("totalManagers", userRepo.countByRole(Role.MANAGER))
            )),
            Map.entry("byDepartment", byDept),
            Map.entry("riskDistribution", riskDist),
            Map.entry("monthlyJoins", monthlyJoins)
        ));
    }

    // ── All employees list ─────────────────────────────────────────────────
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(
            userRepo.findByRole(Role.EMPLOYEE).stream()
                    .map(userService::toMap).toList()
        );
    }

    // ── Single employee detail ─────────────────────────────────────────────
    @GetMapping("/employees/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        User emp = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return ResponseEntity.ok(userService.toMap(emp));
    }

    // ── Verify / reject a document ─────────────────────────────────────────
    @PutMapping("/employees/{id}/document/{docType}/verify")
    public ResponseEntity<?> verifyDocument(@PathVariable Long id,
                                             @PathVariable String docType,
                                             @RequestBody Map<String,Object> body) {
        boolean approved = (Boolean) body.getOrDefault("approved", true);
        String note = (String) body.getOrDefault("note", "");

        Map<String,Object> result = userService.updateDocument(id, docType, approved);
        User emp = userRepo.findById(id).orElseThrow();

        // Notify employee
        String title = approved ? "Document Verified ✅" : "Document Rejected ❌";
        String msg   = docType.replace("_"," ").toUpperCase() + " has been " +
                       (approved ? "verified by HR." : "rejected. Reason: " + note);

        notifRepo.save(Notification.builder()
            .userId(id).title(title).message(msg)
            .type(approved ? "SUCCESS" : "ALERT")
            .severity(approved ? "LOW" : "MEDIUM").build());

        log.info("📋 HR {} document {} for employee {}", approved ? "verified" : "rejected", docType, emp.getEmail());
        return ResponseEntity.ok(Map.of("message", title, "user", result));
    }

    // ── Send document reminder email ───────────────────────────────────────
    @PostMapping("/employees/{id}/remind-documents")
    public ResponseEntity<?> remindDocuments(@PathVariable Long id) {
        User emp = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));

        List<String> missing = getMissingDocNames(emp);
        if (missing.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No missing documents for this employee."));
        }

        emailService.sendDocumentReminderEmail(emp, missing);

        notifRepo.save(Notification.builder()
            .userId(id).title("Document Reminder Sent 📧")
            .message("HR has sent you a reminder to upload: " + String.join(", ", missing))
            .type("WARNING").severity("MEDIUM").build());

        log.info("📧 Document reminder sent to {}", emp.getEmail());
        return ResponseEntity.ok(Map.of("message", "Reminder email sent to " + emp.getEmail()));
    }

    // ── Trigger risk alert manually ────────────────────────────────────────
    @PostMapping("/employees/{id}/risk-alert")
    public ResponseEntity<?> sendRiskAlert(@PathVariable Long id) {
        User emp = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));

        List<String> reasons = buildRiskReasons(emp);
        emailService.sendHighRiskAlert(emp, emp.getRiskScore() != null ? emp.getRiskScore() : 0, reasons);

        emp.setAlertSent(true);
        emp.setLastAlertAt(LocalDateTime.now());
        userRepo.save(emp);

        return ResponseEntity.ok(Map.of("message", "Risk alert emails sent to HR team."));
    }

    // ── Schedule meeting ───────────────────────────────────────────────────
    @PostMapping("/meetings")
    public ResponseEntity<?> scheduleMeeting(@RequestBody Map<String, Object> body,
                                              Authentication auth) {
        Long participantId = Long.valueOf(body.get("participantId").toString());
        User participant   = userRepo.findById(participantId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        User organizer     = userRepo.findByEmail(auth.getName()).orElseThrow();

        String title       = (String) body.getOrDefault("title", "Meeting");
        String description = (String) body.getOrDefault("description", "");
        String type        = (String) body.getOrDefault("meetingType", "ONE_ON_ONE");
        String meetLink    = (String) body.getOrDefault("meetLink", "https://meet.google.com/nexaworks");
        int duration       = ((Number) body.getOrDefault("durationMinutes", 30)).intValue();
        LocalDateTime at   = LocalDateTime.parse((String) body.get("scheduledAt"));

        Meeting meeting = Meeting.builder()
            .title(title).description(description)
            .scheduledAt(at).durationMinutes(duration)
            .meetingType(type).organizer(organizer.getName())
            .organizerId(organizer.getId())
            .participantId(participantId)
            .meetLink(meetLink).status("SCHEDULED").build();
        meetingRepo.save(meeting);

        // Notify participant
        notifRepo.save(Notification.builder()
            .userId(participantId)
            .title("Meeting Scheduled 📅")
            .message(title + " on " + at.toLocalDate() + " at " + at.toLocalTime())
            .type("INFO").severity("LOW").build());

        // Send email
        emailService.sendMeetingNotification(participant, title, at.toString(), organizer.getName(), meetLink);

        log.info("📅 Meeting scheduled: {} -> {}", organizer.getEmail(), participant.getEmail());
        return ResponseEntity.ok(Map.of("message", "Meeting scheduled and participant notified.", "meeting", meeting));
    }

    // ── All meetings (HR view) ─────────────────────────────────────────────
    @GetMapping("/meetings")
    public ResponseEntity<?> getAllMeetings() {
        return ResponseEntity.ok(meetingRepo.findByStatusOrderByScheduledAtAsc("SCHEDULED"));
    }

    // ── All feedback (HR view) ─────────────────────────────────────────────
    @GetMapping("/feedback")
    public ResponseEntity<?> getAllFeedback() {
        return ResponseEntity.ok(feedbackRepo.findAllByOrderByCreatedAtDesc());
    }

    // ── High risk employees ────────────────────────────────────────────────
    @GetMapping("/high-risk")
    public ResponseEntity<?> getHighRisk() {
        return ResponseEntity.ok(
            userRepo.findAllHighRisk(50).stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(userService::toMap).toList()
        );
    }

    // ── All users list ─────────────────────────────────────────────────────
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(
            userRepo.findAll().stream().map(userService::toMap).toList()
        );
    }

    // ── Send notification to employee ──────────────────────────────────────
    @PostMapping("/employees/{id}/notify")
    public ResponseEntity<?> sendNotification(@PathVariable Long id,
                                               @RequestBody Map<String,String> body) {
        Notification n = Notification.builder()
            .userId(id)
            .title(body.getOrDefault("title", "Notification from HR"))
            .message(body.getOrDefault("message", ""))
            .type(body.getOrDefault("type", "INFO"))
            .severity(body.getOrDefault("severity", "LOW"))
            .build();
        notifRepo.save(n);
        return ResponseEntity.ok(Map.of("message", "Notification sent."));
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private boolean hasMissingDocs(User u) {
        return !Boolean.TRUE.equals(u.getDocPan()) ||
               !Boolean.TRUE.equals(u.getDocAadhaar()) ||
               !Boolean.TRUE.equals(u.getDocOfferLetter());
    }

    private List<String> getMissingDocNames(User u) {
        List<String> missing = new ArrayList<>();
        if (!Boolean.TRUE.equals(u.getDocPan()))          missing.add("PAN Card");
        if (!Boolean.TRUE.equals(u.getDocAadhaar()))      missing.add("Aadhaar Card");
        if (!Boolean.TRUE.equals(u.getDocVoterId()))       missing.add("Voter ID");
        if (!Boolean.TRUE.equals(u.getDocSalarySlip()))   missing.add("Salary Slip");
        if (!Boolean.TRUE.equals(u.getDocOfferLetter()))  missing.add("Offer Letter");
        if (!Boolean.TRUE.equals(u.getDocTenthCert()))    missing.add("10th Certificate");
        if (!Boolean.TRUE.equals(u.getDocTwelfthCert()))  missing.add("12th Certificate");
        if (!Boolean.TRUE.equals(u.getDocDegree()))        missing.add("Degree Certificate");
        return missing;
    }

    private List<String> buildRiskReasons(User u) {
        List<String> r = new ArrayList<>();
        if (u.getEngagementScore() != null && u.getEngagementScore() < 50) r.add("Very low engagement score: " + u.getEngagementScore() + "%");
        else if (u.getEngagementScore() != null && u.getEngagementScore() < 65) r.add("Below-average engagement score: " + u.getEngagementScore() + "%");
        if (u.getTaskCompletion() != null && u.getTaskCompletion() < 50) r.add("Low task completion: " + u.getTaskCompletion() + "%");
        if (u.getLoginFrequency() != null && u.getLoginFrequency() <= 3) r.add("Infrequent system login (freq: " + u.getLoginFrequency() + "/10)");
        if (u.getSentimentScore() != null && u.getSentimentScore() < 0.35) r.add("Highly negative feedback sentiment");
        if (!Boolean.TRUE.equals(u.getOnboardingComplete())) r.add("Onboarding is still incomplete");
        if (hasMissingDocs(u)) r.add("Critical documents are missing");
        return r;
    }
}

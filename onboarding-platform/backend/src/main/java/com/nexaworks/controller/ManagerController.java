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
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','HR')")
@CrossOrigin(origins = "*")
public class ManagerController {

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final MeetingRepository meetingRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserService userService;
    private final EmailService emailService;

    // ── Manager Dashboard ──────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        User manager = getManager(auth);
        List<User> team = userRepo.findByManagerName(manager.getName())
                .stream().filter(u -> u.getRole() == Role.EMPLOYEE).toList();

        long completed  = team.stream().filter(u -> Boolean.TRUE.equals(u.getOnboardingComplete())).count();
        long highRisk   = team.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 50).count();
        double avgEngage = team.stream().mapToInt(u -> u.getEngagementScore() != null ? u.getEngagementScore() : 0)
                               .average().orElse(0);
        double avgTask   = team.stream().mapToInt(u -> u.getTaskCompletion() != null ? u.getTaskCompletion() : 0)
                               .average().orElse(0);

        // Upcoming meetings organized by this manager
        List<Meeting> myMeetings = meetingRepo.findByOrganizerIdOrderByScheduledAtAsc(manager.getId())
                .stream().filter(m -> "SCHEDULED".equals(m.getStatus())).toList();

        // Risk breakdown for team
        Map<String,Long> riskDist = new LinkedHashMap<>();
        riskDist.put("Critical", team.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 75).count());
        riskDist.put("High",     team.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 50 && u.getRiskScore() < 75).count());
        riskDist.put("Medium",   team.stream().filter(u -> u.getRiskScore() != null && u.getRiskScore() >= 30 && u.getRiskScore() < 50).count());
        riskDist.put("Low",      team.stream().filter(u -> u.getRiskScore() == null || u.getRiskScore() < 30).count());

        return ResponseEntity.ok(Map.of(
            "manager", userService.toMap(manager),
            "teamStats", Map.of(
                "totalTeamSize",    team.size(),
                "onboardingDone",   completed,
                "onboardingPending",team.size() - completed,
                "completionRate",   team.isEmpty() ? 0 : Math.round(completed * 100.0 / team.size()),
                "highRiskCount",    highRisk,
                "avgEngagement",    Math.round(avgEngage),
                "avgTaskCompletion",Math.round(avgTask)
            ),
            "riskDistribution", riskDist,
            "upcomingMeetings", myMeetings,
            "teamSize", team.size()
        ));
    }

    // ── Get team ───────────────────────────────────────────────────────────
    @GetMapping("/team")
    public ResponseEntity<?> getTeam(Authentication auth) {
        User manager = getManager(auth);
        List<Map<String,Object>> team = userRepo.findByManagerName(manager.getName())
                .stream().filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(userService::toMap).toList();
        return ResponseEntity.ok(team);
    }

    // ── Get single team member ─────────────────────────────────────────────
    @GetMapping("/team/{id}")
    public ResponseEntity<?> getTeamMember(@PathVariable Long id, Authentication auth) {
        User manager = getManager(auth);
        User emp = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));

        // Verify this employee belongs to this manager
        if (!manager.getName().equals(emp.getManagerName())) {
            return ResponseEntity.status(403).body(Map.of("error", "This employee is not in your team."));
        }
        return ResponseEntity.ok(userService.toMap(emp));
    }

    // ── Schedule meeting (manager) ─────────────────────────────────────────
    @PostMapping("/meetings")
    public ResponseEntity<?> scheduleMeeting(@RequestBody Map<String, Object> body,
                                              Authentication auth) {
        User manager = getManager(auth);
        Long participantId = Long.valueOf(body.get("participantId").toString());
        User participant   = userRepo.findById(participantId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));

        String title       = (String) body.getOrDefault("title", "1:1 Check-in");
        String description = (String) body.getOrDefault("description", "");
        String type        = (String) body.getOrDefault("meetingType", "ONE_ON_ONE");
        String meetLink    = (String) body.getOrDefault("meetLink", "https://meet.google.com/nexaworks-" + participant.getId());
        int duration       = ((Number) body.getOrDefault("durationMinutes", 30)).intValue();
        LocalDateTime at   = LocalDateTime.parse((String) body.get("scheduledAt"));

        Meeting meeting = Meeting.builder()
            .title(title).description(description)
            .scheduledAt(at).durationMinutes(duration)
            .meetingType(type).organizer(manager.getName())
            .organizerId(manager.getId()).participantId(participantId)
            .meetLink(meetLink).status("SCHEDULED").build();
        meetingRepo.save(meeting);

        notifRepo.save(Notification.builder()
            .userId(participantId)
            .title("Meeting Scheduled by " + manager.getName() + " 📅")
            .message(title + " scheduled for " + at.toLocalDate())
            .type("INFO").severity("LOW").build());

        emailService.sendMeetingNotification(participant, title, at.toString(), manager.getName(), meetLink);

        return ResponseEntity.ok(Map.of("message", "Meeting scheduled.", "meeting", meeting));
    }

    // ── Manager's own meetings ─────────────────────────────────────────────
    @GetMapping("/meetings")
    public ResponseEntity<?> getMyMeetings(Authentication auth) {
        User manager = getManager(auth);
        return ResponseEntity.ok(meetingRepo.findByOrganizerIdOrderByScheduledAtAsc(manager.getId()));
    }

    // ── Team feedback (read-only) ──────────────────────────────────────────
    @GetMapping("/team-feedback")
    public ResponseEntity<?> getTeamFeedback(Authentication auth) {
        User manager = getManager(auth);
        List<Long> teamIds = userRepo.findByManagerName(manager.getName())
                .stream().filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(User::getId).toList();
        List<Feedback> feedbacks = feedbackRepo.findAllByOrderByCreatedAtDesc()
                .stream().filter(f -> teamIds.contains(f.getUserId())).toList();
        return ResponseEntity.ok(feedbacks);
    }

    // ── High-risk in team ──────────────────────────────────────────────────
    @GetMapping("/high-risk")
    public ResponseEntity<?> getHighRiskTeam(Authentication auth) {
        User manager = getManager(auth);
        List<Map<String,Object>> highRisk = userRepo.findByManagerName(manager.getName())
                .stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE && u.getRiskScore() != null && u.getRiskScore() >= 40)
                .sorted(Comparator.comparingInt(User::getRiskScore).reversed())
                .map(userService::toMap).toList();
        return ResponseEntity.ok(highRisk);
    }

    // ── Mark meeting done ──────────────────────────────────────────────────
    @PutMapping("/meetings/{id}/complete")
    public ResponseEntity<?> completeMeeting(@PathVariable Long id) {
        Meeting m = meetingRepo.findById(id).orElseThrow();
        m.setStatus("COMPLETED");
        meetingRepo.save(m);
        return ResponseEntity.ok(Map.of("message", "Meeting marked as completed."));
    }

    private User getManager(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Manager not found"));
    }
}

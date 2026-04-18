package com.nexaworks.scheduler;

import com.nexaworks.entity.Notification;
import com.nexaworks.entity.User;
import com.nexaworks.enums.Role;
import com.nexaworks.repository.NotificationRepository;
import com.nexaworks.repository.UserRepository;
import com.nexaworks.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class AlertScheduler {

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final EmailService emailService;

    /**
     * Run on startup to push initial notifications for high-risk employees
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void runOnStartup() {
        log.info("🚀 AlertScheduler: Running initial risk scan on startup...");
        runRiskScan();
        runDocumentReminders();
        runLowEngagementCheck();
    }

    /**
     * Every 6 hours: scan for high-risk employees and send alerts
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void runRiskScan() {
        log.info("⏰ Scheduled: Running risk scan...");
        List<User> unalerted = userRepo.findUnalertedHighRisk();

        for (User emp : unalerted) {
            List<String> reasons = buildRiskReasons(emp);
            if (reasons.isEmpty()) continue;

            try {
                // Send email alert to HR
                emailService.sendHighRiskAlert(emp,
                    emp.getRiskScore() != null ? emp.getRiskScore() : 0,
                    reasons);
            } catch (Exception e) {
                log.error("Failed to send high-risk alert email for {}: {}", emp.getEmail(), e.getMessage());
            }

            try {
                // In-app notification for HR team
                List<User> hrTeam = userRepo.findByRole(Role.HR);
                for (User hr : hrTeam) {
                    notifRepo.save(Notification.builder()
                        .userId(hr.getId())
                        .title("🚨 High Risk Alert: " + emp.getName())
                        .message(emp.getName() + " from " + emp.getDepartment() +
                                 " has a risk score of " + emp.getRiskScore() +
                                 ". Reasons: " + String.join("; ", reasons))
                        .type("ALERT")
                        .severity(emp.getRiskScore() >= 75 ? "CRITICAL" : "HIGH")
                        .build());
                }
            } catch (Exception e) {
                log.error("Failed to create high-risk notifications for {}: {}", emp.getEmail(), e.getMessage());
            }

            try {
                // Notify manager if assigned
                if (emp.getManagerName() != null) {
                    userRepo.findAll().stream()
                        .filter(u -> u.getRole() == Role.MANAGER && emp.getManagerName().equals(u.getName()))
                        .findFirst()
                        .ifPresent(mgr -> notifRepo.save(Notification.builder()
                            .userId(mgr.getId())
                            .title("⚠️ Team Member at Risk: " + emp.getName())
                            .message(emp.getName() + " shows attrition risk (score: " + emp.getRiskScore() +
                                     "). Please schedule a 1:1 meeting.")
                            .type("WARNING")
                            .severity("HIGH")
                            .build()));
                }
            } catch (Exception e) {
                log.error("Failed to notify manager for {}: {}", emp.getEmail(), e.getMessage());
            }

            try {
                emp.setAlertSent(true);
                emp.setLastAlertAt(LocalDateTime.now());
                userRepo.save(emp);
            } catch (Exception e) {
                log.error("Failed to mark alert sent for {}: {}", emp.getEmail(), e.getMessage());
            }

            log.info("🚨 Risk alert processed for {} (score: {})", emp.getName(), emp.getRiskScore());
        }
        log.info("✅ Risk scan complete. Alerted {} employees.", unalerted.size());
    }

    /**
     * Every 12 hours: remind employees with missing critical docs
     */
    @Scheduled(cron = "0 0 */12 * * *")
    public void runDocumentReminders() {
        log.info("⏰ Scheduled: Running document reminder scan...");
        List<User> employees = userRepo.findByRole(Role.EMPLOYEE);
        int reminded = 0;

        for (User emp : employees) {
            List<String> missingDocs = getMissingDocs(emp);
            if (missingDocs.isEmpty() || Boolean.TRUE.equals(emp.getOnboardingComplete())) continue;

            // Only remind if joined in last 30 days
            if (emp.getJoiningDate() != null &&
                emp.getJoiningDate().isBefore(java.time.LocalDate.now().minusDays(30))) {
                continue;
            }

            try {
                emailService.sendDocumentReminderEmail(emp, missingDocs);
            } catch (Exception e) {
                log.error("Failed to send document reminder email to {}: {}", emp.getEmail(), e.getMessage());
            }

            try {
                notifRepo.save(Notification.builder()
                    .userId(emp.getId())
                    .title("⏰ Documents Pending")
                    .message("Please upload the following documents: " + String.join(", ", missingDocs))
                    .type("WARNING")
                    .severity("MEDIUM")
                    .build());
            } catch (Exception e) {
                log.error("Failed to create document reminder notification for {}: {}", emp.getEmail(), e.getMessage());
            }

            reminded++;
        }
        log.info("📄 Document reminders sent to {} employees.", reminded);
    }

    /**
     * Every 24 hours: check for low engagement
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void runLowEngagementCheck() {
        log.info("⏰ Scheduled: Running low engagement check...");
        List<User> lowEngagement = userRepo.findByRole(Role.EMPLOYEE).stream()
            .filter(u -> u.getEngagementScore() != null && u.getEngagementScore() < 45)
            .collect(Collectors.toList());

        for (User emp : lowEngagement) {
            try {
                emailService.sendLowEngagementAlert(emp);
            } catch (Exception e) {
                log.error("Failed to send low engagement email for {}: {}", emp.getEmail(), e.getMessage());
            }

            try {
                userRepo.findByRole(Role.HR).stream().findFirst().ifPresent(hr ->
                    notifRepo.save(Notification.builder()
                        .userId(hr.getId())
                        .title("📉 Low Engagement: " + emp.getName())
                        .message(emp.getName() + " has an engagement score of only " +
                                 emp.getEngagementScore() + "%. Immediate attention required.")
                        .type("WARNING")
                        .severity("HIGH")
                        .build())
                );
            } catch (Exception e) {
                log.error("Failed to create low engagement notification for {}: {}", emp.getEmail(), e.getMessage());
            }
        }
        log.info("💜 Low engagement alerts sent for {} employees.", lowEngagement.size());
    }

    private List<String> buildRiskReasons(User u) {
        List<String> r = new ArrayList<>();
        if (u.getEngagementScore() != null && u.getEngagementScore() < 50)
            r.add("Very low engagement: " + u.getEngagementScore() + "%");
        if (u.getTaskCompletion() != null && u.getTaskCompletion() < 50)
            r.add("Low task completion: " + u.getTaskCompletion() + "%");
        if (u.getLoginFrequency() != null && u.getLoginFrequency() <= 3)
            r.add("Infrequent logins");
        if (u.getSentimentScore() != null && u.getSentimentScore() < 0.35)
            r.add("Negative feedback sentiment");
        if (!Boolean.TRUE.equals(u.getOnboardingComplete()))
            r.add("Onboarding incomplete");
        if (!getMissingDocs(u).isEmpty())
            r.add("Missing documents: " + String.join(", ", getMissingDocs(u)));
        return r;
    }

    private List<String> getMissingDocs(User u) {
        List<String> missing = new ArrayList<>();
        if (!Boolean.TRUE.equals(u.getDocPan()))         missing.add("PAN Card");
        if (!Boolean.TRUE.equals(u.getDocAadhaar()))     missing.add("Aadhaar");
        if (!Boolean.TRUE.equals(u.getDocOfferLetter())) missing.add("Offer Letter");
        return missing;
    }
}

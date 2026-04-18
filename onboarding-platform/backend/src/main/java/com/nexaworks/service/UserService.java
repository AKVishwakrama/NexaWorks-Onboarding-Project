package com.nexaworks.service;

import com.nexaworks.entity.Notification;
import com.nexaworks.entity.User;
import com.nexaworks.enums.Role;
import com.nexaworks.repository.NotificationRepository;
import com.nexaworks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Fetch user profile as DTO map ──────────────────────────────────────
    public Map<String,Object> getUserProfile(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        return toMap(u);
    }

    // ── All employees (for HR) ─────────────────────────────────────────────
    public List<Map<String,Object>> getAllEmployees() {
        return userRepo.findByRole(Role.EMPLOYEE).stream()
                .map(this::toMap).toList();
    }

    // ── Team for a manager ─────────────────────────────────────────────────
    public List<Map<String,Object>> getTeamForManager(String managerName) {
        return userRepo.findByManagerName(managerName).stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(this::toMap).toList();
    }

    // ── Update document flag ───────────────────────────────────────────────
    @Transactional
    public Map<String,Object> updateDocument(Long userId, String docType, boolean uploaded) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        switch (docType.toLowerCase()) {
            case "pan"         -> user.setDocPan(uploaded);
            case "aadhaar"     -> user.setDocAadhaar(uploaded);
            case "voter_id"    -> user.setDocVoterId(uploaded);
            case "passport"    -> user.setDocPassport(uploaded);
            case "salary_slip" -> user.setDocSalarySlip(uploaded);
            case "offer_letter"-> user.setDocOfferLetter(uploaded);
            case "tenth_cert"  -> user.setDocTenthCert(uploaded);
            case "twelfth_cert"-> user.setDocTwelfthCert(uploaded);
            case "degree"      -> user.setDocDegree(uploaded);
            case "experience_letter" -> user.setDocExperienceLetter(uploaded);
            case "relieving_letter"  -> user.setDocRelievingLetter(uploaded);
            case "photo"       -> user.setDocPhoto(uploaded);
        }
        recalcProgress(user);
        User saved = userRepo.save(user);

        // Push in-app notification
        notifRepo.save(Notification.builder()
                .userId(userId)
                .title("Document " + (uploaded ? "Uploaded ✅" : "Removed"))
                .message(docType.replace("_"," ").toUpperCase() + " has been " + (uploaded ? "successfully uploaded." : "removed."))
                .type(uploaded ? "SUCCESS" : "WARNING")
                .severity("LOW")
                .build());

        return toMap(saved);
    }

    // ── Update task flag ───────────────────────────────────────────────────
    @Transactional
    public Map<String,Object> updateTask(Long userId, String taskKey, boolean completed) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        switch (taskKey) {
            case "taskItSetup"      -> user.setTaskItSetup(completed);
            case "taskEmailSetup"   -> user.setTaskEmailSetup(completed);
            case "taskBuddyMeet"    -> user.setTaskBuddyMeet(completed);
            case "taskTeamIntro"    -> user.setTaskTeamIntro(completed);
            case "taskHrOrientation"-> user.setTaskHrOrientation(completed);
            case "taskPoliciesRead" -> user.setTaskPoliciesRead(completed);
            case "taskFirstProject" -> user.setTaskFirstProject(completed);
            case "taskTraining1"    -> user.setTaskTraining1(completed);
            case "taskTraining2"    -> user.setTaskTraining2(completed);
            case "taskTraining3"    -> user.setTaskTraining3(completed);
        }
        recalcProgress(user);
        User saved = userRepo.save(user);

        if (completed) {
            notifRepo.save(Notification.builder()
                    .userId(userId)
                    .title("Task Completed ✅")
                    .message(taskKey.replaceAll("([A-Z])"," $1").trim() + " marked as complete.")
                    .type("SUCCESS").severity("LOW").build());
        }
        return toMap(saved);
    }

    // ── Update AI scores (called by AI microservice proxy) ─────────────────
    @Transactional
    public void updateAiScores(Long userId, int riskScore, double sentimentScore, String lastFeedback) {
        userRepo.findById(userId).ifPresent(u -> {
            u.setRiskScore(riskScore);
            u.setSentimentScore(sentimentScore);
            if (lastFeedback != null) u.setLastFeedback(lastFeedback);
            userRepo.save(u);
        });
    }

    // ── Recalculate progress based on docs + tasks ─────────────────────────
    private void recalcProgress(User u) {
        // Docs weight: 40 points (8 core docs × 5)
        int docScore = 0;
        if (Boolean.TRUE.equals(u.getDocPan()))           docScore += 5;
        if (Boolean.TRUE.equals(u.getDocAadhaar()))       docScore += 5;
        if (Boolean.TRUE.equals(u.getDocVoterId()))        docScore += 5;
        if (Boolean.TRUE.equals(u.getDocSalarySlip()))     docScore += 5;
        if (Boolean.TRUE.equals(u.getDocOfferLetter()))    docScore += 5;
        if (Boolean.TRUE.equals(u.getDocTenthCert()))      docScore += 5;
        if (Boolean.TRUE.equals(u.getDocTwelfthCert()))    docScore += 5;
        if (Boolean.TRUE.equals(u.getDocDegree()))         docScore += 5;

        // Tasks weight: 60 points (10 tasks × 6)
        int taskScore = 0;
        if (Boolean.TRUE.equals(u.getTaskItSetup()))        taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskEmailSetup()))     taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskBuddyMeet()))      taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskTeamIntro()))      taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskHrOrientation()))  taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskPoliciesRead()))   taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskFirstProject()))   taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskTraining1()))      taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskTraining2()))      taskScore += 6;
        if (Boolean.TRUE.equals(u.getTaskTraining3()))      taskScore += 6;

        int total = Math.min(100, docScore + taskScore);
        u.setOnboardingProgress(total);
        u.setOnboardingComplete(total >= 90);
    }

    // ── Convert entity to API response map ────────────────────────────────
    public Map<String,Object> toMap(User u) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole().name().toLowerCase());
        m.put("department", u.getDepartment());
        m.put("managerName", u.getManagerName());
        m.put("location", u.getLocation());
        m.put("phone", u.getPhone());
        m.put("gender", u.getGender());
        m.put("age", u.getAge());
        m.put("experienceYears", u.getExperienceYears());
        m.put("salary", u.getSalary());
        m.put("employeeCode", u.getEmployeeCode());
        m.put("joiningDate", u.getJoiningDate());
        m.put("onboardingProgress", u.getOnboardingProgress());
        m.put("onboardingComplete", u.getOnboardingComplete());
        m.put("engagementScore", u.getEngagementScore());
        m.put("taskCompletion", u.getTaskCompletion());
        m.put("loginFrequency", u.getLoginFrequency());
        m.put("riskScore", u.getRiskScore());
        m.put("sentimentScore", u.getSentimentScore());
        m.put("lastFeedback", u.getLastFeedback());
        m.put("createdAt", u.getCreatedAt());
        m.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + u.getName().replace(" ",""));
        m.put("documents", Map.ofEntries(
            Map.entry("pan",              Boolean.TRUE.equals(u.getDocPan())),
            Map.entry("aadhaar",          Boolean.TRUE.equals(u.getDocAadhaar())),
            Map.entry("voter_id",         Boolean.TRUE.equals(u.getDocVoterId())),
            Map.entry("passport",         Boolean.TRUE.equals(u.getDocPassport())),
            Map.entry("salary_slip",      Boolean.TRUE.equals(u.getDocSalarySlip())),
            Map.entry("offer_letter",     Boolean.TRUE.equals(u.getDocOfferLetter())),
            Map.entry("tenth_cert",       Boolean.TRUE.equals(u.getDocTenthCert())),
            Map.entry("twelfth_cert",     Boolean.TRUE.equals(u.getDocTwelfthCert())),
            Map.entry("degree",           Boolean.TRUE.equals(u.getDocDegree())),
            Map.entry("experience_letter",Boolean.TRUE.equals(u.getDocExperienceLetter())),
            Map.entry("relieving_letter", Boolean.TRUE.equals(u.getDocRelievingLetter())),
            Map.entry("photo",            Boolean.TRUE.equals(u.getDocPhoto()))
        ));
        m.put("tasks", Map.ofEntries(
            Map.entry("taskItSetup",        Boolean.TRUE.equals(u.getTaskItSetup())),
            Map.entry("taskEmailSetup",     Boolean.TRUE.equals(u.getTaskEmailSetup())),
            Map.entry("taskBuddyMeet",      Boolean.TRUE.equals(u.getTaskBuddyMeet())),
            Map.entry("taskTeamIntro",      Boolean.TRUE.equals(u.getTaskTeamIntro())),
            Map.entry("taskHrOrientation",  Boolean.TRUE.equals(u.getTaskHrOrientation())),
            Map.entry("taskPoliciesRead",   Boolean.TRUE.equals(u.getTaskPoliciesRead())),
            Map.entry("taskFirstProject",   Boolean.TRUE.equals(u.getTaskFirstProject())),
            Map.entry("taskTraining1",      Boolean.TRUE.equals(u.getTaskTraining1())),
            Map.entry("taskTraining2",      Boolean.TRUE.equals(u.getTaskTraining2())),
            Map.entry("taskTraining3",      Boolean.TRUE.equals(u.getTaskTraining3()))
        ));
        return m;
    }
}

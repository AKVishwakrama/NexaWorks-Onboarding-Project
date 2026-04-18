package com.nexaworks.entity;

import com.nexaworks.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ── Profile ────────────────────────────────────────────
    private String department;
    private String managerName;         // for EMPLOYEE / HR
    private String location;
    private String phone;
    private String gender;
    private Integer age;
    private Integer experienceYears;
    private Long salary;
    private LocalDate joiningDate;
    private String employeeCode;        // e.g. NW-EMP-001

    // ── Onboarding ─────────────────────────────────────────
    @Column(nullable = false)
    private Integer onboardingProgress = 0;     // 0-100

    @Column(nullable = false)
    private Boolean onboardingComplete = false;

    @Column(nullable = false)
    private Integer engagementScore = 70;

    @Column(nullable = false)
    private Integer taskCompletion = 0;

    @Column(nullable = false)
    private Integer loginFrequency = 5;         // 1-10

    // ── Documents ──────────────────────────────────────────
    private Boolean docPan          = false;
    private Boolean docAadhaar      = false;
    private Boolean docVoterId      = false;
    private Boolean docPassport     = false;
    private Boolean docSalarySlip   = false;
    private Boolean docOfferLetter  = false;
    private Boolean docTenthCert    = false;
    private Boolean docTwelfthCert  = false;
    private Boolean docDegree       = false;
    private Boolean docExperienceLetter = false;
    private Boolean docRelievingLetter  = false;
    private Boolean docPhoto        = false;

    // ── Tasks (used by checklist) ──────────────────────────
    private Boolean taskItSetup         = false;
    private Boolean taskEmailSetup       = false;
    private Boolean taskBuddyMeet        = false;
    private Boolean taskTeamIntro        = false;
    private Boolean taskHrOrientation    = false;
    private Boolean taskPoliciesRead     = false;
    private Boolean taskFirstProject     = false;
    private Boolean taskTraining1        = false;
    private Boolean taskTraining2        = false;
    private Boolean taskTraining3        = false;

    // ── AI scores (updated by AI service) ──────────────────
    @Column(nullable = false)
    private Integer riskScore = 0;              // 0-100

    @Column(columnDefinition = "TEXT")
    private String lastFeedback;

    @Column(nullable = false)
    private Double sentimentScore = 0.5;        // 0-1

    // ── Alert flags ────────────────────────────────────────
    private Boolean alertSent = false;
    private LocalDateTime lastAlertAt;

    // ── Audit ──────────────────────────────────────────────
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Spring Security ────────────────────────────────────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername()              { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

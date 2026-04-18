package com.nexaworks.controller;

import com.nexaworks.entity.User;
import com.nexaworks.enums.Role;
import com.nexaworks.repository.UserRepository;
import com.nexaworks.security.JwtUtils;
import com.nexaworks.service.EmailService;
import com.nexaworks.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final UserService userService;

    /**
     * LOGIN
     * Body: { email, password, role }
     * Returns JWT + user profile
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").toLowerCase().trim();
        String password = body.getOrDefault("password", "");
        String roleStr = body.getOrDefault("role", "").toUpperCase().trim();

        if (email.isEmpty() || password.isEmpty() || roleStr.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, password and role are required."));
        }

        // Find user first so we can give role-specific error
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "No account found with this email address.",
                "hint", "Check the demo credentials in the login page."
            ));
        }

        // Role mismatch check
        Role requestedRole;
        try {
            requestedRole = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + roleStr));
        }

        if (user.getRole() != requestedRole) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Role mismatch. Your account role is: " + user.getRole().name().toLowerCase() + ". Please select the correct role."
            ));
        }

        // Authenticate password
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid password. Please try again."));
        }

        String token = jwtUtils.generateToken(user, user.getId(), user.getRole().name());
        log.info("✅ Login: {} ({})", user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "tokenType", "Bearer",
            "user", userService.toMap(user)
        ));
    }

    /**
     * REGISTER (self-registration for demo)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String email = ((String) body.getOrDefault("email", "")).toLowerCase().trim();
        String name  = (String) body.getOrDefault("name", "");
        String pwd   = (String) body.getOrDefault("password", "");
        String role  = ((String) body.getOrDefault("role", "employee")).toUpperCase();

        if (email.isEmpty() || name.isEmpty() || pwd.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name, email and password are required."));
        }
        if (pwd.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }
        if (userRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered. Please login instead."));
        }

        long count = userRepo.count();
        User u = User.builder()
            .name(name)
            .email(email)
            .password(encoder.encode(pwd))
            .role(Role.valueOf(role))
            .department((String) body.getOrDefault("department", "General"))
            .phone((String) body.getOrDefault("phone", ""))
            .gender((String) body.getOrDefault("gender", ""))
            .joiningDate(LocalDate.now())
            .employeeCode("NW-EMP-" + String.format("%03d", count + 1))
            .onboardingProgress(5)
            .onboardingComplete(false)
            .engagementScore(70)
            .taskCompletion(0)
            .loginFrequency(5)
            .riskScore(25)
            .sentimentScore(0.5)
            .alertSent(false)
            .build();

        User saved = userRepo.save(u);
        emailService.sendWelcomeEmail(saved);
        log.info("🆕 Registered: {} ({})", saved.getEmail(), saved.getRole());

        return ResponseEntity.ok(Map.of(
            "message", "Registration successful! Welcome email sent.",
            "employeeCode", saved.getEmployeeCode()
        ));
    }

    /**
     * GET CURRENT USER (from JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userService.toMap(user));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "NexaWorks Auth",
            "users", userRepo.count()
        ));
    }
}

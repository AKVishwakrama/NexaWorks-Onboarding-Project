package com.nexaworks.service;

import com.nexaworks.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.alert.emails}")
    private String alertEmailsRaw;

    // ── Async email sender ─────────────────────────────────────────────────
    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom("NexaWorks Platform <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("📧 Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ── Send to multiple alert recipients ──────────────────────────────────
    @Async
    public void sendAlertToHRTeam(String subject, String htmlBody) {
        String[] recipients = alertEmailsRaw.split(",");
        for (String email : recipients) {
            sendEmail(email.trim(), subject, htmlBody);
        }
    }

    // ── Welcome email on joining ───────────────────────────────────────────
    public void sendWelcomeEmail(User user) {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f4ff;margin:0;padding:0}
              .wrapper{max-width:600px;margin:30px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(80,80,200,.12)}
              .header{background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:40px 32px;text-align:center}
              .header h1{color:#fff;margin:0;font-size:26px;font-weight:700;letter-spacing:-.5px}
              .header p{color:#c7d2fe;margin:8px 0 0}
              .body{padding:36px 32px}
              .greeting{font-size:20px;font-weight:600;color:#1e1b4b;margin-bottom:8px}
              .text{color:#4b5563;line-height:1.7;margin-bottom:16px}
              .info-card{background:#f5f3ff;border-left:4px solid #6366f1;border-radius:8px;padding:16px 20px;margin:20px 0}
              .info-row{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #e9d5ff;font-size:14px}
              .info-row:last-child{border-bottom:none}
              .label{color:#6b7280;font-weight:500}
              .value{color:#1e1b4b;font-weight:600}
              .cta{display:block;text-align:center;margin:28px 0}
              .btn{background:linear-gradient(135deg,#4f46e5,#7c3aed);color:#fff!important;text-decoration:none;padding:14px 36px;border-radius:50px;font-weight:600;font-size:15px;display:inline-block}
              .checklist{background:#f0fdf4;border-radius:8px;padding:16px 20px;margin:20px 0}
              .check-item{color:#166534;font-size:14px;padding:4px 0}
              .footer{background:#f8fafc;padding:20px 32px;text-align:center;color:#9ca3af;font-size:12px}
              .nexa-logo{font-weight:800;color:#4f46e5;font-size:18px}
            </style></head><body>
            <div class="wrapper">
              <div class="header">
                <h1>🎉 Welcome to NexaWorks!</h1>
                <p>Your onboarding journey starts today</p>
              </div>
              <div class="body">
                <div class="greeting">Namaste, %s! 🙏</div>
                <p class="text">We are thrilled to have you join the NexaWorks family. This email contains everything you need to get started. Your personalized onboarding dashboard is ready and waiting for you.</p>
                
                <div class="info-card">
                  <div class="info-row"><span class="label">Employee Code</span><span class="value">%s</span></div>
                  <div class="info-row"><span class="label">Department</span><span class="value">%s</span></div>
                  <div class="info-row"><span class="label">Reporting Manager</span><span class="value">%s</span></div>
                  <div class="info-row"><span class="label">Joining Date</span><span class="value">%s</span></div>
                  <div class="info-row"><span class="label">Login Email</span><span class="value">%s</span></div>
                </div>

                <div class="checklist">
                  <strong style="color:#166534;display:block;margin-bottom:8px">📋 Your First Week Checklist</strong>
                  <div class="check-item">☐ Upload all required documents (PAN, Aadhaar, Voter ID, Salary Slip)</div>
                  <div class="check-item">☐ Complete IT & email setup</div>
                  <div class="check-item">☐ Attend HR orientation session</div>
                  <div class="check-item">☐ Meet your buddy/mentor</div>
                  <div class="check-item">☐ Complete mandatory compliance training</div>
                  <div class="check-item">☐ Team introduction session</div>
                </div>

                <div class="cta">
                  <a href="http://localhost:5173" class="btn">🚀 Go to Your Dashboard</a>
                </div>

                <p class="text" style="font-size:13px;color:#6b7280">If you have any questions, reach out to your HR team. We are always here to help you settle in comfortably.</p>
              </div>
              <div class="footer">
                <div class="nexa-logo">NexaWorks</div>
                <p>AI-Powered Workforce Intelligence Platform<br>© 2024 NexaWorks. All rights reserved.</p>
              </div>
            </div>
            </body></html>
            """.formatted(
                user.getName(),
                user.getEmployeeCode() != null ? user.getEmployeeCode() : "NW-EMP-" + user.getId(),
                user.getDepartment(),
                user.getManagerName() != null ? user.getManagerName() : "HR Team",
                user.getJoiningDate() != null ? user.getJoiningDate().toString() : "Today",
                user.getEmail()
            );
        sendEmail(user.getEmail(), "🎉 Welcome to NexaWorks, " + user.getName() + "!", html);
    }

    // ── High-risk alert email to HR team ──────────────────────────────────
    public void sendHighRiskAlert(User employee, int riskScore, List<String> reasons) {
        String reasonsHtml = reasons.stream()
            .map(r -> "<li style='padding:4px 0;color:#dc2626'>⚠️ " + r + "</li>")
            .reduce("", String::concat);

        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#fef2f2;margin:0;padding:0}
              .wrapper{max-width:600px;margin:30px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(220,38,38,.15)}
              .header{background:linear-gradient(135deg,#dc2626,#b91c1c);padding:32px;text-align:center}
              .header h1{color:#fff;margin:0;font-size:24px}
              .header p{color:#fecaca;margin:8px 0 0;font-size:14px}
              .body{padding:32px}
              .risk-badge{display:inline-block;background:#fee2e2;color:#dc2626;border:2px solid #dc2626;border-radius:50px;padding:8px 24px;font-size:20px;font-weight:700;margin:16px 0}
              .employee-card{background:#f9fafb;border-radius:12px;padding:20px;margin:16px 0;border:1px solid #e5e7eb}
              .row{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #f3f4f6;font-size:14px}
              .row:last-child{border-bottom:none}
              .reasons-box{background:#fff5f5;border-left:4px solid #dc2626;border-radius:8px;padding:16px 20px;margin:16px 0}
              .action-box{background:#eff6ff;border-radius:12px;padding:20px;margin:16px 0}
              .action-item{color:#1d4ed8;font-size:14px;padding:4px 0}
              .btn{background:#dc2626;color:#fff!important;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:600;display:inline-block;margin:4px}
              .footer{background:#f8fafc;padding:16px 32px;text-align:center;color:#9ca3af;font-size:12px}
            </style></head><body>
            <div class="wrapper">
              <div class="header">
                <h1>🚨 Attrition Risk Alert</h1>
                <p>Immediate HR Attention Required</p>
              </div>
              <div class="body">
                <p style="color:#374151;font-size:15px">The NexaWorks AI system has flagged an employee as <strong>high attrition risk</strong>. Please review and take action within 24 hours.</p>
                
                <div style="text-align:center">
                  <div class="risk-badge">Risk Score: %d / 100</div>
                </div>

                <div class="employee-card">
                  <div class="row"><span style="color:#6b7280">Name</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#6b7280">Department</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#6b7280">Email</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#6b7280">Manager</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#6b7280">Joining Date</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#6b7280">Engagement Score</span><strong>%d%%</strong></div>
                  <div class="row"><span style="color:#6b7280">Task Completion</span><strong>%d%%</strong></div>
                </div>

                <div class="reasons-box">
                  <strong style="color:#dc2626;display:block;margin-bottom:8px">⚠️ Risk Factors Identified:</strong>
                  <ul style="margin:0;padding-left:20px">%s</ul>
                </div>

                <div class="action-box">
                  <strong style="color:#1d4ed8;display:block;margin-bottom:8px">✅ Recommended Actions:</strong>
                  <div class="action-item">→ Schedule a 1:1 meeting with the employee within 24 hours</div>
                  <div class="action-item">→ Review their onboarding checklist for blockers</div>
                  <div class="action-item">→ Assign a senior buddy/mentor immediately</div>
                  <div class="action-item">→ Send engagement survey to understand concerns</div>
                  <div class="action-item">→ Discuss role clarity and expectations</div>
                </div>

                <p style="text-align:center;margin-top:20px">
                  <a href="http://localhost:5173/hr/dashboard" class="btn">🔍 View HR Dashboard</a>
                  <a href="http://localhost:5173/hr/employees/%s" class="btn" style="background:#4f46e5">👤 View Profile</a>
                </p>
              </div>
              <div class="footer">
                <p>NexaWorks AI Platform • Automated Risk Alert<br>Generated at %s</p>
              </div>
            </div>
            </body></html>
            """.formatted(
                riskScore,
                employee.getName(), employee.getDepartment(), employee.getEmail(),
                employee.getManagerName() != null ? employee.getManagerName() : "N/A",
                employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : "N/A",
                employee.getEngagementScore(), employee.getTaskCompletion(),
                reasonsHtml,
                employee.getId(),
                java.time.LocalDateTime.now().toString().replace("T"," ").substring(0,19)
            );

        sendAlertToHRTeam("🚨 [NexaWorks] Attrition Risk Alert – " + employee.getName() + " (Score: " + riskScore + ")", html);
    }

    // ── Missing document reminder to employee ──────────────────────────────
    public void sendDocumentReminderEmail(User employee, List<String> missingDocs) {
        String docsHtml = missingDocs.stream()
            .map(d -> "<div style='padding:8px 12px;margin:6px 0;background:#fef9c3;border-left:3px solid #eab308;border-radius:6px;font-weight:600;color:#854d0e'>📄 " + d + "</div>")
            .reduce("", String::concat);

        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#fffbeb;margin:0;padding:0}
              .wrapper{max-width:580px;margin:30px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(234,179,8,.15)}
              .header{background:linear-gradient(135deg,#d97706,#f59e0b);padding:32px;text-align:center}
              .header h1{color:#fff;margin:0;font-size:22px}
              .body{padding:32px}
              .btn{background:#d97706;color:#fff!important;text-decoration:none;padding:14px 32px;border-radius:50px;font-weight:600;display:inline-block;margin-top:16px}
              .footer{padding:16px;text-align:center;color:#9ca3af;font-size:12px}
            </style></head><body>
            <div class="wrapper">
              <div class="header">
                <h1>⏰ Documents Pending – Action Required</h1>
              </div>
              <div class="body">
                <p style="color:#374151;font-size:15px">Hi <strong>%s</strong>,</p>
                <p style="color:#374151">Your onboarding is <strong>incomplete</strong> because the following documents have not been uploaded yet. Please upload them at the earliest to avoid delays in your payroll and system access.</p>
                %s
                <p style="color:#374151;margin-top:20px">Please upload these documents within <strong>48 hours</strong> to keep your onboarding on track.</p>
                <div style="text-align:center;margin-top:24px">
                  <a href="http://localhost:5173/employee/documents" class="btn">📤 Upload Documents Now</a>
                </div>
                <p style="color:#6b7280;font-size:13px;margin-top:20px">If you face any issues uploading, please contact your HR team at %s</p>
              </div>
              <div class="footer">NexaWorks Onboarding Platform • This is an automated reminder</div>
            </div>
            </body></html>
            """.formatted(employee.getName(), docsHtml, fromEmail);

        sendEmail(employee.getEmail(), "⏰ [Action Required] Upload Pending Documents – NexaWorks", html);
    }

    // ── Meeting scheduled email ───────────────────────────────────────────
    public void sendMeetingNotification(User participant, String meetingTitle,
                                        String scheduledAt, String organizer, String meetLink) {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f9ff;margin:0;padding:0}
              .wrapper{max-width:560px;margin:30px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(14,165,233,.12)}
              .header{background:linear-gradient(135deg,#0ea5e9,#6366f1);padding:32px;text-align:center}
              .header h1{color:#fff;margin:0;font-size:22px}
              .body{padding:32px}
              .meet-card{background:#f0f9ff;border-radius:12px;padding:20px;margin:16px 0;border:1px solid #bae6fd}
              .row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #e0f2fe;font-size:14px}
              .row:last-child{border-bottom:none}
              .btn{background:#0ea5e9;color:#fff!important;text-decoration:none;padding:14px 32px;border-radius:50px;font-weight:600;display:inline-block}
              .footer{padding:16px;text-align:center;color:#9ca3af;font-size:12px}
            </style></head><body>
            <div class="wrapper">
              <div class="header"><h1>📅 Meeting Scheduled</h1></div>
              <div class="body">
                <p style="color:#374151">Hi <strong>%s</strong>, a meeting has been scheduled for you.</p>
                <div class="meet-card">
                  <div class="row"><span style="color:#0369a1">Meeting</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#0369a1">Date & Time</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#0369a1">Organizer</span><strong>%s</strong></div>
                  <div class="row"><span style="color:#0369a1">Meet Link</span><a href="%s" style="color:#6366f1">%s</a></div>
                </div>
                <div style="text-align:center;margin-top:20px">
                  <a href="%s" class="btn">🎥 Join Meeting</a>
                </div>
              </div>
              <div class="footer">NexaWorks Platform • Automated Calendar Notification</div>
            </div>
            </body></html>
            """.formatted(participant.getName(), meetingTitle, scheduledAt, organizer,
                          meetLink != null ? meetLink : "#", meetLink != null ? meetLink : "TBD",
                          meetLink != null ? meetLink : "#");

        sendEmail(participant.getEmail(), "📅 [NexaWorks] Meeting: " + meetingTitle, html);
    }

    // ── Low engagement alert ──────────────────────────────────────────────
    public void sendLowEngagementAlert(User employee) {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#f5f3ff;margin:0;padding:0}
              .wrapper{max-width:560px;margin:30px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(124,58,237,.12)}
              .header{background:linear-gradient(135deg,#7c3aed,#4f46e5);padding:32px;text-align:center}
              .header h1{color:#fff;margin:0;font-size:22px}
              .body{padding:32px}
              .btn{background:#7c3aed;color:#fff!important;text-decoration:none;padding:14px 32px;border-radius:50px;font-weight:600;display:inline-block}
              .footer{padding:16px;text-align:center;color:#9ca3af;font-size:12px}
            </style></head><body>
            <div class="wrapper">
              <div class="header"><h1>💜 We're Here for You</h1></div>
              <div class="body">
                <p style="color:#374151">Hi <strong>HR Team</strong>,</p>
                <p style="color:#374151">Our AI system has detected a significant drop in engagement for <strong>%s</strong> (%s department). Their current engagement score is <strong>%d%%</strong>.</p>
                <p style="color:#374151">We recommend reaching out within <strong>24 hours</strong> for a supportive check-in call.</p>
                <div style="text-align:center;margin:24px 0">
                  <a href="http://localhost:5173/hr/employees/%s" class="btn">👤 View Employee Profile</a>
                </div>
              </div>
              <div class="footer">NexaWorks AI • Automated Engagement Monitor</div>
            </div>
            </body></html>
            """.formatted(employee.getName(), employee.getDepartment(),
                          employee.getEngagementScore(), employee.getId());

        sendAlertToHRTeam("[NexaWorks] Low Engagement Detected – " + employee.getName(), html);
    }
}

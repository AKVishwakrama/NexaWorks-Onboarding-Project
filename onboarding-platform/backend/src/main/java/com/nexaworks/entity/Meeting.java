package com.nexaworks.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Meeting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    private String meetingType;   // ONE_ON_ONE | TEAM | TRAINING | ORIENTATION

    private String organizer;     // name of organiser

    private Long organizerId;

    @Column(nullable = false)
    private Long participantId;   // primary participant (employee)

    private String meetLink;

    private String status;        // SCHEDULED | COMPLETED | CANCELLED

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}

package com.nexaworks.repository;

import com.nexaworks.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByParticipantIdOrderByScheduledAtAsc(Long participantId);
    List<Meeting> findByOrganizerIdOrderByScheduledAtAsc(Long organizerId);
    List<Meeting> findByStatusOrderByScheduledAtAsc(String status);
}

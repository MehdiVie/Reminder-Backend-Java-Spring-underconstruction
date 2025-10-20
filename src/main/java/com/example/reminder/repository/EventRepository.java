package com.example.reminder.repository;

import com.example.reminder.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // return all Events after specific date
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :date")
    Page<Event> findAllAfterDate(LocalDate date , Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.reminderSent = false AND e.reminderTime <= :now ")
    List<Event> findPendingReminders(@Param("now") LocalDateTime now);
}

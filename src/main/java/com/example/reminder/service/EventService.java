package com.example.reminder.service;

import com.example.reminder.model.Event;
import com.example.reminder.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class EventService {
    private final EventRepository repo;

    // Allowed sort fields (white list)
    private static final Set<String> ALLOWED_SORTS = Set.of("id", "eventDate", "title", "reminderTime");


    public EventService(EventRepository repository) {
        this.repo = repository;
    }

    public Page<Event> getPagedEvents(Integer page, Integer size, String sortBy,
                                      String direction, LocalDate afterDate) {
        // defaults
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 0 || size > 100) ? 10 : size;

        // safe direction
        Sort.Direction dir;
        try {
            dir = (direction == null) ? Sort.Direction.ASC : Sort.Direction.valueOf(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            dir = Sort.Direction.ASC;
        }

        //safe sortBy
        String sortProb = (sortBy == null || !ALLOWED_SORTS.contains(sortBy)) ? "id" : sortBy;

        // stable sort
        Sort sort = Sort.by(new Sort.Order(dir, sortProb) , new Sort.Order(dir , "id"));

        Pageable pageable = PageRequest.of(p,s,sort);

        if (afterDate != null) {
            return repo.findAllAfterDate(afterDate, pageable);
        }



        return repo.findAll(pageable);

    }

    public Event getEventById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Event> getAllEvents() {
        return repo.findAll();
    }

    public Event createEvent(Event event) {
        return repo.save(event);
    }

    public Event updateEvent(Long id , Event updatedEvent) {
        Event event = this.getEventById(id);

        if (event == null) return null;

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setEventDate(updatedEvent.getEventDate());
        event.setReminderTime(updatedEvent.getReminderTime());
        return repo.save(event);
    }

    public void deleteEvent(Long id) {
        repo.deleteById(id);
    }
}

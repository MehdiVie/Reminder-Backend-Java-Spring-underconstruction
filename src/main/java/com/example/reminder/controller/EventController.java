package com.example.reminder.controller;

import com.example.reminder.dto.ApiResponse;
import com.example.reminder.dto.PageResponse;
import com.example.reminder.model.Event;
import com.example.reminder.exception.ResourceNotFoundException;
import com.example.reminder.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200") // Allow all origins (for Angular frontend)
public class EventController {
    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    /*
    Get /api/events/paged
    support pagination, sorting and optional date filter
    Example
    /api/events/paged?page=0&size=5&sortBy=is&direction=asc&afterDate=2025-10-18
    * */
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Object>> getPagedEvents (
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String afterDate) {

        LocalDate dateFilter = null;
        if (afterDate != null && !afterDate.isEmpty()) {
            dateFilter = LocalDate.parse(afterDate);
        }

        var pageResult= service.getPagedEvents(page,size,sortBy,direction,dateFilter);

        log.info("Get /api/events/paged -> page={} , size={} , sortBy={} , direction={} , afterDate={} " ,
                page , size , sortBy , direction , afterDate
                );

        PageResponse<Event> responseData = new PageResponse<>();
        responseData.setContent(pageResult.getContent());
        responseData.setCurrentPage( pageResult.getNumber());
        responseData.setTotalItems( pageResult.getTotalElements());
        responseData.setTotalPages( pageResult.getTotalPages());

        return  ResponseEntity.ok(new ApiResponse<>("success", "Paged Events retrieved" , responseData));
    }

    /**
     * GET /api/events
     * Retrieve all events from the database.
     * Returns 200 OK if data exists, If list is empty, data = [] and a friendly message.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getAll() {
        List<Event> events = service.getAllEvents();
        log.info("Get /api/evets -> {} items", events.size());
        String message = (events.isEmpty()) ? "No Events found." : "Events retrieves successfully.";
        return ResponseEntity.ok(new ApiResponse<>("success", message, events));
    }


    /**
     * GET /api/events/{id}
     * Retrieve single event from the database.
     * On missing record -> throw ResourceNotFoundException (handled globally)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getById(@PathVariable Long id) {
        Event event = service.getEventById(id);
        if (event == null) {
            log.warn("Get /api/events/{} -> not found", id);
            throw new ResourceNotFoundException("Event with ID : "+ id +" not found.");
        }
        log.info("Get /api/events/{} -> OK ", id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Event retrieved successfully", event));
    }

    /**
     * POST /api/events
     * Creates a new Event
     * Return 201 after create
     * Validation Errors handled globally
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Event>> create(@RequestBody @Valid Event event) {
        Event createdEvent = service.createEvent(event);
        log.info("Post /api/events -> created id={} , title={}", createdEvent.getId(), createdEvent.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).
                body(new ApiResponse<>("success", "Event Created",createdEvent)); // 201 Created
    }

    /**
     * PUT /api/events/{id}
     * Update an Event
     * If entity missing -> 404 via exception.
     * Validation Errors handled globally
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> update(@PathVariable Long id , @RequestBody @Valid Event event) {
        Event existingEvent = service.getEventById(id);

        if (existingEvent == null) {
            log.warn("Get /api/events/{} -> not found", id);
            throw new ResourceNotFoundException("Event with ID : "+ id +" not found.");
        }
        Event updatedEvent = service.updateEvent(id, event);
        log.info("Put /api/events/{} -> updated", id);
        return ResponseEntity.ok(new ApiResponse<>("success" , "Event Updated.",updatedEvent ));

    }

    /**
     * DELETE /api/events/{id}
     * If entity missing -> 404 via exception.
     * We return 200 with a success message (envelope) for consistency.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Event existingEvent = service.getEventById(id);

        if (existingEvent == null) {
            log.warn("Get /api/events/{} -> not found", id);
            throw new ResourceNotFoundException("Event with ID : "+ id +" not found.");
        }

        service.deleteEvent(id);
        log.info("Delete /api/events/{} -> deleted", id);
        return ResponseEntity.ok(new ApiResponse<>("success","Event Deleted.",null));
    }

}

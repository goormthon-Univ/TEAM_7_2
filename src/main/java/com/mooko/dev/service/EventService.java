package com.mooko.dev.service;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event findEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Transactional
    public Event makeNewEvent(NewEventDto newEventDto, User user) {
        Event event = Event.builder()
                .title(newEventDto.getTitle())
                .activeStatus(true)
                .roomMaker(user)
                .createdAt(LocalDateTime.now())
                .build();

        return eventRepository.save(event);
    }

    @Transactional
    public void updateEventName(String eventName, Event event) {
        event.updateEventName(eventName);
        eventRepository.save(event);
    }

    @Transactional
    public void updateEventDate(UpdateEventDateDto updateEventDateDto, Event event) {
        event.updateEventDate(updateEventDateDto.getStartDate(), updateEventDateDto.getEndDate());
        eventRepository.save(event);
    }

    @Transactional
    public void updateEventStatus(Event event, boolean status) {
        event.updateEventStatus(status);
    }

    @Transactional
    public void addUser(User user, Event event) {
        event.getUsers().add(user);
        eventRepository.save(event);
    }
}

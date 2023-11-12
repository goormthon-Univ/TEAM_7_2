package com.mooko.dev.service;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.NewEventDto;
import com.mooko.dev.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

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
}

package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.event.req.NewEventDto;
import com.mooko.dev.dto.event.req.UpdateEventDateDto;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event findEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Transactional
    public Event makeNewEvent(String title, String startDate, String endDate) {
        Event event = Event.builder()
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
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
    public void updateEventDate(Event event, String startDate, String endDate) {
        event.updateEventDate(startDate, endDate);
        eventRepository.save(event);
    }

//    @Transactional
//    public void updateEventStatus(Event event, boolean status) {
//        event.updateEventStatus(status);
//    }

    @Transactional
    public void addEventUser(User user, Event event) {
        event.getUsers().add(user);
        eventRepository.save(event);
    }

    @Transactional
    public void addBarcode(Event event, Barcode barcode) {
        event.updateBarcode(barcode);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEventUser(User user, Event event) {
        if (event.getUsers().removeIf(u -> u.equals(user))) {
            eventRepository.save(event);
        } else {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }
    }

    @Transactional
    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public Event findEventByBarcode(Barcode barcode){
        return eventRepository.findByBarcode(barcode);
    }

    public List<Event> findEventByRoomaker(User user){
        return eventRepository.findByRoomMaker(user);
    }

    @Transactional
    public void addEventPhoto(Event event, List<EventPhoto> eventPhotos){
        event.updateEventPhoto(eventPhotos);

    }

}

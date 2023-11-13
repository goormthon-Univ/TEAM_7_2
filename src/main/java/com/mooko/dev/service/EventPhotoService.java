package com.mooko.dev.service;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import com.mooko.dev.repository.EventPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPhotoService {
    private final EventPhotoRepository eventPhotoRepository;

    public List<String> findUserEventPhotoList(User user, Event event) {
        List<EventPhoto> eventPhotoList = eventPhotoRepository.findByUserAndEvent(user, event);
        return eventPhotoList.stream().map(EventPhoto::getUrl).toList();
    }

    public List<String> findAllEventPhotoList(Event event) {
        List<EventPhoto> eventPhotos = eventPhotoRepository.findByEvent(event);
        return eventPhotos.stream().map(EventPhoto::getUrl).toList();
    }
}

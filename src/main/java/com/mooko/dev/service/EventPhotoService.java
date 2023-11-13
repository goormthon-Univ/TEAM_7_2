package com.mooko.dev.service;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import com.mooko.dev.repository.EventPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPhotoService {
    private final EventPhotoRepository eventPhotoRepository;

    public List<EventPhoto> findUserEventPhotoList(User user, Event event) {
        return eventPhotoRepository.findByUserAndEvent(user, event);
    }

    public List<String> findAllEventPhotoList(Event event) {
        List<EventPhoto> eventPhotos = eventPhotoRepository.findByEvent(event);
        return eventPhotos.stream().map(EventPhoto::getUrl).toList();
    }


    @Transactional
    public void deleteEventPhoto(List<EventPhoto> eventPhotoList) {
        eventPhotoRepository.deleteAll(eventPhotoList);
    }

    @Transactional
    public List<EventPhoto> makeNewEventPhoto(User user, Event event, List<String> eventPhotoUrlList) {
        List<EventPhoto> eventPhotos = new ArrayList<>();

        for (String s : eventPhotoUrlList) {
            EventPhoto eventPhoto = EventPhoto.builder()
                    .user(user)
                    .event(event)
                    .createdAt(LocalDateTime.now())
                    .url(s)
                    .build();
            eventPhotos.add(eventPhotoRepository.save(eventPhoto));
        }

        return eventPhotos;
    }


}

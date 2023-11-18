package com.mooko.dev.repository;

import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
//    List<EventPhoto> findByUserAndEvent(User user, Event event);

    List<EventPhoto> findByEvent(Event event);

    void deleteByEvent(Event event);
}

package com.mooko.dev.repository;

import com.mooko.dev.domain.EventPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
}

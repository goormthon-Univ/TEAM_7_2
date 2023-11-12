package com.mooko.dev.repository;

import com.mooko.dev.domain.EventPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
}

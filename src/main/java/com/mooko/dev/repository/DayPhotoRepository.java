package com.mooko.dev.repository;

import com.mooko.dev.domain.DayPhoto;
import com.mooko.dev.service.DayPhotoService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayPhotoRepository extends JpaRepository<DayPhoto, Long> {
}

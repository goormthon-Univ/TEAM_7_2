package com.mooko.dev.repository;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.DayPhoto;
import com.mooko.dev.service.DayPhotoService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface DayPhotoRepository extends JpaRepository<DayPhoto, Long> {

    DayPhoto findByDayAndThumbnailTrue(Day day);

    List<DayPhoto> findByDay(Day day);
}

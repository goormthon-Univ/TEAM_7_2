package com.mooko.dev.repository;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.EventPhoto;
import com.mooko.dev.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface DayRepository extends JpaRepository<Day, Long> {
    Day findByUserAndYearAndMonthAndDay(User user, int year, int month, int day);

    List<Day> findByUserAndYearAndMonth (User user, int year, int month);
}

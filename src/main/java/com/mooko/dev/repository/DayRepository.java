package com.mooko.dev.repository;

import com.mooko.dev.domain.Day;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayRepository extends JpaRepository<Day, Long> {
}

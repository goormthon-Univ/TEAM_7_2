package com.mooko.dev.repository;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface EventRepository extends JpaRepository<Event, Long> {

    Event findByBarcode(Barcode barcode);
}

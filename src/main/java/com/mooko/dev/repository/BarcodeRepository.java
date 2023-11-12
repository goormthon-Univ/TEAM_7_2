package com.mooko.dev.repository;

import com.mooko.dev.domain.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarcodeRepository extends JpaRepository<Barcode, Long> {
}

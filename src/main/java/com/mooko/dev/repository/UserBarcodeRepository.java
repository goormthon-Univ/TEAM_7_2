package com.mooko.dev.repository;

import com.mooko.dev.domain.UserBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBarcodeRepository extends JpaRepository<UserBarcode, Long> {
}

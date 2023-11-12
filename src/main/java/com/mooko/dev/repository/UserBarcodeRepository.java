package com.mooko.dev.repository;

import com.mooko.dev.domain.UserBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserBarcodeRepository extends JpaRepository<UserBarcode, Long> {
}

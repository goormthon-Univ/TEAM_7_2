package com.mooko.dev.repository;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarcodeRepository extends JpaRepository<Barcode, Long> {
    List<Barcode> findByIdAndTitle(Long id, String title);
}

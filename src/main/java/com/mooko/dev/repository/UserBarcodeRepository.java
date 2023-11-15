package com.mooko.dev.repository;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.User;
import com.mooko.dev.domain.UserBarcode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserBarcodeRepository extends JpaRepository<UserBarcode, Long> {
    List<UserBarcode> findByUser(User user);

    UserBarcode findByBarcode(Barcode barcode);
}

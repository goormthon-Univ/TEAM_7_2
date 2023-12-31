package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.User;
import com.mooko.dev.domain.UserBarcode;
import com.mooko.dev.repository.UserBarcodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBarcodeService {

    private final UserBarcodeRepository userBarcodeRepository;

    @Transactional
    public void makeUserBarcode(User user, Barcode barcode) {
        UserBarcode userBarcode = UserBarcode.builder()
                .user(user)
                .barcode(barcode)
                .build();
        userBarcodeRepository.save(userBarcode);
    }

    @Transactional
    public void makeUserDayBarcode(User user, Barcode barcode) {
        UserBarcode userBarcode = UserBarcode.builder()
                .user(user)
                .barcode(barcode)
                .build();
        userBarcodeRepository.save(userBarcode);
    }

    public List<UserBarcode> findUserBarcodeList(User user){
        List<UserBarcode> userBarcodeList = userBarcodeRepository.findByUser(user);
        return userBarcodeList;
    }

    @Transactional
    public void deleteUserBarcode(UserBarcode userBarcode){
        userBarcodeRepository.delete(userBarcode);
    }

    public List<UserBarcode> findUserBarcodeByBarcode(Barcode barcode){
        List<UserBarcode> userBarcode = userBarcodeRepository.findByBarcode(barcode);
        return userBarcode;
    }
}

package com.mooko.dev.service;

import com.mooko.dev.domain.User;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}

package com.mooko.dev.service;

import com.mooko.dev.domain.Day;
import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

//    @Transactional
//    public void addEvent(User user, Event event) {
//        user.updateEvent(event);
//        userRepository.save(user);
//    }

//    @Transactional
//    public void updateCheckStatus(User user, boolean checkStatus) {
//        user.updateCheckStatus(checkStatus);
//        userRepository.save(user);
//    }

    @Transactional
    public User save(User user){
        return userRepository.save(user);
    }

//    @Transactional
//    public void deleteEvent(User user) {
//        user.updateEvent(null);
//        userRepository.save(user);
//    }

    @Transactional
    public void updateUserInfo(User user, String newProfileImgUrl, String nickname, String birth, String gender, boolean modalActive) {
        user.updateUserInfo(newProfileImgUrl,nickname, birth,
                gender, modalActive);
        userRepository.save(user);
    }

    public List<User> findUserByEvent(Event event) {
        return userRepository.findByEvent(event);
    }
}

package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.user.req.UserNewInfoDto;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.dto.user.res.UserPassportDto;
import com.mooko.dev.facade.AggregationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    private final AggregationFacade aggregationFacade;

    //3. 나의 이벤트
    @GetMapping("/my-event")
    public ResponseEntity<UserEventStatusDto> showUserEventStatus(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = principalDetails.getUser();
        UserEventStatusDto userEventStatusDto = aggregationFacade.showUserEventStatus(user);
        return ResponseEntity.ok(userEventStatusDto);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<Void> reIssueRefreshToken() {
        log.info("refreshToken 발급 완료");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/user-info")
    public ResponseEntity<UserPassportDto> showUserInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails){
        User user = principalDetails.getUser();
        UserPassportDto userPassportDto = aggregationFacade.showUserInfo(user);
        return ResponseEntity.ok(userPassportDto);
    }

    @PostMapping("/user-info")
    public ResponseEntity<Void> updateUserInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute UserNewInfoDto userNewInfoDto){
        User user = principalDetails.getUser();
        aggregationFacade.updateUserInfo(user, userNewInfoDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

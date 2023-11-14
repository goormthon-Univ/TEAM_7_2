package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.user.res.UserEventStatusDto;
import com.mooko.dev.facade.AggregationFacade;
import com.mooko.dev.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final AggregationFacade aggregationFacade;


    @GetMapping("/my-event")
    public ResponseEntity<UserEventStatusDto> showUserEventStatus(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = principalDetails.getUser();
        UserEventStatusDto userEventStatusDto = aggregationFacade.showUserEventStatus(user);
        return ResponseEntity.ok(userEventStatusDto);
    }
}

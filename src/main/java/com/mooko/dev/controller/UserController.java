package com.mooko.dev.controller;

import static java.awt.SystemColor.info;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    private final AggregationFacade aggregationFacade;
    /**
     * Test용
     */
    private final GrantedAuthoritiesMapper authoritiesMapper;


    //3. 나의 이벤트
//    @GetMapping("/my-event")
//    public ResponseEntity<UserEventStatusDto> showUserEventStatus(@AuthenticationPrincipal PrincipalDetails principalDetails) {
//        User user = principalDetails.getUser();
//
//        UserEventStatusDto userEventStatusDto = aggregationFacade.showUserEventStatus(user);
//        return ResponseEntity.ok(userEventStatusDto);
//    }

    @GetMapping("/refreshToken")
    public ResponseEntity<Void> reIssueRefreshToken() {
        log.info("refreshToken 발급 완료");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/for-test")
    public void test(){
        Date nowDate = new Date();

        String accessToken1 = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(nowDate.getTime() + 604800000))
                .withClaim("socialId", "1111111111")
                .sign(Algorithm.HMAC512("asdfasdfasdfasdrtqwetqpkna;pilugipk1j23k4lhj1adfkj9312kl3n@*!!~asdkflj9@#Nvlny.asdf"));

        String accessToken2 = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(nowDate.getTime() + 604800000))
                .withClaim("socialId", "2222222222")
                .sign(Algorithm.HMAC512("asdfasdfasdfasdrtqwetqpkna;pilugipk1j23k4lhj1adfkj9312kl3n@*!!~asdkflj9@#Nvlny.asdf"));

        log.info("accessToken1 = {}", accessToken1);
        log.info("accessToken2 = {}", accessToken2);

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

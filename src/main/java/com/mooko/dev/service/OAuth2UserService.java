package com.mooko.dev.service;
import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.oauth.CustomOAuth2user;
import com.mooko.dev.oauth.KakaoUserInfo;
import com.mooko.dev.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {


    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);

        User user = getUser(kakaoUserInfo);

        return new CustomOAuth2user(
                Collections.singleton(new SimpleGrantedAuthority(null)),
                attributes,
                userNameAttributeName,
                kakaoUserInfo.getId()
        );
    }

    private User getUser(KakaoUserInfo kakaoUserInfo) {

        Optional<User> bySocialId = userRepository.findBySocialId(kakaoUserInfo.getId());
        if(bySocialId.isPresent()){
            return bySocialId.get();
        }

        User user = User.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .socialId(kakaoUserInfo.getId())
                .role(Role.GUEST)
                .build();

        userService.save(user);
        return user;
    }


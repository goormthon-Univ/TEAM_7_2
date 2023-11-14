package com.mooko.dev.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class KakaoUserInfo {

    private final Map<String, Object> attributes;


    public String getId(){
        return String.valueOf(attributes.get("id"));
    }

    public String getName(){
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        if (account == null || profile == null) {
            return null;
        }
        return (String) profile.get("nickname");
    }
}

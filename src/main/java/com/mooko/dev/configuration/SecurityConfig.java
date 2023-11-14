package com.mooko.dev.configuration;

import com.mooko.dev.handler.OAuth2LoginFailureHandler;
import com.mooko.dev.handler.OAuth2LoginSuccessHandler;
import com.mooko.dev.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    public SecurityConfig(OAuth2UserService oAuth2UserService, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(config -> config
                        .requestMatchers("/oauth2/authorization/kakao", "/login/oauth2/code/kakao").permitAll()
                        .anyRequest().authenticated()
                );

        http
                .oauth2Login(config -> config
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService))
                );

/*        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(config -> config.anyRequest().permitAll());
        http.oauth2Login(oauth2Configurer -> oauth2Configurer
                .loginPage("/oauth/authorization/kakao")
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService)));

        return http.build();*/
    }
}

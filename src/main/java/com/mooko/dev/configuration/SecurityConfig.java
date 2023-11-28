package com.mooko.dev.configuration;

import com.mooko.dev.handler.OAuth2LoginFailureHandler;
import com.mooko.dev.handler.OAuth2LoginSuccessHandler;
import com.mooko.dev.oauth.CustomAuthenticationEntryPoint;
import com.mooko.dev.security.JwtFilter;
import com.mooko.dev.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CorsFilter corsFilter;
    private final JwtFilter jwtFilter;

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
                        .requestMatchers("/ws-check/**", "/ws-button/**", "/ws-leave-event/**").permitAll()
                        .requestMatchers("/api/v1/user/for-test").permitAll()
                        .requestMatchers("/api/v1/barcode/{barcodeId}").permitAll()
                        .requestMatchers("/api/v1/event/image-list/**").permitAll()
                        .requestMatchers("/api/v1/event/save-photo/**").permitAll()
                        .anyRequest().authenticated()
                );

        http
                .oauth2Login(config -> config
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService))
                );

        http
                .addFilterBefore(jwtFilter, LogoutFilter.class);

        http
                .exceptionHandling(config -> config.authenticationEntryPoint(customAuthenticationEntryPoint));

        http
                .addFilter(corsFilter);

        return http.build();
    }
}

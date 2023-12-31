package com.mooko.dev.oauth;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooko.dev.exception.custom.ErrorCode;
import com.mooko.dev.handler.OAuth2LoginFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.info("request.getRequestURI() = {} ", request.getRequestURI());
        log.info("authException = {}", authException.getClass().toString());


        OAuth2LoginFailureHandler.ErrorResponse errorResponse = new OAuth2LoginFailureHandler.ErrorResponse(ErrorCode.UNAUTHORIZED_USER.getCode());
        String json = new ObjectMapper().writeValueAsString(errorResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
        response.sendRedirect("/");


        log.error("error = {}", (Object) authException.getStackTrace());
    }

}


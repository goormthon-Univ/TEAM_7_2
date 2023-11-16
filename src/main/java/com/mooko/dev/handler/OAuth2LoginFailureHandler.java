package com.mooko.dev.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooko.dev.exception.custom.ErrorCode;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("로그인에 실패했습니다.");


        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNAUTHORIZED_USER.getCode());
        String json = new ObjectMapper().writeValueAsString(errorResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

        //401
            /*{
               code : UNAUTHORIZED USER
            }*/
    }

    public static class ErrorResponse {
        private String code;

        public ErrorResponse(String code) {
            this.code = code;
        }
    }

}
package com.mooko.dev.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400
    USER_ALREADY_HAS_EVENT(HttpStatus.BAD_REQUEST, "USER ALREADY HAS EVENT"),

    //401

    //404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER NOT FOUND"),

    //500

    ;


    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}

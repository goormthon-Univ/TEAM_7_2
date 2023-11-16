package com.mooko.dev.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400
    USER_ALREADY_HAS_EVENT(HttpStatus.BAD_REQUEST, "USER ALREADY HAS EVENT"),
    NOT_ROOM_MAKER(HttpStatus.BAD_REQUEST, "NOT ROOM MAKER"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID REQUEST"),
    EVENT_PHOTO_EXCEED(HttpStatus.BAD_REQUEST, "PHOTO EXCEED"),
    EVENT_PHOTO_IS_LESS_THAN(HttpStatus.BAD_REQUEST, "EVENT PHOTO IS LESS THAN"),
    USER_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER NOT MATCH"),
    EVENT_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "EVENT_TITLE_EMPTY"),

    DAY_PHOTO_EXCEED(HttpStatus.BAD_REQUEST, "DAY_PHOTO_EXCEED"),
    DAY_PHOTO_IS_LESS_THAN(HttpStatus.BAD_REQUEST, "DAY_PHOTO_IS_LESS_THAN"),

    NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "NICKNAME_EMPTY"),

    NOT_OWNER_ACCESS(HttpStatus.BAD_REQUEST, "NOT_OWNER_ACCESS"),

    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED"),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "TOKEN_INVALID"),
    AUTHENTICATION_REQUIRED(HttpStatus.BAD_REQUEST, "AUTHENTICATION_REQUIRED"),

    //401
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED USER"),
    //404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER NOT FOUND"),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT NOT FOUND"),
    DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "DAY NOT FOUND"),
    BARCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "BARCODE NOT FOUND"),

    //500
    S3_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_ERROR"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"),
    ;


    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}

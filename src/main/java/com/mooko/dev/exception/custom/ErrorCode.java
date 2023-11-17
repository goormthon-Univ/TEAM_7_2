package com.mooko.dev.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400
    USER_ALREADY_HAS_EVENT(HttpStatus.BAD_REQUEST, "USER_ALREADY_HAS_EVENT"),
    NOT_ROOM_MAKER(HttpStatus.BAD_REQUEST, "NOT_ROOM_MAKER"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    EVENT_PHOTO_EXCEED(HttpStatus.BAD_REQUEST, "PHOTO_EXCEED"),
    EVENT_PHOTO_IS_LESS_THAN(HttpStatus.BAD_REQUEST, "EVENT_PHOTO_IS_LESS_THAN"),
    USER_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER_NOT_MATCH"),
    EVENT_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "EVENT_TITLE_EMPTY"),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_ALREADY_DELETED"),
    START_DATE_EXCEED_END_DATE(HttpStatus.BAD_REQUEST, "START_DATE_EXCEED_END_DATE"),

    DAY_PHOTO_EXCEED(HttpStatus.BAD_REQUEST, "DAY_PHOTO_EXCEED"),
    DAY_PHOTO_IS_LESS_THAN(HttpStatus.BAD_REQUEST, "DAY_PHOTO_IS_LESS_THAN"),

    NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "NICKNAME_EMPTY"),

    NOT_OWNER_ACCESS(HttpStatus.BAD_REQUEST, "NOT_OWNER_ACCESS"),

    //401
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_USER"),

    //404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND"),
    DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "DAY_NOT_FOUND"),
    BARCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "BARCODE_NOT_FOUND"),

    //500
    S3_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_ERROR"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    ;


    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}

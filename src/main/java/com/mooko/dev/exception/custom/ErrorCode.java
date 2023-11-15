package com.mooko.dev.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400
    USER_ALREADY_HAS_EVENT(HttpStatus.BAD_REQUEST, "USER ALREADY HAS EVENT"),
    NOT_ROOM_MAKER(HttpStatus.BAD_REQUEST, "NOT ROOM MAKER"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID REQUEST"),
    EVENT_PHOTO_EXCEED(HttpStatus.BAD_REQUEST, "EVENT PHOTO EXCEED"),
    EVENT_PHOTO_IS_LESS_THAN(HttpStatus.BAD_REQUEST, "EVENT PHOTO IS LESS THAN"),
    USER_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER NOT MATCH"),
    PHOTO_FOR_BARCODE_LESS_THEN(HttpStatus.BAD_REQUEST, "PHOTO_FOR_BARCODE_LESS_THEN"),
    PHOTO_FOR_BARCODE_EXCEED(HttpStatus.BAD_REQUEST, "PHOTO_FOR_BARCODE_EXCEED"),

    //401

    //404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER NOT FOUND"),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT NOT FOUND"),

    //500
    S3_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_ERROR")
    ;


    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}

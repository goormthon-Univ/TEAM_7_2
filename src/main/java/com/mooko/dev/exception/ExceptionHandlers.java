package com.mooko.dev.exception;


import com.mooko.dev.dto.exception.ErrorDto;
import com.mooko.dev.exception.custom.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ErrorDto> handleCustomException(CustomException e) {
        log.error("error = {}", e.getErrorCode().getCode());
        ErrorDto errorDto = ErrorDto.builder().code(e.getErrorCode().getCode()).build();
        return new ResponseEntity<>(errorDto, e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException e){
        log.error("error = {}", e.getStatusCode());
        ErrorDto errorDto = ErrorDto.builder().code(e.getMessage()).build();
        return new ResponseEntity<>(errorDto,e.getStatusCode());
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDto> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("error = {}", e.getStatusCode());
        ErrorDto errorDto = ErrorDto.builder().code(e.getMessage()).build();
        return new ResponseEntity<>(errorDto, e.getStatusCode());
    }
}

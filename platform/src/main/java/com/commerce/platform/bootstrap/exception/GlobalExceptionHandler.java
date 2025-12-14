package com.commerce.platform.bootstrap.exception;

import com.commerce.shared.exception.BusinessException;
import com.commerce.platform.exception.LockUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.commerce.shared.exception.BusinessError.INVALID_REQUEST_VALUE;
import static com.commerce.shared.exception.BusinessError.UNKNOWN_ERROR;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    // todo 오류관리에 필요한 정보와 후처리

    /**
     * 비즈니스 오류 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            HttpServletRequest request,
            BusinessException de) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        request.getRequestURI(),
                        request.getMethod(),
                        de.getCode(),
                        de.getMessage()));
    }

    /**
     * Valid 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValidException(
            HttpServletRequest request,
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        request.getRequestURI(),
                        request.getMethod(),
                        INVALID_REQUEST_VALUE.getCode(),
                        errors.toString()));
    }

    /**
     * 비즈니스 오류 처리
     */
    @ExceptionHandler(LockUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            HttpServletRequest request,
            LockUnavailableException le) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        request.getRequestURI(),
                        request.getMethod(),
                        "1111",
                        "잠시후 다시 시도해주세"));
    }

    /**
     * 그 외 오류처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            HttpServletRequest request,
            RuntimeException re
    ) {
        log.error(re.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        request.getRequestURI(),
                        request.getMethod(),
                        UNKNOWN_ERROR.getCode(),
                        UNKNOWN_ERROR.getMessage()
                ));
    }

}
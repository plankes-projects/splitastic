package com.epicnerf.api;

import com.epicnerf.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
class GlobalDefaultExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Void> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        if (e instanceof NoResultException) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (e instanceof AuthenticationException) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        throw e;
    }
}
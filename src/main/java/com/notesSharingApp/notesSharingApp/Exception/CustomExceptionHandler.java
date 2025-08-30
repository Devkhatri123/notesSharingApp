package com.notesSharingApp.notesSharingApp.Exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {
    Map<String,Object> response = new HashMap<>();
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> expiredJwtExceptionHandler(ExpiredJwtException expiredJwtException){
        response.put("message","Token is expired");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> malFormedJwtException(MalformedJwtException malformedJwtException){
        Map<String,Object> response = new HashMap<>();
        response.put("message","Token has been changed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> HttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException){
        response.put("error",httpMediaTypeNotSupportedException.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<?> HttpMediaTypeNotSupportedException(HttpClientErrorException.Forbidden Forbidden){
        return ResponseEntity.internalServerError().body(Forbidden.getMessage());
    }
}

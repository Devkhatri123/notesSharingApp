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
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> expiredJwtExceptionHandler(ExpiredJwtException expiredJwtException){
        Map<String,Object> response = new HashMap<>();
        response.put("message","Token is expired");
        response.put("status",404);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> malFormedJwtException(MalformedJwtException malformedJwtException){
        Map<String,Object> response = new HashMap<>();
        response.put("message","Token has been tampered");
        response.put("status",404);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> HttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException){
        Map<String,Object> response = new HashMap<>();
        response.put("error",httpMediaTypeNotSupportedException.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<?> HttpMediaTypeNotSupportedException(HttpClientErrorException.Forbidden Forbidden){
        Map<String,Object> response = new HashMap<>();
        response.put("error",Forbidden.getMessage());
        System.out.println(Forbidden.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}

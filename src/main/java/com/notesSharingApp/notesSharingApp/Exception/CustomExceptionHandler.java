package com.notesSharingApp.notesSharingApp.Exception;

import com.notesSharingApp.notesSharingApp.DTO.jsonResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> expiredJwtExceptionHandler(ExpiredJwtException expiredJwtException){
        jsonResponse response = new jsonResponse();
        response.setMessage("Jwt is expired");
        response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> malFormedJwtException(MalformedJwtException malformedJwtException){
        jsonResponse response = new jsonResponse();
        response.setMessage("Jwt token is changed");
        response.setHttpStatusCode(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response,response.getHttpStatusCode());
    }
}

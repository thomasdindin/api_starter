package fr.thomasdindin.api_starter.controllers;

import fr.thomasdindin.api_starter.authentication.errors.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<Void> handleAccountBlocked(AccountBlockedException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @ExceptionHandler(EmailNotVerfiedException.class)
    public ResponseEntity<Void> handleEmailNotVerified(EmailNotVerfiedException e) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> handleAuthException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(NoMatchException.class)
    public ResponseEntity<Void> handleNoMatchException(NoMatchException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Void> handleEmailAlreadyUsed(EmailAlreadyUsedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

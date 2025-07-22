package ua.sunbeam.genericstore.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        // Log the exception for server-side monitoring
        System.err.println("HttpMessageNotReadableException: " + ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        // Provide a user-friendly message for the client
        errors.put("error", "Request body is missing or malformed JSON.");
        // Optionally, include more details for debugging purposes (be careful with sensitive info in production)
        errors.put("details", ex.getLocalizedMessage());

        // Return a 400 Bad Request status
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex) {
        System.err.println("An unexpected error occurred: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "An unexpected server error occurred.");
        errors.put("details", ex.getLocalizedMessage());
        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

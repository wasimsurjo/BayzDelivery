package com.bayzdelivery.exceptions;

import java.util.AbstractMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

@ControllerAdvice
@Component
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<AbstractMap.SimpleEntry<String, String>> handleResponseStatusException(ResponseStatusException exception) {
    log.warn("Request validation failed: {}", exception.getMessage());
    AbstractMap.SimpleEntry<String, String> response =
        new AbstractMap.SimpleEntry<>("message", exception.getReason());
    return ResponseEntity.status(exception.getStatusCode()).body(response);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<AbstractMap.SimpleEntry<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
    log.warn("Database constraint violation: {}", exception.getMessage());
    String message = "Request could not be processed due to data validation error";
    
    if (exception.getMessage().contains("uk_delivery_order_id")) {
      message = "An order with this ID already exists";
    } else if (exception.getMessage().contains("uk_person_email")) {
      message = "A person with this email already exists";
    } else if (exception.getMessage().contains("uk_person_registration_number")) {
      message = "A delivery person with this registration number already exists";
    } else if (exception.getMessage().contains("uk_delivery_concurrent")) {
      message = "Delivery person is already assigned to another delivery during this time";
    }
    
    AbstractMap.SimpleEntry<String, String> response = new AbstractMap.SimpleEntry<>("message", message);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler
  public ResponseEntity<AbstractMap.SimpleEntry<String, String>> handle(Exception exception) {
    log.error("Unexpected error occurred: ", exception);
    AbstractMap.SimpleEntry<String, String> response =
        new AbstractMap.SimpleEntry<>("message", "Request could not be processed");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}

package com.sha5.ticketpigeon.common.exception;

import com.sha5.ticketpigeon.common.dto.ErrorResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class) 
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        String message = ex.getMessage();
        
        Throwable root = ex.getMostSpecificCause();
        if (root != null) {
            String error = root.getMessage();
            if (error.contains("uk_seat_screen_row_number")) {
                String seatLabel = extractSeatLabel(error);
                message = "Seat" + (seatLabel != null ? " " + seatLabel : "") + " already exists on this screen";
            } else if (error.contains("uk_theater_name_city_address")) {
                message = "Theater already exists";
            } else if (error.contains("uk_screen_theater_name")) {
                message = "Screen already exists in this theater";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String extractSeatLabel(String message) {
        Pattern pattern = Pattern.compile(
            "Key \\(screen_id, row_id, seat_number\\)=\\([^,]+,\\s*([^,]+),\\s*(\\d+)\\)"
        );
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String rowId = matcher.group(1);
            String seatNumber = matcher.group(2);
            return rowId + seatNumber;
        }
        return null;
    }
}

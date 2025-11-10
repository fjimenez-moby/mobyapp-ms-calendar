package com.login.calendar.exception.handler;

import com.login.calendar.exception.InvalidAuthHeaderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.IOException;
import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        logger.severe("Error IO en Calendar: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear evento: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        logger.severe("Error inesperado: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado");
    }

    @ExceptionHandler(InvalidAuthHeaderException.class)
    public ResponseEntity<Object> handleInvalidAuthHeader(InvalidAuthHeaderException ex) {
        logger.warning("UNAUTHORIZED attempt: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(ex.getMessage()));
    }

    private Object createErrorResponse(String msg) {
        return new Object() {
            public final boolean success = false;
            public final String message = msg;
            public final long timestamp = System.currentTimeMillis();
        };
    }


}

package com.veriprotocol.springAI.core;

import java.net.ConnectException;
import java.sql.SQLTransientConnectionException;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handle(Exception e) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(e);

        if (isDbDown(root)) {
            return new ErrorResponse(
                    "DB_UNAVAILABLE",
                    "Database is unavailable. Please retry."
            );
        }

        return new ErrorResponse(
                "INTERNAL_ERROR",
                "Internal server error"
        );
    }

    private boolean isDbDown(Throwable t) {
        if (t == null) return false;

        if (t instanceof ConnectException) return true;
        if (t instanceof SQLTransientConnectionException) return true;

        String msg = t.getMessage();
        return msg != null && (
                msg.contains("Connection refused") ||
                msg.contains("Failed to obtain JDBC Connection") ||
                msg.contains("Connection is not available")
        );
    }

    public record ErrorResponse(String code, String message) {}
}

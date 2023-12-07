package ru.nechaev.pasteshare.exception.access;

import org.springframework.http.HttpStatus;
import ru.nechaev.pasteshare.exception.ApplicationException;

public class PermissionDeniedEntityAccessException extends ApplicationException {
    public PermissionDeniedEntityAccessException(String errorMessage) {
        super(HttpStatus.FORBIDDEN, errorMessage);
    }
}

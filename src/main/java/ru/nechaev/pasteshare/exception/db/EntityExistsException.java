package ru.nechaev.pasteshare.exception.db;

import org.springframework.http.HttpStatus;
import ru.nechaev.pasteshare.exception.ApplicationException;

public class EntityExistsException extends ApplicationException {
    public EntityExistsException(String errorMessage) {
        super(HttpStatus.CONFLICT, errorMessage);
    }
}

package ru.nechaev.pasteshare.exception.db;

import org.springframework.http.HttpStatus;
import ru.nechaev.pasteshare.exception.ApplicationException;

public class EntityNotFoundException extends ApplicationException {
    public EntityNotFoundException(String errorMessage) {
        super(HttpStatus.NOT_FOUND, errorMessage);
    }
}

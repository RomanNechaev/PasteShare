package ru.nechaev.pasteshare.exception.db;

import org.springframework.http.HttpStatus;
import ru.nechaev.pasteshare.exception.ApplicationException;

public class S3Exception extends ApplicationException {
    public S3Exception(String errorMessage) {
        super(HttpStatus.BAD_GATEWAY, errorMessage);
    }
}

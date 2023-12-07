package ru.nechaev.pasteshare.exception.security;

import org.springframework.http.HttpStatus;
import ru.nechaev.pasteshare.exception.ApplicationException;

public class TokenConvertException extends ApplicationException {
    public TokenConvertException(String errorMessage) {
        super(HttpStatus.BAD_REQUEST, errorMessage);
    }
}

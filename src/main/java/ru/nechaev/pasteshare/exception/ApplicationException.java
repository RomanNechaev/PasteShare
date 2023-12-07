package ru.nechaev.pasteshare.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class ApplicationException extends RuntimeException {
    private final HttpStatus targetStatus;
    private final String errorMessage;
}

package ru.nechaev.pasteshare.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nechaev.pasteshare.exception.validate.ValidationErrorResponse;
import ru.nechaev.pasteshare.exception.validate.Violation;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandlingControllerAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintValidationException(ConstraintViolationException e) {
        List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidationErrorResponse(violations));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(ApplicationException e) {
        return ResponseEntity.status(e.getTargetStatus()).body(new ErrorMessage(e.getErrorMessage()));
    }

//    @ExceptionHandler(Throwable.class)
//    public ResponseEntity<ErrorMessage> handleException(Throwable e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage("Unknown error!"));
//    }
}

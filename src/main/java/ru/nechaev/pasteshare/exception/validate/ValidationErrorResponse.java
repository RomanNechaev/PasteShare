package ru.nechaev.pasteshare.exception.validate;

import java.util.List;

public record ValidationErrorResponse(List<Violation> violations) {
}

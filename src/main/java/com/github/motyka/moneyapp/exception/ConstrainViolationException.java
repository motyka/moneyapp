package com.github.motyka.moneyapp.exception;

public class ConstrainViolationException extends MoneyAppException {
    public ConstrainViolationException(String message) {
        super(message);
    }

    public ConstrainViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}

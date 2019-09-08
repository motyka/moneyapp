package com.github.motyka.moneyapp.exception;

public class MoneyAppException extends RuntimeException {
    public MoneyAppException() {
        super();
    }

    public MoneyAppException(String message) {
        super(message);
    }

    public MoneyAppException(String message, Throwable cause) {
        super(message, cause);
    }
}

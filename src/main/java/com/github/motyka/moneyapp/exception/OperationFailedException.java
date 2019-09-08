package com.github.motyka.moneyapp.exception;

public class OperationFailedException extends MoneyAppException {
    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Exception cause) {
        super(message, cause);
    }
}

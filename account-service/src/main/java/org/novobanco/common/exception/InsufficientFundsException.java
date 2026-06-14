package org.novobanco.common.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient funds to complete the operation");
    }
}

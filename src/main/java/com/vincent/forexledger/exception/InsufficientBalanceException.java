package com.vincent.forexledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientBalanceException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Balance is insufficient. Current: %.2f. Amount: %.2f.";

    public InsufficientBalanceException(double currentBalance, double amount) {
        super(String.format(MESSAGE_TEMPLATE, currentBalance, amount));
    }
}

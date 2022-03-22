package com.vincent.forexledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class DuplicatedKeyException extends RuntimeException {

    public DuplicatedKeyException(String message) {
        super(message);
    }
}

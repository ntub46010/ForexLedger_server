package com.vincent.forexledger.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TokenParseFailedException extends RuntimeException {

    public TokenParseFailedException(Throwable cause) {
        super(cause);
    }
}

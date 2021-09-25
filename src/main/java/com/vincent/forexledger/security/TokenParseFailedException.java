package com.vincent.forexledger.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
class TokenParseFailedException extends RuntimeException {

    TokenParseFailedException(Throwable cause) {
        super(cause);
    }
}

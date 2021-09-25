package com.vincent.forexledger.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincent.forexledger.security.IAccessTokenParser;
import com.vincent.forexledger.security.SpringUser;
import com.vincent.forexledger.security.TokenParseFailedException;

public class DummyAccessTokenParser implements IAccessTokenParser {

    private final ObjectMapper objectMapper;

    public DummyAccessTokenParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public SpringUser parse(String token) throws TokenParseFailedException {
        try {
            return objectMapper.readValue(token, SpringUser.class);
        } catch (JsonProcessingException e) {
            throw new TokenParseFailedException(e);
        }
    }
}

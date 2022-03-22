package com.vincent.forexledger.security;

public interface IAccessTokenParser {
    SpringUser parse(String token) throws TokenParseFailedException;
}

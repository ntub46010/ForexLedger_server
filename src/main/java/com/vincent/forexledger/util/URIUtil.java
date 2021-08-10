package com.vincent.forexledger.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public class URIUtil {
    private URIUtil() {
    }

    public static URI create(String path, String... uriVariable) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path(path)
                .buildAndExpand((Object[]) uriVariable)
                .toUri();
    }
}

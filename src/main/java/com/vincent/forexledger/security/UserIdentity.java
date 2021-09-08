package com.vincent.forexledger.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserIdentity {
    private static final SpringUser EMPTY_USER = new SpringUser();

    private SpringUser getSpringUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        return "anonymousUser".equals(principal)
                ? EMPTY_USER
                : (SpringUser) principal;
    }

    public String getId() {
        return getSpringUser().getId();
    }

    public String getName() {
        return getSpringUser().getName();
    }

    public String getEmail() {
        return getSpringUser().getEmail();
    }
}

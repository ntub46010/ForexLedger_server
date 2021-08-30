package com.vincent.forexledger.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.vincent.forexledger.security.SpringUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class VerifyTokenFilter extends OncePerRequestFilter {

    private static final String PREFIX_BEARAR_TOKEN = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            var headerAuthorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotEmpty(headerAuthorization)) {
                var bearerToken = StringUtils.replaceOnce(headerAuthorization, PREFIX_BEARAR_TOKEN, "");
                var firebaseToken = FirebaseAuth.getInstance().verifyIdToken(bearerToken);
                setAuthentication(firebaseToken);
            }
            filterChain.doFilter(request, response);
        } catch (FirebaseAuthException e) {
            // TODO: handle token expired
            logger.error(e.getMessage(), e);
        }
    }

    private void setAuthentication(FirebaseToken token) {
        var springUser = new SpringUser();
        springUser.setId(token.getUid());
        springUser.setName(token.getName());
        springUser.setEmail(token.getEmail());

        var authentication =
                new UsernamePasswordAuthenticationToken(springUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

package com.vincent.forexledger.filter;

import com.vincent.forexledger.security.IAccessTokenParser;
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

    private IAccessTokenParser tokenParser;

    public VerifyTokenFilter(IAccessTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var headerAuthorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotEmpty(headerAuthorization)) {
            var bearerToken = StringUtils
                    .replaceOnce(headerAuthorization, PREFIX_BEARAR_TOKEN, "");
            var springUser = tokenParser.parse(bearerToken);
            setAuthentication(springUser);
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(SpringUser user) {
        var authentication =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

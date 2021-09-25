package com.vincent.forexledger.security;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.filter.VerifyTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private IAccessTokenParser tokenParser;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.POST, "/users").permitAll()
                .antMatchers(HttpMethod.GET, APIPathConstants.EXCHANGE_RATES).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new VerifyTokenFilter(tokenParser), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();
    }
}

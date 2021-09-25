package com.vincent.forexledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("squid:S2187")
@AutoConfigureMockMvc
@SpringBootTest
public class BaseTest {

    protected HttpHeaders httpHeaders;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected ExchangeRateRepository exchangeRateRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Before
    public void init() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        clearDB();
    }

    private void clearDB() {
        appUserRepository.deleteAll();
        exchangeRateRepository.deleteAll();
        bookRepository.deleteAll();
    }

    protected void appendAccessToken(String userId, String name) {
        var tokenInfo = new HashMap<String, Object>();
        tokenInfo.put("name", name);

        try {
            var token = FirebaseAuth.getInstance().createCustomToken(userId, tokenInfo);
            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }

}

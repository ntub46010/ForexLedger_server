package com.vincent.forexledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincent.forexledger.repository.AppUserRepository;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Before
    public void init() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        clearDB();
    }

    private void clearDB() {
        appUserRepository.deleteAll();
    }

}
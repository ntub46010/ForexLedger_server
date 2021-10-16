package com.vincent.forexledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincent.forexledger.config.DummyComponentConfig;
import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.security.SpringUser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("squid:S2187")
@Import(DummyComponentConfig.class)
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

    protected void appendAccessToken(String userId, String name) throws Exception {
        var springUser = new SpringUser();
        springUser.setId(userId);
        springUser.setName(name);
        springUser.setEmail(name + "@test.com");

        var token = objectMapper.writeValueAsString(springUser);
        httpHeaders.remove(HttpHeaders.AUTHORIZATION);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected String createBook(String name, BankType bank, CurrencyType currencyType) throws Exception {
        var request = new CreateBookRequest();
        request.setName(name);
        request.setBank(bank);
        request.setCurrencyType(currencyType);

        var mvcResult = mockMvc.perform(post(APIPathConstants.BOOKS)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);

        return StringUtils.substringAfterLast(location, '/');
    }

    protected ExchangeRate createExchangeRate(BankType bank, CurrencyType currencyType, double sellingRate, double buyingRate) {
        var exchangeRate = new ExchangeRate();
        exchangeRate.setBankType(bank);
        exchangeRate.setCurrencyType(currencyType);
        exchangeRate.setSellingRate(sellingRate);
        exchangeRate.setBuyingRate(buyingRate);

        return exchangeRateRepository.insert(exchangeRate);
    }

}

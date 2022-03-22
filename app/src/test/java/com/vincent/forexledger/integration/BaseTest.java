package com.vincent.forexledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincent.forexledger.config.DummyComponentConfig;
import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.security.SpringUser;
import com.vincent.forexledger.service.ExchangeRateTable;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

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
    protected ExchangeRateTable exchangeRateTable;

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected ExchangeRateRepository exchangeRateRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected EntryRepository entryRepository;

    @Before
    public void init() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        clearDB();
        insertDefaultExchangeRate();
    }

    private void clearDB() {
        appUserRepository.deleteAll();
        exchangeRateRepository.deleteAll();
        bookRepository.deleteAll();
        entryRepository.deleteAll();
    }

    private void insertDefaultExchangeRate() {
        var exchangeRates = List.of(
                genExchangeRate(BankType.FUBON, CurrencyType.USD, 27.945, 27.845),
                genExchangeRate(BankType.FUBON, CurrencyType.CNY, 4.3927, 4.3427),
                genExchangeRate(BankType.FUBON, CurrencyType.JPY, 0.2467, 0.2431),
                genExchangeRate(BankType.FUBON, CurrencyType.EUR, 32.6698, 32.2698),
                genExchangeRate(BankType.FUBON, CurrencyType.HKD, 3.6146, 3.5606),
                genExchangeRate(BankType.FUBON, CurrencyType.AUD, 21.0559, 20.7559),
                genExchangeRate(BankType.FUBON, CurrencyType.ZAR, 1.9624, 1.8524),
                genExchangeRate(BankType.FUBON, CurrencyType.CAD, 22.748, 22.448),
                genExchangeRate(BankType.FUBON, CurrencyType.GBP, 38.6584, 38.1784),
                genExchangeRate(BankType.FUBON, CurrencyType.SGD, 20.8652, 20.6252),
                genExchangeRate(BankType.FUBON, CurrencyType.CHF, 30.5799, 30.2599),
                genExchangeRate(BankType.FUBON, CurrencyType.NZD, 20.1708, 19.8808),
                genExchangeRate(BankType.FUBON, CurrencyType.SEK, 3.2802, 3.2202),
                genExchangeRate(BankType.FUBON, CurrencyType.THB, 0.8576, 0.8176),
                genExchangeRate(BankType.RICHART, CurrencyType.USD, 27.91, 27.87),
                genExchangeRate(BankType.RICHART, CurrencyType.CNY, 4.381, 4.349),
                genExchangeRate(BankType.RICHART, CurrencyType.JPY, 0.2465, 0.2446),
                genExchangeRate(BankType.RICHART, CurrencyType.EUR, 32.528, 32.338),
                genExchangeRate(BankType.RICHART, CurrencyType.HKD, 3.6031, 3.5731),
                genExchangeRate(BankType.RICHART, CurrencyType.AUD, 20.875, 20.743),
                genExchangeRate(BankType.RICHART, CurrencyType.ZAR, 1.9044, 1.8564),
                genExchangeRate(BankType.RICHART, CurrencyType.CAD, 22.593, 22.449),
                genExchangeRate(BankType.RICHART, CurrencyType.GBP, 38.428, 38.208),
                genExchangeRate(BankType.RICHART, CurrencyType.SGD, 20.734, 20.602),
                genExchangeRate(BankType.RICHART, CurrencyType.CHF, 30.516, 30.312),
                genExchangeRate(BankType.RICHART, CurrencyType.NZD, 19.983, 19.839),
                genExchangeRate(BankType.RICHART, CurrencyType.SEK, 3.2665, 3.2305)
        );
        var responses = ExchangeRateConverter.toResponses(exchangeRates);

        exchangeRateTable.put(responses);
        exchangeRateRepository.insert(exchangeRates);
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

    protected String createEntry(CreateEntryRequest request) throws Exception {
        var mvcResult = mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);

        return StringUtils.substringAfterLast(location, '/');
    }

    private ExchangeRate genExchangeRate(BankType bank, CurrencyType currencyType, double sellingRate, double buyingRate) {
        var exchangeRate = new ExchangeRate();
        exchangeRate.setBankType(bank);
        exchangeRate.setCurrencyType(currencyType);
        exchangeRate.setSellingRate(sellingRate);
        exchangeRate.setBuyingRate(buyingRate);
        exchangeRate.setCreatedTime(new Date());

        return exchangeRate;
    }

}

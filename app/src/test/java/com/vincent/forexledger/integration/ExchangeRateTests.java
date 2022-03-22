package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.constants.QueryStringConstants;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("squid:S2699")
@RunWith(SpringRunner.class)
public class ExchangeRateTests extends BaseTest {

    @Test
    public void testGetExchangeRates() throws Exception {
        var fubonExchangeRates = exchangeRateRepository.findByBankTypeIn(List.of(BankType.FUBON));

        var mvcResult = mockMvc.perform(get(APIPathConstants.EXCHANGE_RATES)
                .param(QueryStringConstants.BANK, BankType.FUBON.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(fubonExchangeRates.size())))
                .andExpect(jsonPath("$[0].updatedTime").value(IsNull.notNullValue()))
                .andReturn();
        var responseStr = mvcResult.getResponse().getContentAsString();
        var responses = objectMapper.readValue(responseStr, ExchangeRateResponse[].class);

        var actualCurrencyToExRateMap = Arrays.stream(responses)
                .collect(Collectors.toMap(ExchangeRateResponse::getCurrencyType, Function.identity()));
        var expectedCurrencyTypes = fubonExchangeRates.stream()
                .map(ExchangeRate::getCurrencyType)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                actualCurrencyToExRateMap.keySet(), expectedCurrencyTypes));

        fubonExchangeRates.forEach(expectedRate -> {
            var actualRate = actualCurrencyToExRateMap.get(expectedRate.getCurrencyType());
            Assert.assertEquals(expectedRate.getSellingRate(), actualRate.getSellingRate(), 0);
            Assert.assertEquals(expectedRate.getBuyingRate(), actualRate.getBuyingRate(), 0);
        });
    }

    @Test
    public void test400WhenGetExchangeRateWithoutSpecifyingBank() throws Exception {
        mockMvc.perform(get(APIPathConstants.EXCHANGE_RATES))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test400WhenGetExchangeRateWithUnknownBank() throws Exception {
        mockMvc.perform(get(APIPathConstants.EXCHANGE_RATES)
                .param(QueryStringConstants.BANK, "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}

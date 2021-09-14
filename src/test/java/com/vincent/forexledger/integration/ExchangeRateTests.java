package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.constants.QueryStringConstants;
import com.vincent.forexledger.model.CurrencyType;
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
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("squid:S2699")
@RunWith(SpringRunner.class)
public class ExchangeRateTests extends BaseTest {

    @Test
    public void testGetExchangeRates() throws Exception {
        var usdRate = createExchangeRate(CurrencyType.USD, BankType.FUBON, 27.76, 27.66);
        var cnyRate = createExchangeRate(CurrencyType.CNY, BankType.FUBON,4.318, 4.278);
        var jpyRate = createExchangeRate(CurrencyType.JPY, BankType.RICHART,0.2539, 0.2504);
        exchangeRateRepository.insert(List.of(usdRate, cnyRate, jpyRate));

        var mvcResult = mockMvc.perform(get(APIPathConstants.EXCHANGE_RATES)
                .param(QueryStringConstants.BANK, BankType.FUBON.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].updatedTime").value(IsNull.notNullValue()))
                .andExpect(jsonPath("$[1].updatedTime").value(IsNull.notNullValue()))
                .andReturn();
        var responseStr = mvcResult.getResponse().getContentAsString();
        var responses = objectMapper.readValue(responseStr, ExchangeRateResponse[].class);

        var currencyToExRateMap = Arrays.stream(responses)
                .collect(Collectors.toMap(ExchangeRateResponse::getCurrencyType, Function.identity()));
        var expectedCurrencyTypes = Stream.of(usdRate, cnyRate)
                .map(ExchangeRate::getCurrencyType)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                currencyToExRateMap.keySet(), expectedCurrencyTypes));

        var usdRateRes = currencyToExRateMap.get(CurrencyType.USD);
        Assert.assertEquals(usdRate.getSellingRate(), usdRateRes.getSellingRate(), 0);
        Assert.assertEquals(usdRate.getBuyingRate(), usdRateRes.getBuyingRate(), 0);

        var cnyRateRes = currencyToExRateMap.get(CurrencyType.CNY);
        Assert.assertEquals(cnyRate.getSellingRate(), cnyRateRes.getSellingRate(), 0);
        Assert.assertEquals(cnyRate.getBuyingRate(), cnyRateRes.getBuyingRate(), 0);
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

    private ExchangeRate createExchangeRate(
            CurrencyType currencyType, BankType bankType, double sellingRate, double buyingRate) {
        var rate = new ExchangeRate();
        rate.setCurrencyType(currencyType);
        rate.setBankType(bankType);
        rate.setSellingRate(sellingRate);
        rate.setBuyingRate(buyingRate);
        rate.setCreatedTime(new Date());

        return rate;
    }
}
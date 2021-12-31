package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExchangeRateConverterTest {

    /*
    @Test
    public void testConvertToRichartRate() {
        var taishinExRates = Arrays.asList(
                createFindRateResponse(CurrencyType.USD, BankType.RICHART, 27.76, 27.66),
                createFindRateResponse(CurrencyType.CNY, BankType.RICHART,4.318, 4.278),
                createFindRateResponse(CurrencyType.JPY, BankType.RICHART,0.2539, 0.2504),
                createFindRateResponse(CurrencyType.EUR, BankType.RICHART,32.914, 32.564),
                createFindRateResponse(CurrencyType.HKD, BankType.RICHART,3.5877, 3.5377),
                createFindRateResponse(CurrencyType.AUD, BankType.RICHART,20.493, 20.273),
                createFindRateResponse(CurrencyType.ZAR, BankType.RICHART,1.9894, 1.9094),
                createFindRateResponse(CurrencyType.CAD, BankType.RICHART,21.953, 21.713),
                createFindRateResponse(CurrencyType.GBP, BankType.RICHART,38.553, 38.153),
                createFindRateResponse(CurrencyType.SGD, BankType.RICHART,20.764, 20.544),
                createFindRateResponse(CurrencyType.CHF, BankType.RICHART,30.362, 30.022),
                createFindRateResponse(CurrencyType.NZD, BankType.RICHART,19.833, 19.593),
                createFindRateResponse(CurrencyType.SEK, BankType.RICHART,3.24, 3.18)
        );

        var currencyToRichartRateMap = taishinExRates.stream()
                .map(ExchangeRateConverter::toRichartExRate)
                .collect(Collectors.toMap(FindRateResponse::getCurrencyType, Function.identity()));

        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.USD), 27.73, 27.69);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.CNY), 4.314, 4.282);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.JPY), 0.2531, 0.2512);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.EUR), 32.834, 32.644);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.HKD), 3.5777, 3.5477);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.AUD), 20.449, 20.317);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.ZAR), 1.9734, 1.9254);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.CAD), 21.905, 21.761);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.GBP), 38.463, 38.243);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.SGD), 20.72, 20.588);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.CHF), 30.294, 30.09);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.NZD), 19.785, 19.641);
        assertExchangeRate(currencyToRichartRateMap.get(CurrencyType.SEK), 3.228, 3.192);
    }
    */

    @Test
    public void testConvertFindRateResponseToExchangeRate() {
        var createdTime = new Date();
        BiConsumer<FindRateResponse, ExchangeRate> assertFunc = (expected, actual) -> {
            Assert.assertEquals(expected.getCurrencyType(), actual.getCurrencyType());
            Assert.assertEquals(expected.getBankType(), actual.getBankType());
            Assert.assertEquals(expected.getSellingRate(), actual.getSellingRate(), 0);
            Assert.assertEquals(expected.getBuyingRate(), actual.getBuyingRate(), 0);
            Assert.assertEquals(createdTime, actual.getCreatedTime());
        };

        var response = createFindRateResponse(CurrencyType.USD, BankType.FUBON,27.76, 27.66);

        var exchangeRate = ExchangeRateConverter.toExchangeRate(response, createdTime);
        assertFunc.accept(response, exchangeRate);

        var exchangeRates = ExchangeRateConverter.toExchangeRates(List.of(response), createdTime);
        Assert.assertEquals(1, exchangeRates.size());
        assertFunc.accept(response, exchangeRates.get(0));
    }

    @Test
    public void testConvertToResponse() {
        BiConsumer<ExchangeRate, ExchangeRateResponse> assertFunc = (expected, actual) -> {
            Assert.assertEquals(expected.getBankType(), actual.getBank());
            Assert.assertEquals(expected.getCurrencyType(), actual.getCurrencyType());
            Assert.assertEquals(expected.getSellingRate(), actual.getSellingRate(), 0);
            Assert.assertEquals(expected.getBuyingRate(), actual.getBuyingRate(), 0);
            Assert.assertEquals(expected.getCreatedTime(), actual.getUpdatedTime());
        };

        var exchangeRate = new ExchangeRate();
        exchangeRate.setBankType(BankType.FUBON);
        exchangeRate.setCurrencyType(CurrencyType.USD);
        exchangeRate.setSellingRate(27.76);
        exchangeRate.setBuyingRate(27.66);
        exchangeRate.setCreatedTime(new Date());

        var response = ExchangeRateConverter.toResponse(exchangeRate);
        assertFunc.accept(exchangeRate, response);

        var responses = ExchangeRateConverter.toResponses(List.of(exchangeRate));
        Assert.assertEquals(1, responses.size());
        assertFunc.accept(exchangeRate, responses.get(0));
    }

    private FindRateResponse createFindRateResponse(
            CurrencyType currencyType, BankType bankType, double sellingRate, double buyingRate) {
        var rate = new FindRateResponse();
        rate.setCurrencyType(currencyType);
        rate.setBankType(bankType);
        rate.setSellingRate(sellingRate);
        rate.setBuyingRate(buyingRate);

        return rate;
    }

    private void assertExchangeRate(FindRateResponse rate, double expectedSellingRate, double expectedBuyingRate) {
        Assert.assertEquals(expectedSellingRate, rate.getSellingRate(), 0);
        Assert.assertEquals(expectedBuyingRate, rate.getBuyingRate(), 0);
    }
}

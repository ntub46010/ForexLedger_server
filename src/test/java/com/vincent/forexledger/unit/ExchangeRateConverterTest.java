package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExchangeRateConverterTest {

    @Test
    public void testConvertTaishinRateToRichartRate() {
        List<FindRateResponse> taishinRates = Arrays.asList(
                createFindRateResponse(CurrencyType.USD, 27.76, 27.66),
                createFindRateResponse(CurrencyType.CNY, 4.318, 4.278),
                createFindRateResponse(CurrencyType.JPY, 0.2539, 0.2504),
                createFindRateResponse(CurrencyType.EUR, 32.914, 32.564),
                createFindRateResponse(CurrencyType.HKD, 3.5877, 3.5377),
                createFindRateResponse(CurrencyType.AUD, 20.493, 20.273),
                createFindRateResponse(CurrencyType.ZAR, 1.9894, 1.9094),
                createFindRateResponse(CurrencyType.CAD, 21.953, 21.713),
                createFindRateResponse(CurrencyType.GBP, 38.553, 38.153),
                createFindRateResponse(CurrencyType.SGD, 20.764, 20.544),
                createFindRateResponse(CurrencyType.CHF, 30.362, 30.022),
                createFindRateResponse(CurrencyType.NZD, 19.833, 19.593),
                createFindRateResponse(CurrencyType.SEK, 3.24, 3.18)
        );

        Map<CurrencyType, FindRateResponse> richartRateMap = taishinRates.stream()
                .map(ExchangeRateConverter::toRichartExRate)
                .collect(Collectors.toMap(FindRateResponse::getCurrencyType, Function.identity()));

        assertExchangeRate(richartRateMap.get(CurrencyType.USD), 27.73, 27.69);
        assertExchangeRate(richartRateMap.get(CurrencyType.CNY), 4.314, 4.282);
        assertExchangeRate(richartRateMap.get(CurrencyType.JPY), 0.2531, 0.2512);
        assertExchangeRate(richartRateMap.get(CurrencyType.EUR), 32.834, 32.644);
        assertExchangeRate(richartRateMap.get(CurrencyType.HKD), 3.5777, 3.5477);
        assertExchangeRate(richartRateMap.get(CurrencyType.AUD), 20.449, 20.317);
        assertExchangeRate(richartRateMap.get(CurrencyType.ZAR), 1.9734, 1.9254);
        assertExchangeRate(richartRateMap.get(CurrencyType.CAD), 21.905, 21.761);
        assertExchangeRate(richartRateMap.get(CurrencyType.GBP), 38.463, 38.243);
        assertExchangeRate(richartRateMap.get(CurrencyType.SGD), 20.72, 20.588);
        assertExchangeRate(richartRateMap.get(CurrencyType.CHF), 30.294, 30.09);
        assertExchangeRate(richartRateMap.get(CurrencyType.NZD), 19.785, 19.641);
        assertExchangeRate(richartRateMap.get(CurrencyType.SEK), 3.23, 3.19);
    }

    private FindRateResponse createFindRateResponse(CurrencyType type, double sellingRate, double buyingRate) {
        FindRateResponse rate = new FindRateResponse();
        rate.setCurrencyType(type);
        rate.setSellingRate(sellingRate);
        rate.setBuyingRate(buyingRate);

        return rate;
    }

    private void assertExchangeRate(FindRateResponse rate, double expectedSellingRate, double expectedBuyingRate) {
        Assert.assertEquals(expectedSellingRate, rate.getSellingRate(), 0);
        Assert.assertEquals(expectedBuyingRate, rate.getBuyingRate(), 0);
    }
}

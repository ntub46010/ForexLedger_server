package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.util.CalcUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateConverter {
    private static final Map<CurrencyType, BigDecimal> richartDiscountMap;

    static {
        richartDiscountMap = new EnumMap<>(CurrencyType.class);
        richartDiscountMap.put(CurrencyType.USD, BigDecimal.valueOf(0.03));
        richartDiscountMap.put(CurrencyType.CNY, BigDecimal.valueOf(0.004));
        richartDiscountMap.put(CurrencyType.JPY, BigDecimal.valueOf(0.0008));
        richartDiscountMap.put(CurrencyType.EUR, BigDecimal.valueOf(0.08));
        richartDiscountMap.put(CurrencyType.HKD, BigDecimal.valueOf(0.01));
        richartDiscountMap.put(CurrencyType.AUD, BigDecimal.valueOf(0.044));
        richartDiscountMap.put(CurrencyType.GBP, BigDecimal.valueOf(0.09));
    }

    private ExchangeRateConverter() {
    }

    public static FindRateResponse toFindRateResponse(Element tableRow) {
        var tableData = tableRow.select("td");
        var currencyTypeFullName = tableData.get(0).selectFirst("a").text();
        var currencyCode = StringUtils.split(currencyTypeFullName, " ")[1];

        var rate = new FindRateResponse();
        rate.setCurrencyType(CurrencyType.valueOf(currencyCode));
        rate.setSellingRate(Double.parseDouble(tableData.get(4).text()));
        rate.setBuyingRate(Double.parseDouble(tableData.get(3).text()));

        return rate;
    }

    public static FindRateResponse toRichartExRate(FindRateResponse response) {
        var discount = richartDiscountMap.get(response.getCurrencyType());
        if (discount == null) {
            discount = CalcUtil.subtractToDecimal(response.getSellingRate(), response.getBuyingRate());
            discount = CalcUtil.divideToDecimal(discount, 5, 4);
        }

        var sellingRate = CalcUtil.subtractToDouble(response.getSellingRate(), discount);
        response.setSellingRate(sellingRate);

        var buyingRate = CalcUtil.addToDouble(response.getBuyingRate(), discount);
        response.setBuyingRate(buyingRate);

        return response;
    }

    public static List<ExchangeRate> toExchangeRates(List<FindRateResponse> response, Date createdTime) {
        return response.stream()
                .map(r -> toExchangeRate(r, createdTime))
                .collect(Collectors.toList());
    }

    public static ExchangeRate toExchangeRate(FindRateResponse response, Date createdTime) {
        var rate = new ExchangeRate();
        rate.setCurrencyType(response.getCurrencyType());
        rate.setBankType(response.getBankType());
        rate.setSellingRate(response.getSellingRate());
        rate.setBuyingRate(response.getBuyingRate());
        rate.setCreatedTime(createdTime);

        return rate;
    }

    public static List<ExchangeRateResponse> toResponses(List<ExchangeRate> rates) {
        return rates.stream()
                .map(ExchangeRateConverter::toResponse)
                .collect(Collectors.toList());
    }

    public static ExchangeRateResponse toResponse(ExchangeRate rate) {
        var response = new ExchangeRateResponse();
        response.setCurrencyType(rate.getCurrencyType());
        response.setSellingRate(rate.getSellingRate());
        response.setBuyingRate(rate.getBuyingRate());
        response.setUpdatedTime(rate.getCreatedTime());

        return response;
    }
}

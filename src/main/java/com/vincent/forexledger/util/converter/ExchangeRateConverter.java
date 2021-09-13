package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        Elements tableData = tableRow.select("td");
        String currencyTypeLabel = tableData.get(0).selectFirst("a").text();
        String currencyCode = StringUtils.split(currencyTypeLabel, " ")[1];

        FindRateResponse rate = new FindRateResponse();
        rate.setCurrencyType(CurrencyType.fromString(currencyCode));
        rate.setSellingRate(Double.parseDouble(tableData.get(4).text()));
        rate.setBuyingRate(Double.parseDouble(tableData.get(3).text()));

        return rate;
    }

    public static FindRateResponse toRichartExRate(FindRateResponse response) {
        BigDecimal sellingRate = BigDecimal.valueOf(response.getSellingRate());
        BigDecimal buyingRate = BigDecimal.valueOf(response.getBuyingRate());

        BigDecimal discount = richartDiscountMap.get(response.getCurrencyType());
        if (discount == null) {
            discount = sellingRate
                    .subtract(buyingRate)
                    .divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP);
        }

        sellingRate  = sellingRate.subtract(discount);
        buyingRate = buyingRate.add(discount);
        response.setSellingRate(sellingRate.doubleValue());
        response.setBuyingRate(buyingRate.doubleValue());

        return response;
    }

    public static List<ExchangeRate> toExchangeRates(List<FindRateResponse> response, Date createdTime) {
        return response.stream()
                .map(r -> toExchangeRate(r, createdTime))
                .collect(Collectors.toList());
    }

    public static ExchangeRate toExchangeRate(FindRateResponse response, Date createdTime) {
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyType(response.getCurrencyType());
        rate.setBankType(rate.getBankType());
        rate.setSellingRate(response.getSellingRate());
        rate.setBuyingRate(response.getBuyingRate());
        rate.setCreatedTime(createdTime);

        return rate;
    }
}

package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

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

    public static FindRateResponse toRichartExRate(FindRateResponse taishinExRate) {
        BigDecimal sellingRate = BigDecimal.valueOf(taishinExRate.getSellingRate());
        BigDecimal buyingRate = BigDecimal.valueOf(taishinExRate.getBuyingRate());

        BigDecimal discount = richartDiscountMap.get(taishinExRate.getCurrencyType());
        if (discount == null) {
            discount = sellingRate
                    .subtract(buyingRate)
                    .divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP);
        }

        sellingRate  = sellingRate.subtract(discount);
        buyingRate = buyingRate.add(discount);
        taishinExRate.setSellingRate(sellingRate.doubleValue());
        taishinExRate.setBuyingRate(buyingRate.doubleValue());

        return taishinExRate;
    }

    public static ExchangeRate toExchangeRate(FindRateResponse response) {
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyType(response.getCurrencyType());
        rate.setSellingRate(response.getSellingRate());
        rate.setBuyingRate(response.getBuyingRate());

        return rate;
    }
}

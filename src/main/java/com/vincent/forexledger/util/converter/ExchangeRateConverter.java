package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.EnumMap;
import java.util.Map;

public class ExchangeRateConverter {
    private static final Map<CurrencyType, Double> richartDiscountMap;

    static {
        richartDiscountMap = new EnumMap<>(CurrencyType.class);
        richartDiscountMap.put(CurrencyType.USD, 0.03);
        richartDiscountMap.put(CurrencyType.CNY, 0.004);
        richartDiscountMap.put(CurrencyType.JPY, 0.0008);
        richartDiscountMap.put(CurrencyType.EUR, 0.08);
        richartDiscountMap.put(CurrencyType.HKD, 0.01);
        richartDiscountMap.put(CurrencyType.AUD, 0.044);
        richartDiscountMap.put(CurrencyType.GBP, 0.09);
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
        double sellingRate = taishinExRate.getSellingRate();
        double buyingRate = taishinExRate.getBuyingRate();

        Double discount = richartDiscountMap.get(taishinExRate.getCurrencyType());
        if (discount == null) {
            discount = (sellingRate - buyingRate) / 5;
        }

        sellingRate -= discount;
        buyingRate += discount;
        taishinExRate.setSellingRate(sellingRate);
        taishinExRate.setBuyingRate(buyingRate);

        return taishinExRate;
    }
}

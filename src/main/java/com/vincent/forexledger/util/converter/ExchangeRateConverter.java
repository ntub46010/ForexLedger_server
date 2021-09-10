package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExchangeRateConverter {
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
        return null;
    }
}

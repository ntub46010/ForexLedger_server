package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.util.ExchangeRateDiscountTable;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRateConverter {

    private ExchangeRateConverter() {
    }

    public static FindRateResponse toFindRateResponse(BankType bank, Element tableRow) {
        var tableData = tableRow.select("td");
        var currencyTypeFullName = tableData.get(0).selectFirst("a").text();
        var currencyCode = StringUtils.split(currencyTypeFullName, " ")[1];

        var rate = new FindRateResponse();
        rate.setBankType(bank);
        rate.setCurrencyType(CurrencyType.valueOf(currencyCode));
        rate.setSellingRate(Double.parseDouble(tableData.get(4).text()));
        rate.setBuyingRate(Double.parseDouble(tableData.get(3).text()));
        ExchangeRateDiscountTable.updatedToDiscountedRate(rate);

        return rate;
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
        response.setBank(rate.getBankType());
        response.setCurrencyType(rate.getCurrencyType());
        response.setSellingRate(rate.getSellingRate());
        response.setBuyingRate(rate.getBuyingRate());
        response.setUpdatedTime(rate.getCreatedTime());

        return response;
    }
}

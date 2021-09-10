package com.vincent.forexledger.client;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExchangeRateClient {

    public List<FindRateResponse> load(BankType bank) throws IOException {
        Document htmlDocument = Jsoup.connect(bank.getExchangeRateUrl()).get();

        Elements tableRowsOfExRates = htmlDocument
                .select("div#right table")
                .first()
                .select("tbody tr")
                .next();

        List<FindRateResponse> rates = tableRowsOfExRates.stream()
                .map(this::toFindRateResponse)
                .collect(Collectors.toList());

        if (bank == BankType.RICHART) {
            // TODO
        }

        return rates;
    }

    private FindRateResponse toFindRateResponse(Element tableRow) {
        Elements tableData = tableRow.select("td");
        String currencyTypeLabel = tableData.get(0).selectFirst("a").text();
        String currencyCode = StringUtils.split(currencyTypeLabel, " ")[1];

        FindRateResponse rate = new FindRateResponse();
        rate.setCurrencyType(CurrencyType.fromString(currencyCode));
        rate.setSellingRate(Double.parseDouble(tableData.get(4).text()));
        rate.setBuyingRate(Double.parseDouble(tableData.get(3).text()));

        return rate;
    }

}

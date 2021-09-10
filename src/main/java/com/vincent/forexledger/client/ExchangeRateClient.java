package com.vincent.forexledger.client;

import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExchangeRateClient {

    public List<FindRateResponse> load(BankType bank) throws IOException {
        Document htmlDocument = Jsoup.connect(bank.getExchangeRateUrl()).get();
        Elements tableRowsOfExRates = htmlDocument
                .select("div#right table")
                .first()
                .select("tbody tr")
                .next();

        Stream<FindRateResponse> tableRowOfExRatesStream = tableRowsOfExRates
                .stream()
                .map(ExchangeRateConverter::toFindRateResponse);

        List<FindRateResponse> rates;
        if (bank == BankType.RICHART) {
            rates = tableRowOfExRatesStream
                    .map(ExchangeRateConverter::toRichartExRate)
                    .collect(Collectors.toList());
        } else {
            rates = tableRowOfExRatesStream.collect(Collectors.toList());
        }

        return rates;
    }

}

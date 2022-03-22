package com.vincent.forexledger.client;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.jsoup.Jsoup;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

public class DownloadExchangeRateClient {
    private RestTemplate restTemplate;

    public DownloadExchangeRateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FindRateResponse> load(BankType bank) {
        var htmlStr = restTemplate.getForObject(bank.getExchangeRateUrl(), String.class);
        var htmlDocument = Jsoup.parse(htmlStr);
        var tableRowsOfExRates = htmlDocument
                .selectFirst("div#right table")
                .select("tbody tr")
                .next();

        var responses = tableRowsOfExRates.stream()
                .map(row -> ExchangeRateConverter.toFindRateResponse(bank, row))
                .collect(Collectors.toList());

        responses.removeIf(rate ->
                rate.getBankType() == BankType.RICHART || rate.getCurrencyType() == CurrencyType.THB);

        return responses;
    }

}

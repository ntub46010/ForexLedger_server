package com.vincent.forexledger.service;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DownloadExchangeRateClient exchangeRateClient;
    private ExchangeRateRepository repository;

    public ExchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        this.exchangeRateClient = client;
        this.repository = repository;
    }

    @Scheduled(cron = "0 */15 * * * ?")
    private void refreshExchangeRateData() {
        List<ExchangeRate> allExRates = new ArrayList<>();
        Date now = new Date();
        for (BankType bank : BankType.values()) {
            try {
                List<FindRateResponse> responses = exchangeRateClient.load(bank);
                List<ExchangeRate> rates = ExchangeRateConverter.toExchangeRates(responses, now);

                allExRates.addAll(rates);
            } catch (IOException e) {
                logger.error("Failed to download or parse data during download {} exchange rate. {}",
                        bank.name(), e.getMessage());
            }
        }

        Map<BankType, List<ExchangeRate>> bankExRateMap = allExRates.stream()
                .collect(Collectors.groupingBy(ExchangeRate::getBankType, Collectors.toList()));
        overwriteBankExchangeRateSafely(bankExRateMap);
    }

    private void overwriteBankExchangeRateSafely(Map<BankType, List<ExchangeRate>> bankExRateMap) {
        List<ExchangeRate> newExRates = new ArrayList<>();
        bankExRateMap.values().forEach(newExRates::addAll);

        repository.insert(newExRates);
        repository.deleteByBankTypeIn(bankExRateMap.keySet());
    }

}

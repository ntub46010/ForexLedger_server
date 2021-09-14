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

import java.util.*;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DownloadExchangeRateClient exchangeRateClient;
    private ExchangeRateRepository repository;

    public ExchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        this.exchangeRateClient = client;
        this.repository = repository;
    }

    @Scheduled(cron = "${cron.exchangerate.refresh}")
    public void refreshExchangeRateData() {
        logger.info("Start to refresh exchange rate.");
        List<ExchangeRate> allExRates = new ArrayList<>();
        Date now = new Date();
        for (BankType bank : BankType.values()) {
            try {
                List<FindRateResponse> responses = exchangeRateClient.load(bank);
                List<ExchangeRate> rates = ExchangeRateConverter.toExchangeRates(responses, now);

                allExRates.addAll(rates);
            } catch (Exception e) {
                logger.error("Failed to download or parse data during download {} exchange rate. {}",
                        bank.name(), e.getMessage());
            }
        }

        Map<BankType, List<ExchangeRate>> bankExRateMap = allExRates.stream()
                .collect(Collectors.groupingBy(ExchangeRate::getBankType, Collectors.toList()));
        overwriteBankExchangeRateSafely(bankExRateMap);
        logger.info("Finish refreshing exchange rate.");
    }

    private void overwriteBankExchangeRateSafely(Map<BankType, List<ExchangeRate>> bankExRateMap) {
        List<ExchangeRate> oldRates = repository.findByBankTypeIn(bankExRateMap.keySet());
        List<String> oldRateIds = oldRates.stream()
                .map(ExchangeRate::getId)
                .collect(Collectors.toList());

        List<ExchangeRate> newExRates = bankExRateMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        repository.insert(newExRates);
        repository.deleteAllById(oldRateIds);
    }

}

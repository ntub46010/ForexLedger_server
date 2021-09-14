package com.vincent.forexledger.service;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.exception.NotFoundException;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DownloadExchangeRateClient exchangeRateClient;
    private ExchangeRateRepository repository;
    private Map<BankType, List<ExchangeRateResponse>> bankExchangeRateMap;

    public ExchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        this.exchangeRateClient = client;
        this.repository = repository;
        this.bankExchangeRateMap = new EnumMap<>(BankType.class);
    }

    public List<ExchangeRateResponse> loadExchangeRates(BankType bank) {
        List<ExchangeRateResponse> responses = bankExchangeRateMap.get(bank);
        if (responses == null) {
            List<ExchangeRate> dbRates = repository.findByBankTypeIn(List.of(bank));
            if (CollectionUtils.isEmpty(dbRates)) {
                throw new NotFoundException("Can't found exchange rates of " + bank);
            }
            responses = ExchangeRateConverter.toResponses(dbRates);
            bankExchangeRateMap.put(bank, responses);
        }

        return responses;
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

        bankExRateMap.forEach((bank, rates) -> {
            List<ExchangeRateResponse> responses = ExchangeRateConverter.toResponses(rates);
            bankExchangeRateMap.put(bank, responses);
        });
    }

}

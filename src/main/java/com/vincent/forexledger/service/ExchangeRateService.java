package com.vincent.forexledger.service;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.exception.NotFoundException;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.util.converter.ExchangeRateConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private DownloadExchangeRateClient exchangeRateClient;
    private ExchangeRateRepository repository;
    private Map<BankType, List<ExchangeRateResponse>> bankToExchangeRatesMap;

    public ExchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        this.exchangeRateClient = client;
        this.repository = repository;
        this.bankToExchangeRatesMap = new EnumMap<>(BankType.class);
    }

    public List<ExchangeRateResponse> loadExchangeRates(BankType bank) {
        var responses = bankToExchangeRatesMap.get(bank);
        if (responses == null) {
            var dbRates = repository.findByBankTypeIn(List.of(bank));
            if (CollectionUtils.isEmpty(dbRates)) {
                throw new NotFoundException("Can't found exchange rates of bank: " + bank);
            }
            responses = ExchangeRateConverter.toResponses(dbRates);
            bankToExchangeRatesMap.put(bank, responses);
        }

        return responses;
    }

    public ExchangeRateResponse loadExchangeRate(BankType bank, CurrencyType currencyType) {
        var bankExRates = loadExchangeRates(bank);
        return bankExRates.stream()
                .filter(rate -> rate.getCurrencyType() == currencyType)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Can't find exchange rate. " +
                        "Bank: %s. Currency type: %s.", bank.name(), currencyType.name())));
    }

    @Scheduled(cron = "${cron.exchangerate.refresh}")
    public void refreshExchangeRateData() {
        logger.info("Start to refresh exchange rate.");
        var allSavingExchangeRates = new ArrayList<ExchangeRate>();
        var now = new Date();
        for (var bank : BankType.values()) {
            try {
                var findRateResponses = exchangeRateClient.load(bank);
                var exchangeRates = ExchangeRateConverter.toExchangeRates(findRateResponses, now);

                allSavingExchangeRates.addAll(exchangeRates);
            } catch (Exception e) {
                logger.error("Failed to download or parse data during download {} exchange rate. {}",
                        bank.name(), e.getMessage());
            }
        }

        var bankToSavingExRatesMap = allSavingExchangeRates.stream()
                .collect(Collectors.groupingBy(ExchangeRate::getBankType, Collectors.toList()));
        overwriteBankExchangeRateSafely(bankToSavingExRatesMap);
        logger.info("Finish refreshing exchange rate.");
    }

    private void overwriteBankExchangeRateSafely(Map<BankType, List<ExchangeRate>> bankToExRatesMap) {
        var oldExRates = repository.findByBankTypeIn(bankToExRatesMap.keySet());
        var oldExRateIds = oldExRates.stream()
                .map(ExchangeRate::getId)
                .collect(Collectors.toList());

        var newExRates = bankToExRatesMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        repository.insert(newExRates);
        repository.deleteAllById(oldExRateIds);

        bankToExRatesMap.forEach((bank, rates) -> {
            var responses = ExchangeRateConverter.toResponses(rates);
            bankToExchangeRatesMap.put(bank, responses);
        });
    }

}

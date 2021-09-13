package com.vincent.forexledger.service;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import org.springframework.scheduling.annotation.Scheduled;

public class ExchangeRateService {
    private DownloadExchangeRateClient exchangeRateClient;
    private ExchangeRateRepository repository;

    public ExchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        this.exchangeRateClient = client;
        this.repository = repository;
    }

    @Scheduled(cron = "0 */30 * * * ?")
    private void refreshExchangeRateData() {
        // TODO
    }
}

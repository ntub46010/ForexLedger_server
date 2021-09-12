package com.vincent.forexledger.service;

import com.vincent.forexledger.repository.ExchangeRateRepository;

public class ExchangeRateService {
    private ExchangeRateRepository repository;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
    }
}

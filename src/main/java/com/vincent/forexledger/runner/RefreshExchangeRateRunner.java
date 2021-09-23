package com.vincent.forexledger.runner;

import com.vincent.forexledger.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RefreshExchangeRateRunner implements CommandLineRunner {

    @Value("${runner.exchangerate.refresh:false}")
    private boolean isNeededToRefresh;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Override
    public void run(String... args) {
        if (isNeededToRefresh) {
            exchangeRateService.refreshExchangeRateData();
        }
    }
}

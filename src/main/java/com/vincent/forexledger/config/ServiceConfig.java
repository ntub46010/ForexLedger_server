package com.vincent.forexledger.config;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.service.AppUserService;
import com.vincent.forexledger.service.ExchangeRateService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ServiceConfig {

    @Bean
    public AppUserService userService(AppUserRepository appUserRepository) {
        return new AppUserService(appUserRepository);
    }

    @Bean
    public ExchangeRateService exchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository) {
        return new ExchangeRateService(client, repository);
    }

    @Bean
    public DownloadExchangeRateClient downloadExchangeRateClient() {
        var restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))
                .build();
        return new DownloadExchangeRateClient(restTemplate);
    }
}

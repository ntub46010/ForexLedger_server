package com.vincent.forexledger.config;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.security.FirebaseTokenParser;
import com.vincent.forexledger.security.IAccessTokenParser;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.service.*;
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
    public ExchangeRateTable exchangeRateTable() {
        return new ExchangeRateTable();
    }

    @Bean
    public ExchangeRateService exchangeRateService(DownloadExchangeRateClient client, ExchangeRateRepository repository,
                                                   ExchangeRateTable exchangeRateTable) {
        return new ExchangeRateService(client, repository, exchangeRateTable);
    }

    @Bean
    public BookService bookService(UserIdentity userIdentity, BookRepository repository,
                                   ExchangeRateTable exchangeRateTable) {
        return new BookService(userIdentity, repository, exchangeRateTable);
    }

    @Bean
    public EntryService entryService(UserIdentity userIdentity, EntryRepository repository,
                                     BookService bookService) {
        return new EntryService(userIdentity, repository, bookService);
    }

    @Bean
    public RefreshBookMetaDataTask refreshBookMetaDataTask(
            BookRepository bookRepository, EntryRepository entryRepository) {
        return new RefreshBookMetaDataTask(bookRepository, entryRepository);
    }

    @Bean
    public BackupService backupService(UserIdentity userIdentity, BookService bookService,
                                       EntryRepository entryRepository, RefreshBookMetaDataTask task) {
        return new BackupService(userIdentity, bookService, entryRepository, task);
    }

    @Bean
    public DownloadExchangeRateClient downloadExchangeRateClient() {
        var restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))
                .build();
        return new DownloadExchangeRateClient(restTemplate);
    }

    @Bean
    public IAccessTokenParser firebaseTokenParser() {
        return new FirebaseTokenParser();
    }
}

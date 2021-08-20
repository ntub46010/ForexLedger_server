package com.vincent.forexledger.config;

import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.service.AppUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public AppUserService userService(AppUserRepository appUserRepository) {
        return new AppUserService(appUserRepository);
    }
}

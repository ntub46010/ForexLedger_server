package com.vincent.forexledger.config;

import com.vincent.forexledger.component.DummyAccessTokenParser;
import com.vincent.forexledger.security.IAccessTokenParser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class DummyComponentConfig {

    @Bean
    @Primary
    public IAccessTokenParser dummyAccessTokenParser() {
        return new DummyAccessTokenParser();
    }
}

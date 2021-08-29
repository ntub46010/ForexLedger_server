package com.vincent.forexledger.config;

import com.vincent.forexledger.filter.VerifyTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<VerifyTokenFilter> verifyTokenFilter() {
        var bean = new FilterRegistrationBean<VerifyTokenFilter>();
        bean.setFilter(new VerifyTokenFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}

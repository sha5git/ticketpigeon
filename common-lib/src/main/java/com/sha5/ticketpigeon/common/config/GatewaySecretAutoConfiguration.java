package com.sha5.ticketpigeon.common.config;

import com.sha5.ticketpigeon.common.filter.GatewaySecretFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configures the GatewaySecretFilter for any service that sets
 * the 'gateway.secret' property in its application.yml.
 *
 * Services that DON'T set this property (or set it to blank) will
 * not have the filter active — useful during early development.
 */
@Configuration
@ConditionalOnProperty(name = "gateway.secret")
public class GatewaySecretAutoConfiguration {

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Bean
    public FilterRegistrationBean<GatewaySecretFilter> gatewaySecretFilterRegistration() {
        FilterRegistrationBean<GatewaySecretFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GatewaySecretFilter(gatewaySecret));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("gatewaySecretFilter");
        return registration;
    }
}

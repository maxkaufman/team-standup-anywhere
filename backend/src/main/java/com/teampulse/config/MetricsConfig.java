package com.teampulse.config;

import com.teampulse.service.MetricsCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bridges the MetricsCollector singleton into the Spring context.
 * This lets us @Autowire it while maintaining the JVM-level singleton guarantee.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public MetricsCollector metricsCollector() {
        return MetricsCollector.getInstance();
    }
}

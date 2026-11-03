package com.example.medicine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    public static final String STOCK_EVENTS = "medicine-stock-events";
    public static final String PRICE_UPDATES = "medicine-price-updates";

    @Bean
    NewTopic stockEventsTopic() {
        return new NewTopic(STOCK_EVENTS, 3, (short) 1);
    }

    @Bean
    NewTopic priceUpdatesTopic() {
        return new NewTopic(PRICE_UPDATES, 1, (short) 1);
    }
}

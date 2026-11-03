package com.example.pharmacy.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    public static final String NOTIFICATIONS = "pharmacy-notifications";

    @Bean
    NewTopic notificationsTopic() {
        return new NewTopic(NOTIFICATIONS, 2, (short) 1);
    }
}

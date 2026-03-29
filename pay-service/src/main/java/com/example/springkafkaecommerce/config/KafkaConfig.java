package com.example.springkafkaecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        var backOff = new ExponentialBackOff(1_000L, 3.0);
        backOff.setMaxInterval(27_000L);
        backOff.setMaxElapsedTime(94_000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
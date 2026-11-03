package com.example.pharmacy.api;

import com.example.pharmacy.config.KafkaConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PharmacyController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/stock")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> receiveStock(@Valid @RequestBody StockRequest request) {
        Map<String, Object> event = Map.of("event", "STOCK_RECEIVED", "code", request.code(),
                "quantity", request.quantity(), "occurredAt", Instant.now().toString());
        kafkaTemplate.send(KafkaConfig.NOTIFICATIONS, request.code(), event);
        return event;
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> sell(@Valid @RequestBody SaleRequest request) {
        Map<String, Object> event = Map.of("event", "MEDICINE_SOLD", "code", request.code(),
                "quantity", request.quantity(), "occurredAt", Instant.now().toString());
        kafkaTemplate.send(KafkaConfig.NOTIFICATIONS, request.code(), event);
        return event;
    }

    public record StockRequest(@NotBlank String code, @Min(1) int quantity) {}
    public record SaleRequest(@NotBlank String code, @Min(1) int quantity) {}
}

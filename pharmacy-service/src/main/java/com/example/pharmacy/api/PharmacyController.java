package com.example.pharmacy.api;

import com.example.pharmacy.config.KafkaConfig;
import com.example.pharmacy.model.OrderEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    public ResponseEntity<Map<String, Object>> sell(@Valid @RequestBody SaleRequest request) {
        OrderEvent orderEvent = new OrderEvent(request.orderId(), request.medicineId(),
                request.quantity(), Instant.now());
        try {
            kafkaTemplate.send(KafkaConfig.MEDICINE_STOCK_EVENTS, orderEvent.medicineId(), orderEvent).get();
            return ResponseEntity.ok(Map.of("message", "Thanh toán thành công",
                    "orderId", orderEvent.orderId(), "event", orderEvent));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka publish interrupted", exception);
        } catch (ExecutionException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể gửi sự kiện đơn hàng lên Kafka", exception.getCause());
        }
    }

    public record StockRequest(@NotBlank String code, @Min(1) int quantity) {
    }

    public record SaleRequest(@NotBlank String orderId, @NotBlank String medicineId,
                              @Min(1) int quantity) {
    }
}

package com.example.medicine.api;

import com.example.medicine.config.KafkaConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, Medicine> medicines = new ConcurrentHashMap<>();

    public MedicineController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medicine create(@Valid @RequestBody MedicineRequest request) {
        Medicine medicine = new Medicine(request.code(), request.name(), request.price(), request.quantity());
        medicines.put(medicine.code(), medicine);
        publishStockEvent(medicine, "STOCK_RECEIVED");
        return medicine;
    }

    @PutMapping("/{code}/price")
    public Medicine updatePrice(@PathVariable String code, @Valid @RequestBody PriceRequest request) {
        Medicine medicine = medicine(code);
        Medicine updated = new Medicine(medicine.code(), medicine.name(), request.price(), medicine.quantity());
        medicines.put(code, updated);
        kafkaTemplate.send(KafkaConfig.PRICE_UPDATES, code,
                Map.of("event", "PRICE_UPDATED", "code", code, "price", request.price(),
                        "occurredAt", Instant.now().toString()));
        return updated;
    }

    @GetMapping("/{code}")
    public Medicine find(@PathVariable String code) {
        return medicine(code);
    }

    private Medicine medicine(String code) {
        Medicine medicine = medicines.get(code);
        if (medicine == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found: " + code);
        }
        return medicine;
    }

    private void publishStockEvent(Medicine medicine, String event) {
        kafkaTemplate.send(KafkaConfig.STOCK_EVENTS, medicine.code(),
                Map.of("event", event, "code", medicine.code(), "quantity", medicine.quantity(),
                        "occurredAt", Instant.now().toString()));
    }

    public record Medicine(String code, String name, BigDecimal price, int quantity) {}
    public record MedicineRequest(@NotBlank String code, @NotBlank String name,
                                  @NotNull @DecimalMin("0.0") BigDecimal price, @Min(0) int quantity) {}
    public record PriceRequest(@NotNull @DecimalMin("0.0") BigDecimal price) {}
}

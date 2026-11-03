# Pharmacy Kafka Microservices

Gồm hai service Spring Boot:

- `medicine-service` (`8081`): quản lý thuốc, phát sự kiện tồn kho vào `medicine-stock-events` (3 partitions) và cập nhật giá vào `medicine-price-updates` (1 partition).
- `pharmacy-service` (`8082`): xử lý nhập kho/bán lẻ và phát thông báo vào `pharmacy-notifications` (2 partitions).

## Chạy Kafka

```bash
docker compose up -d
docker compose logs -f kafka-init
```

Kiểm tra topic và partition:

```bash
docker exec -it kafka-pharmacy /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
docker exec -it kafka-pharmacy /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic medicine-stock-events
```

## Chạy service

```bash
./mvnw spring-boot:run -pl medicine-service
./mvnw spring-boot:run -pl pharmacy-service
```

Nếu chưa có Maven Wrapper, dùng `mvn spring-boot:run -pl <service>`.

Ví dụ tạo thuốc:

```bash
curl -X POST http://localhost:8081/api/medicines \
  -H 'Content-Type: application/json' \
  -d '{"code":"PARA-500","name":"Paracetamol","price":12000,"quantity":100}'
```
# micro_btvn10_01

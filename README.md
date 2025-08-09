# Midas Core – JPMorgan Chase Virtual Internship (Forage)

**Midas Core** is a Spring Boot microservice I built as part of the **JPMorgan Chase Advanced Software Engineering Virtual Experience** on Forage.  
It simulates a financial transaction system that ingests messages from **Kafka**, validates and persists them with **JPA/H2**, integrates with an external **Incentive API**, and exposes a REST endpoint to query user balances.

---

## Features

### Task 1 – Kafka Integration
- Added a `@KafkaListener` to consume JSON `Transaction` messages from a topic.
- Used Spring Kafka with `JsonDeserializer` to convert payloads to domain objects.

### Task 2 – Validation + Persistence (H2/JPA)
- Implemented business rules:
  - Sender & recipient must exist.
  - Sender must have sufficient balance.
- Persisted valid transactions in **H2** via **Spring Data JPA**.
- Created `TransactionRecord` entity with `@ManyToOne` links to `UserRecord`.

### Task 3 – Incentive API Integration
- Called an external **Incentive API** (`/incentive` on port **8080**) using `RestTemplate`.
- Stored the returned incentive amount and applied it to the **recipient** balance (not deducted from sender).

### Task 4 – End-to-End Processing
- Verified the full flow: Kafka → Validation → Incentive → Persistence → Updated balances.

### Task 5 – REST API for Balances
- Exposed `GET /balance?userId={id}` which returns a JSON `Balance` object.
- Returns `0` if the user doesn’t exist.
- App serves HTTP on **port 33400**.

---

## Tech Stack

- **Java 17**, **Maven**
- **Spring Boot 3**, **Spring Web**
- **Spring Data JPA**, **Hibernate**
- **H2** (in-memory DB for dev/tests)
- **Spring Kafka**
- **JUnit 5**

---

## Project Structure (high level)

```
src/
 └── main/
     ├── java/com/jpmc/midascore/
     │   ├── MidasCoreApplication.java         # Spring Boot entry point
     │   ├── KafkaTransactionListener.java     # Consumes Transaction events
     │   ├── BalanceController.java            # GET /balance
     │   ├── entity/
     │   │   ├── UserRecord.java               # JPA entity for users
     │   │   └── TransactionRecord.java        # JPA entity for persisted txns
     │   ├── repository/
     │   │   ├── UserRepository.java
     │   │   └── TransactionRecordRepository.java
     │   ├── foundation/
     │   │   └── Transaction.java              # DTO used by Kafka/messages
     │   └── Incentive.java                    # DTO for Incentive API response
     └── resources/
         ├── application.yml                   # Default config (tests/embedded kafka)
         └── application-local.yml             # Local overrides (e.g., disable Kafka)
```

---

## Configuration

### `application.yml` (default)
- Used by tests (e.g., `TaskTwoTests`, `TaskThreeTests`, `TaskFourTests`, `TaskFiveTests`) which boot **Embedded Kafka**.
- **Do not** disable Kafka here; tests rely on it.
- Server runs on port **33400**.

### `application-local.yml` (local/dev)
- Use to **disable Kafka** when you just want to run the REST API locally:
  ```yaml
  spring:
    kafka:
      enabled: false
  ```
- In `KafkaTransactionListener`:
  ```java
  @ConditionalOnProperty(name = "spring.kafka.enabled",
                         havingValue = "true",
                         matchIfMissing = true)
  ```
  so the listener is skipped when `spring.kafka.enabled=false`.

---

## Run Instructions

### 0) Build
```bash
mvn clean package
```

### 1) Start the Incentive API (required for tasks using incentives)
```bash
java -jar transactionincentiveapi.jar
```
Listens on **http://localhost:8080/incentive**.

Test it:
```bash
curl -X POST http://localhost:8080/incentive   -H "Content-Type: application/json"   -d '{"senderId":1,"recipientId":2,"amount":100.0}'
```

### 2) Run Midas Core
Without profile (Kafka enabled):
```bash
mvn spring-boot:run
```

With `local` profile (Kafka disabled):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3) Call the Balance API
```bash
curl "http://localhost:33400/balance?userId=8"
```

---

## Tests

Run all:
```bash
mvn test
```

Run Task 5 only:
```bash
mvn -Dtest=TaskFiveTests test
```

> **Note:** Incentive API must be running for tasks involving incentives (Task 4, Task 5).  
> Do **not** use `local` profile when running tests.

---

## Troubleshooting

- **Kafka bootstrap error**: Happens if `${spring.embedded.kafka.brokers}` is not resolved. Run tests (Embedded Kafka) or disable Kafka locally.
- **Port issue**: Ensure `server.port: 33400` in `application.yml`.
- **404 on `/balance`**: Ensure `BalanceController` is picked up by component scanning.

---

## What I Learned

- Event-driven design with Kafka.
- Transaction validation & persistence with JPA/Hibernate.
- External API integration using Spring.
- Profile-based configuration management.
- Building clean REST APIs in Spring Boot.

---

## Tags
`#Java` `#SpringBoot` `#Kafka` `#JPA` `#H2` `#REST` `#JPMC` `#Forage` `#BackendDevelopment`

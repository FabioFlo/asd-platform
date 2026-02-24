# ASD Platform — Microservices (v2)

**Java 25 LTS · Spring Boot 3.5 · Vertical Slice Architecture · Apache Kafka · PostgreSQL 17**

---

## What changed from v1

| Concern | v1 | v2 |
|---|---|---|
| Java version | 21 | **25 LTS** |
| Spring Boot | 3.3 | **3.5.x** |
| Architecture | Layered (controller/service/repo) | **Vertical Slice** |
| Result types | Exceptions + booleans | **Sealed interfaces** |
| Lombok | Everywhere | **JPA entities only** |
| DTOs/commands | Mixed | **Java records throughout** |
| MapStruct | Everywhere | **Only for complex mappings** |

---

## Architecture: Vertical Slice

Each feature is a self-contained package with everything it needs:

```
features/
  registerparticipant/
    RegisterParticipantCommand.java   ← record — immutable input
    RegisterParticipantResult.java    ← sealed — exhaustive outcomes
    RegisterParticipantHandler.java   ← all business logic
    RegisterParticipantController.java← thin HTTP adapter
    ComplianceClient.java             ← HTTP client (local to this feature)
```

Shared infrastructure (entities, repositories) lives in `shared/`:

```
shared/
  entity/    ← JPA entities (Lombok justified here)
  repository/← Spring Data interfaces
```

### Why Sealed Interfaces?

```java
// The compiler FORCES you to handle every outcome.
// No hidden exception paths. No forgotten null checks.
return switch (handler.handle(cmd)) {
    case RegisterParticipantResult.Registered r    -> ResponseEntity.status(201).body(...);
    case RegisterParticipantResult.Ineligible i    -> ResponseEntity.status(422).body(...);
    case RegisterParticipantResult.AlreadyRegistered a -> ResponseEntity.status(409).body(...);
    case RegisterParticipantResult.ComplianceUnavailable u -> ResponseEntity.status(503)
            .header("Retry-After", "120").body(...);   // ← FAIL-CLOSED
};
// Forgot a case? Compile error. Not a runtime NPE.
```

### Lombok Policy

```
✅ Use on @Entity classes (JPA needs no-arg constructor + mutable fields)
❌ Never on records, commands, results, responses, events
❌ Never use @Slf4j — use LoggerFactory.getLogger() directly
```

### MapStruct Policy

```
✅ Use when: updating existing entity (MappingTarget), 
             null-safe Map<String,Object> deep copy,
             multi-source aggregation
❌ Skip when: flat record → record mapping → use static factory instead
```

---

## Fail-Closed: How It Works

```
RegisterParticipantHandler.resolveEligibility()

  1. Check EligibilityCache (local DB)
     ├─ INELIGIBLE → return Ineligible (deny)
     └─ ELIGIBLE   → proceed (no network call needed)

  2. Cache miss → call Compliance HTTP sync
     ├─ eligible:true  → update cache, proceed
     ├─ eligible:false → update cache, return Ineligible (deny)
     └─ IOException    → return ComplianceUnavailable (DENY — fail-closed)
                               ↑
                               HTTP 503 + Retry-After: 120
                               Never silently allows through

  3. ComplianceEventConsumer (async Kafka)
     compliance.document.expired → markIneligible() in cache
     compliance.document.renewed → removeBlocker() from cache
     ↑ Keeps cache warm so sync calls are rare
```

---

## Module Layout

```
asd-platform/
├── pom.xml                              Root multi-module POM
│
├── shared/
│   ├── asd-events/                      Domain event records (pure Java 25)
│   │   └── it/asd/events/
│   │       ├── DomainEvent.java         Sealed marker interface
│   │       ├── EventEnvelope.java       Kafka transport wrapper (record)
│   │       ├── KafkaTopics.java         Topic name constants
│   │       └── compliance/competition/  Event records per domain
│   │
│   └── asd-common/                      Shared infra (Kafka, exceptions)
│       └── it/asd/common/
│           ├── kafka/KafkaConfig.java
│           ├── kafka/EventPublisher.java
│           └── exception/
│
├── services/
│   ├── compliance-service/
│   │   └── it/asd/compliance/
│   │       ├── ComplianceServiceApplication.java
│   │       ├── features/
│   │       │   ├── uploaddocument/      Command + Sealed(2) + Handler + Controller
│   │       │   ├── checkeligibility/    Query  + Sealed(3) + Handler + Controller
│   │       │   ├── renewdocument/       Command + Sealed(3) + Handler + Controller
│   │       │   └── expirycheck/         (no HTTP) Result + Handler + Scheduler
│   │       └── shared/
│   │           ├── entity/              DocumentEntity (Lombok), DocumentType, DocumentStatus
│   │           └── repository/          DocumentRepository
│   │
│   └── competition-service/
│       └── it/asd/competition/
│           ├── CompetitionServiceApplication.java
│           ├── features/
│           │   ├── registerparticipant/ Command + Sealed(4) + Handler + Controller
│           │   │                        + ComplianceClient (local)
│           │   ├── recordresult/        Command + Sealed(2) + Handler + Controller
│           │   │                        + ParticipationMapper (MapStruct)
│           │   └── eligibilitycache/    EligibilityCacheService + ComplianceEventConsumer
│           └── shared/
│               ├── entity/              EventParticipationEntity, EligibilityCacheEntity
│               └── repository/
│
└── infra/
    └── postgres/init.sql                Creates one DB per service
```

---

## Sealed Result Inventory

| Feature | Sealed Cases |
|---|---|
| `UploadDocumentResult` | `Success` · `InvalidDateRange` |
| `EligibilityResult` | `Eligible` · `ExpiringSoon` · `Ineligible` |
| `RenewDocumentResult` | `Renewed` · `NotFound` · `InvalidDateRange` |
| `ExpiryCheckResult` | `Summary` |
| `RegisterParticipantResult` | `Registered` · `Ineligible` · `AlreadyRegistered` · `ComplianceUnavailable` |
| `RecordResultResult` | `Recorded` · `NotFound` |

---

## Quick Start

```bash
# Start infra
docker-compose up -d postgres kafka kafka-ui

# Build (Java 25 required)
sdk install java 25-open   # if using SDKMAN
mvn clean install -DskipTests

# Run compliance locally
cd services/compliance-service && mvn spring-boot:run

# Run competition locally (needs compliance running)
cd services/competition-service && mvn spring-boot:run

# Full stack
docker-compose up -d
```

Kafka UI → http://localhost:9000

---

## Key API Endpoints

### Compliance (port 8086)
```
POST /compliance/persons/{id}/documents          Upload a document
GET  /compliance/persons/{id}/eligibility        Eligibility check (fail-closed source)
POST /compliance/documents/{id}/renew            Renew a document
```

### Competition (port 8085)
```
POST /competition/events/{id}/participants       Register a participant
PUT  /competition/participants/{id}/result       Record a result
```

---

## Adding a New Feature (vertical slice pattern)

```
1. Create features/myfeature/ package
2. Write MyFeatureCommand.java (record + Bean Validation)
3. Write MyFeatureResult.java (sealed interface with all outcomes)
4. Write MyFeatureHandler.java (@Component, @Transactional, no HTTP code)
5. Write MyFeatureController.java (switch on sealed result, no business logic)
6. Write MyFeatureHandlerTest.java (unit test with mocked repos)
```

## Adding a Sport Satellite Service

1. Create `services/chess-service/` following the same structure
2. Subscribe to `competition.participant.result_set` in a `@KafkaListener`
3. Extract `disciplina`, `personId`, `participationId` from the event
4. Store ELO delta / chess-specific data in your own schema, keyed on `participationId`
5. Never write back to competition-service

## Environment Variables

| Variable | Default | Service |
|---|---|---|
| `DB_HOST` | `localhost` | all |
| `DB_USER` | `asd` | all |
| `DB_PASS` | `asd` | all |
| `KAFKA_SERVERS` | `localhost:9092` | all |
| `COMPLIANCE_SERVICE_URL` | `http://localhost:8086` | competition |

# ASD Platform — Microservices (v2)

**Java 25 LTS · Spring Boot 3.5 · Vertical Slice Architecture · Apache Kafka · PostgreSQL 17**

---

## What changed from v1

| Concern       | v1                                | v2                            |
|---------------|-----------------------------------|-------------------------------|
| Java version  | 21                                | **25 LTS**                    |
| Spring Boot   | 3.3                               | **3.5.x**                     |
| Architecture  | Layered (controller/service/repo) | **Vertical Slice**            |
| Result types  | Exceptions + booleans             | **Sealed interfaces**         |
| Lombok        | Everywhere                        | **JPA entities only**         |
| DTOs/commands | Mixed                             | **Java records throughout**   |
| MapStruct     | Everywhere                        | **Only for complex mappings** |

---

## Architecture: Vertical Slice

Each feature is a self-contained package with everything it needs:

```
features/
  registerparticipant/
    RegisterParticipantCommand.java    ← record — immutable input
    RegisterParticipantResult.java     ← sealed — exhaustive outcomes
    RegisterParticipantHandler.java    ← all business logic
    RegisterParticipantController.java ← thin HTTP adapter
    ComplianceClient.java              ← HTTP client (local to this feature)
```

Shared infrastructure (entities, repositories) lives in `shared/`:

```
shared/
  entity/     ← JPA entities (Lombok justified here)
  repository/ ← Spring Data interfaces
  readmodel/  ← local cache entities updated by Kafka events
```

### Why Sealed Interfaces?

```java
// The compiler FORCES you to handle every outcome.
// No hidden exception paths. No forgotten null checks.
return switch (handler.handle(cmd)) {
    case RegisterParticipantResult.Registered r        -> ResponseEntity.status(201).body(...);
    case RegisterParticipantResult.Ineligible i        -> ResponseEntity.status(422).body(...);
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
✅ Use when: updating existing entity (@MappingTarget),
             null-safe Map<String,Object> deep copy,
             multi-source aggregation
❌ Skip when: flat record → record mapping → use static factory instead
```

---

## Services Overview

| Service               | Port | Owns                             | Depends on                          |
|-----------------------|------|----------------------------------|-------------------------------------|
| `registry-service`    | 8081 | ASD entity, Seasons              | —                                   |
| `identity-service`    | 8082 | Person, Qualifications           | —                                   |
| `membership-service`  | 8083 | Memberships, Groups, Enrollments | registry, identity (sync HTTP)      |
| `scheduling-service`  | 8084 | Venues, Rooms, Sessions          | registry; group cache via Kafka     |
| `competition-service` | 8085 | Event participations, Results    | compliance (sync HTTP, fail-closed) |
| `compliance-service`  | 8086 | Documents, Eligibility           | —                                   |
| `finance-service`     | 8087 | Payments, Fee rules              | purely event-driven, no sync calls  |

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
├── pom.xml                                  Root multi-module POM
│
├── shared/
│   ├── asd-events/                          Domain event records (pure Java 25)
│   │   └── it/asd/events/
│   │       ├── DomainEvent.java             Sealed marker interface
│   │       ├── EventEnvelope.java           Kafka transport wrapper (record)
│   │       ├── KafkaTopics.java             Topic name constants
│   │       └── registry/identity/           Event records per domain
│   │           membership/scheduling/
│   │           compliance/competition/
│   │           finance/
│   │
│   └── asd-common/                          Shared infra (Kafka, exceptions)
│       └── it/asd/common/
│           ├── kafka/KafkaConfig.java
│           ├── kafka/EventPublisher.java
│           └── exception/
│
├── services/
│   ├── registry-service/
│   │   └── it/asd/registry/
│   │       ├── features/
│   │       │   ├── createasd/               Command + Sealed(2) + Handler + Controller
│   │       │   ├── activateseason/          Command + Sealed(5) + Handler + Controller
│   │       │   └── getcurrentseason/        Query  + Sealed(2) + Handler + Controller
│   │       └── shared/
│   │           ├── entity/                  AsdEntity, SeasonEntity, enums
│   │           └── repository/
│   │
│   ├── identity-service/
│   │   └── it/asd/identity/
│   │       ├── features/
│   │       │   ├── registerperson/          Command + Sealed(3) + Handler + Controller
│   │       │   ├── updateperson/            Command + Sealed(3) + Handler + Controller
│   │       │   ├── addqualification/        Command + Sealed(2) + Handler + Controller
│   │       │   └── getperson/               Query  + Sealed(2) + Handler + Controller
│   │       └── shared/
│   │           ├── entity/                  PersonEntity, QualificationEntity, enums
│   │           └── repository/
│   │
│   ├── membership-service/
│   │   └── it/asd/membership/
│   │       ├── features/
│   │       │   ├── enrollmember/            Command + Sealed(5) + Handler + Controller
│   │       │   │                            + IdentityClient + RegistryClient (local)
│   │       │   ├── creategroup/             Command + Sealed(2) + Handler + Controller
│   │       │   └── addtogroup/              Command + Sealed(4) + Handler + Controller
│   │       └── shared/
│   │           ├── entity/                  MembershipEntity, GroupEntity,
│   │           │                            GroupEnrollmentEntity, RoleAssignmentEntity
│   │           ├── repository/
│   │           └── readmodel/               PersonCacheEntity
│   │
│   ├── scheduling-service/
│   │   └── it/asd/scheduling/
│   │       ├── features/
│   │       │   ├── createvenue/             Command + Sealed(2) + Handler + Controller
│   │       │   ├── addroom/                 Command + Sealed(3) + Handler + Controller
│   │       │   └── schedulesession/         Command + Sealed(6) + Handler + Controller
│   │       └── shared/
│   │           ├── entity/                  VenueEntity, RoomEntity, SessionEntity
│   │           ├── repository/
│   │           └── readmodel/               GroupCacheEntity
│   │
│   ├── competition-service/
│   │   └── it/asd/competition/
│   │       ├── features/
│   │       │   ├── registerparticipant/     Command + Sealed(4) + Handler + Controller
│   │       │   │                            + ComplianceClient (local)
│   │       │   ├── recordresult/            Command + Sealed(2) + Handler + Controller
│   │       │   │                            + ParticipationMapper (MapStruct)
│   │       │   └── eligibilitycache/        EligibilityCacheService + ComplianceEventConsumer
│   │       └── shared/
│   │           ├── entity/                  EventParticipationEntity, EligibilityCacheEntity
│   │           └── repository/
│   │
│   ├── compliance-service/
│   │   └── it/asd/compliance/
│   │       ├── features/
│   │       │   ├── uploaddocument/          Command + Sealed(2) + Handler + Controller
│   │       │   ├── checkeligibility/        Query  + Sealed(3) + Handler + Controller
│   │       │   ├── renewdocument/           Command + Sealed(3) + Handler + Controller
│   │       │   └── expirycheck/             (no HTTP) Result + Handler + Scheduler
│   │       └── shared/
│   │           ├── entity/                  DocumentEntity, DocumentType, DocumentStatus
│   │           └── repository/
│   │
│   └── finance-service/
│       └── it/asd/finance/
│           ├── features/
│           │   └── confirmpayment/          Command + Sealed(4) + Handler + Controller
│           ├── consumers/                   MembershipActivatedConsumer
│           │                                GroupEnrollmentAddedConsumer
│           │                                ParticipantRegisteredConsumer
│           ├── scheduler/                   OverdueScanJob
│           └── shared/
│               ├── entity/                  PaymentEntity, FeeRuleEntity, enums
│               └── repository/
│
└── infra/
    └── postgres/init.sql                    Creates one DB per service
```

---

## Sealed Result Inventory

| Feature                     | Service     | Sealed Cases                                                                                           |
|-----------------------------|-------------|--------------------------------------------------------------------------------------------------------|
| `CreateAsdResult`           | registry    | `Created` · `DuplicateCodiceFiscale`                                                                   |
| `ActivateSeasonResult`      | registry    | `Activated` · `AsdNotFound` · `AlreadyHasActiveSeason` · `InvalidDateRange` · `DuplicateCodice`        |
| `GetCurrentSeasonResult`    | registry    | `Found` · `NoActiveSeason`                                                                             |
| `RegisterPersonResult`      | identity    | `Registered` · `DuplicateCodiceFiscale` · `DuplicateEmail`                                             |
| `UpdatePersonResult`        | identity    | `Updated` · `NotFound` · `DuplicateEmail`                                                              |
| `AddQualificationResult`    | identity    | `Added` · `PersonNotFound`                                                                             |
| `EnrollMemberResult`        | membership  | `Enrolled` · `PersonNotFound` · `AsdNotFound` · `SeasonNotFound` · `AlreadyEnrolled`                   |
| `CreateGroupResult`         | membership  | `Created` · `DuplicateName`                                                                            |
| `AddToGroupResult`          | membership  | `Added` · `GroupNotFound` · `NotAMember` · `AlreadyInGroup`                                            |
| `CreateVenueResult`         | scheduling  | `Created` · `DuplicateName`                                                                            |
| `AddRoomResult`             | scheduling  | `Added` · `VenueNotFound` · `DuplicateName`                                                            |
| `ScheduleSessionResult`     | scheduling  | `Scheduled` · `VenueNotFound` · `RoomNotFound` · `GroupNotFound` · `TimeConflict` · `InvalidTimeRange` |
| `RegisterParticipantResult` | competition | `Registered` · `Ineligible` · `AlreadyRegistered` · `ComplianceUnavailable`                            |
| `RecordResultResult`        | competition | `Recorded` · `NotFound`                                                                                |
| `UploadDocumentResult`      | compliance  | `Success` · `InvalidDateRange`                                                                         |
| `EligibilityResult`         | compliance  | `Eligible` · `ExpiringSoon` · `Ineligible`                                                             |
| `RenewDocumentResult`       | compliance  | `Renewed` · `NotFound` · `InvalidDateRange`                                                            |
| `ExpiryCheckResult`         | compliance  | `Summary`                                                                                              |
| `ConfirmPaymentResult`      | finance     | `Confirmed` · `NotFound` · `AlreadyConfirmed` · `AlreadyCancelled`                                     |

---

## Kafka Event Flow

```
registry-service      → asd.created, season.activated, season.closed
identity-service      → person.created, person.updated, identity.qualification.added
membership-service    → membership.activated, membership.group.created,
                        group.enrollment.added
scheduling-service    → scheduling.session.scheduled
compliance-service    → compliance.document.created, compliance.document.expiring_soon,
                        compliance.document.expired, compliance.document.renewed,
                        compliance.person.eligible, compliance.person.ineligible
competition-service   → competition.participant.registered, competition.participant.result_set
finance-service       → finance.payment.created, finance.payment.confirmed,
                        finance.payment.overdue

Key async relationships:
  membership.group.created         → scheduling-service  (GroupCacheEntity)
  person.updated                   → membership-service  (PersonCacheEntity)
  compliance.document.expired/renewed → competition-service (EligibilityCacheEntity)
  membership.activated             → finance-service     (creates QUOTA_ASSOCIATIVA payment)
  group.enrollment.added           → finance-service     (creates QUOTA_CORSO payment)
  competition.participant.registered → finance-service   (creates ISCRIZIONE_GARA payment)
  finance.payment.overdue          → (membership-service can react to suspend memberships)
```

---

## Quick Start

```bash
# Start infra only
docker-compose up -d postgres kafka kafka-ui

# Build all (Java 25 required)
sdk install java 25-open   # if using SDKMAN
mvn clean install -DskipTests

# Run individual services locally (start in dependency order)
cd services/registry-service   && mvn spring-boot:run   # port 8081
cd services/identity-service   && mvn spring-boot:run   # port 8082
cd services/membership-service && mvn spring-boot:run   # port 8083
cd services/scheduling-service && mvn spring-boot:run   # port 8084
cd services/compliance-service && mvn spring-boot:run   # port 8086
cd services/competition-service && mvn spring-boot:run  # port 8085 (needs compliance)
cd services/finance-service    && mvn spring-boot:run   # port 8087

# Full stack via Docker
docker-compose up -d
```

Kafka UI → http://localhost:9000

---

## Building and Running on Windows

The `.sh` scripts don't run natively on Windows. Use one of the two options below depending on your setup.

### Option A — PowerShell (no WSL required)

#### Full build and start

Open PowerShell in the project root and run:

```powershell
# 1. Build all JARs with Maven
mvn clean package -DskipTests

# 2. Build Docker images (copies pre-built JARs only — no internet needed)
docker-compose build

# 3. Start the full stack
docker-compose up -d
```

#### Rebuild a single service after changes

```powershell
# Replace 'compliance' with the service you changed
$SERVICE = "compliance"

mvn package -pl "services/$SERVICE-service" -am -DskipTests -q
docker-compose build "$SERVICE-service"
docker-compose up -d "$SERVICE-service"

# Tail the logs
docker-compose logs -f "$SERVICE-service"
```

#### Start infra only (for local development)

```powershell
docker-compose up -d postgres kafka kafka-ui
```

#### Run a single service locally (no Docker)

```powershell
# Open a separate PowerShell window for each service, in dependency order:
Set-Location services\registry-service;   mvn spring-boot:run  # port 8081
Set-Location services\identity-service;   mvn spring-boot:run  # port 8082
Set-Location services\membership-service; mvn spring-boot:run  # port 8083
Set-Location services\scheduling-service; mvn spring-boot:run  # port 8084
Set-Location services\compliance-service; mvn spring-boot:run  # port 8086
Set-Location services\competition-service; mvn spring-boot:run # port 8085
Set-Location services\finance-service;    mvn spring-boot:run  # port 8087
```

#### Stop everything

```powershell
docker-compose down

# Stop and also delete volumes (wipes all databases — use with care)
docker-compose down -v
```

---

### Option B — WSL 2 (Windows Subsystem for Linux)

If you have WSL 2 installed, the `.sh` scripts work without any changes. Run them from your WSL terminal:

```bash
# Full build and start
./scripts/build-and-run.sh

# Rebuild a single service
./scripts/rebuild-service.sh compliance
./scripts/rebuild-service.sh competition
```

> **Tip:** Make sure Docker Desktop has **"Use the WSL 2 based engine"** enabled in Settings → General, and your distro
> is enabled under Settings → Resources → WSL Integration. This lets the WSL terminal share the same Docker daemon as
> Windows.

---

### Prerequisites (Windows)

| Tool               | Where to get it                                                                            | Required for        |
|--------------------|--------------------------------------------------------------------------------------------|---------------------|
| Java 25            | [Adoptium](https://adoptium.net) or `winget install EclipseAdoptium.Temurin.25.JDK`        | Maven build         |
| Maven 3.9+         | [maven.apache.org](https://maven.apache.org/download.cgi) or `winget install Apache.Maven` | Maven build         |
| Docker Desktop     | [docker.com](https://www.docker.com/products/docker-desktop/)                              | All Docker commands |
| WSL 2 *(optional)* | `wsl --install` in PowerShell as Administrator                                             | Option B only       |

---

## Key API Endpoints

### Registry (port 8081)

```
POST /registry/asd                              Create an ASD
POST /registry/asd/{asdId}/seasons             Activate a season
GET  /registry/asd/{asdId}/season/current      Get the active season
```

### Identity (port 8082)

```
POST  /identity/persons                         Register a person
PATCH /identity/persons/{personId}              Update person details
GET   /identity/persons/{personId}              Get person (called by membership-service)
POST  /identity/persons/{personId}/qualifications  Add a qualification
```

### Membership (port 8083)

```
POST /membership/members                        Enroll a member in an ASD
POST /membership/groups                         Create a group (team/course)
POST /membership/groups/{groupId}/members       Add a person to a group
```

### Scheduling (port 8084)

```
POST /scheduling/venues                         Create a venue
POST /scheduling/venues/{venueId}/rooms         Add a room to a venue
POST /scheduling/sessions                       Schedule a session
```

### Compliance (port 8086)

```
POST /compliance/persons/{id}/documents         Upload a document
GET  /compliance/persons/{id}/eligibility       Eligibility check (fail-closed source)
POST /compliance/documents/{id}/renew           Renew a document
```

### Competition (port 8085)

```
POST /competition/events/{id}/participants      Register a participant
PUT  /competition/participants/{id}/result      Record a result
```

### Finance (port 8087)

```
POST /finance/payments/{paymentId}/confirm      Confirm a payment
```

---

## Adding a New Feature (vertical slice pattern)

```
1. Create features/myfeature/ package
2. Write MyFeatureCommand.java     (record + Bean Validation)
3. Write MyFeatureResult.java      (sealed interface with all outcomes)
4. Write MyFeatureHandler.java     (@Component, @Transactional, no HTTP code)
5. Write MyFeatureController.java  (switch on sealed result, no business logic)
6. Write MyFeatureHandlerTest.java (unit test with mocked repos)
```

## Adding a Sport Satellite Service

```
1. Create services/chess-service/ following the same structure
2. Subscribe to competition.participant.result_set in a @KafkaListener
3. Extract disciplina, personId, participationId from the event
4. Store sport-specific data (ELO delta, swim splits, etc.) in your own schema
   keyed on participationId
5. Never write back to competition-service
```

---

## Environment Variables

| Variable                 | Default                 | Used by             |
|--------------------------|-------------------------|---------------------|
| `DB_HOST`                | `localhost`             | all services        |
| `DB_USER`                | `asd`                   | all services        |
| `DB_PASS`                | `asd`                   | all services        |
| `KAFKA_SERVERS`          | `localhost:9092`        | all services        |
| `COMPLIANCE_SERVICE_URL` | `http://localhost:8086` | competition-service |
| `IDENTITY_SERVICE_URL`   | `http://localhost:8082` | membership-service  |
| `REGISTRY_SERVICE_URL`   | `http://localhost:8081` | membership-service  |

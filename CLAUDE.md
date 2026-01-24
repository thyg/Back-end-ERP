# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Treasury API backend for the RT-ComOps ERP system. A reactive Spring Boot application managing bank accounts, transactions, checks, reconciliation, and audit trails.

## Build & Run Commands

```bash
# Compile
mvn clean compile

# Run (dev mode, port 8080)
mvn spring-boot:run

# Build JAR (skip tests since none exist yet)
mvn clean package -DskipTests

# Run tests
mvn clean test

# Docker build & run
docker build -t treasury-api:latest .
docker run -p 8080:8080 -e DB_HOST=localhost -e DB_PORT=5432 treasury-api:latest
```

## Prerequisites

- **Java 17** (pom.xml target; Dockerfile uses 21)
- **PostgreSQL** on port 5432 with database `treasury_db`, user `treasury_user`
- **Liquibase** runs automatically on startup (migrations in `src/main/resources/db/changelog/`)

## Architecture

**Fully reactive stack:** Spring WebFlux + R2DBC + Project Reactor. All endpoints return `Mono<T>` or `Flux<T>`.

### Package Structure (`com.rtcomops.treasury`)

| Package | Role |
|---------|------|
| `controller/` | REST endpoints (12 controllers), all under `/api/` prefix |
| `service/` | Business logic, reactive transactions |
| `repository/` | R2DBC reactive repositories with custom `@Query` methods |
| `entity/` | Database entities implementing `Persistable<UUID>` |
| `dto/request/` | Input DTOs with Jakarta validation annotations |
| `dto/response/` | Output DTOs |
| `mapper/` | Manual mappers (Entity <-> DTO conversion) |
| `config/` | CORS, R2DBC, Liquibase, OpenAPI configuration |
| `exception/` | `ResourceNotFoundException`, `BusinessException`, global handler |
| `enums/` | `AuditAction`, `AuditModule` |

### Entity Pattern (R2DBC)

All entities implement `Persistable<UUID>` with manual `isNew` tracking (R2DBC requirement):
```java
@Table(schema = "treasury", name = "table_name")
public class MyEntity implements Persistable<UUID> {
    @Transient
    @Builder.Default
    private boolean isNew = true;
}
```
When updating an entity, always call `entity.setNew(false)` before saving to prevent INSERT instead of UPDATE.

### Key Business Rules

- **Transaction lifecycle:** DRAFT -> VALIDATED -> (CANCELLED). Validation updates account balance. Cancellation reverses it.
- **Check lifecycle:** PENDING -> ISSUED/RECEIVED -> DEPOSITED -> CASHED. Cashing a check creates a linked VALIDATED transaction.
- **Transaction references:** Auto-generated as `CODE-YYYYMM-NNNN` (e.g., `VIREMENT-202501-0001`) via `TransactionSequenceRepository`.
- **Audit trail:** All create/update/delete/validate/cancel operations are logged with before/after state via `AuditLogService`.
- **Balance updates:** Only VALIDATED transactions affect account balance. Direction CREDIT adds, DEBIT subtracts.

### API Endpoints

All prefixed with `/api/`:
- `/api/banks`, `/api/bank-accounts`, `/api/bank-transactions`
- `/api/checks`, `/api/checkbooks`
- `/api/bank-statements`, `/api/statement-lines`
- `/api/reconciliation`, `/api/transaction-types`, `/api/account-types`
- `/api/audit-logs`
- Swagger UI: `/api/swagger-ui.html`

### Database

- Schema: `treasury` (all tables prefixed with this schema)
- Migrations: 10 Liquibase changelogs in `src/main/resources/db/changelog/`
- R2DBC pool: 5 initial, 20 max connections
- SSL required for PostgreSQL connection

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | treasury_db | Database name |
| `DB_USER` | treasury_user | Database username |
| `DB_PASSWORD` | treasury_password_2024 | Database password |

## Language

UI-facing strings, comments, and audit messages are in French. Code identifiers and documentation are in English.

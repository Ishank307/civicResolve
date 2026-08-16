# CivicResolve — Real-Time Civic Issue Resolution Platform

Real-time civic issue resolution engine with identity resolution, spatio-temporal correlation, temporal conflict handling, deterministic out-of-order replay, and versioned state management.

---

## Project Structure

```
Workdna/
├── backend/          # Spring Boot 3 + PostgreSQL (Java 21)
│   └── src/main/java/com/workdna/civic/
│       ├── api/          # Controllers & DTOs
│       ├── config/       # CORS, properties, DB configs
│       ├── domain/       # Entities, models & enums
│       ├── repository/   # Spring Data JPA Repositories
│       ├── service/      # Core logic
│       │   ├── identity/    # Identity Resolution & Spatio-Temporal correlation
│       │   ├── temporal/    # Temporal conflict detection
│       │   ├── resolution/  # Tie-breaking precedence & versioned state engine
│       │   ├── audit/       # Decision audit trail & state diffs
│       │   └── replay/      # Chronological deterministic batch replayer
│       └── util/         # Deterministic IssueId SHA-256 generator
├── frontend/         # React + TypeScript (Vite + Dark UI)
│   └── src/
│       ├── api/          # Typed API client
│       ├── components/   # Layout, ReportForm with Edge-Case presets
│       ├── pages/        # Real-time Dashboard, Replay Playground, Audit Trail
│       └── types/        # TypeScript DTO models
├── docker-compose.yml
└── prd.md
```

---

## Prerequisites

- Java 21+
- Maven 3.9+ (or use `./mvnw.cmd` / `./mvnw`)
- Node.js 20+
- PostgreSQL 16+ (or local native service / Docker)

---

## Quick Start

### 1. Start PostgreSQL Database
```bash
docker compose up -d
# Or use native local PostgreSQL running on port 5432 with db: civic_resolution, user: civic, pass: civic
```

### 2. Run Backend (Spring Boot)
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run

# Linux / macOS:
./mvnw spring-boot:run
```
API available at `http://localhost:8080`

### 3. Run Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
UI available at `http://localhost:5173`

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/reports` | Ingest a civic report (returns 201 on success, 409 if conflict pending) |
| POST | `/api/replay` | Replay a batch of reports deterministically |
| GET | `/api/issues` | Get all unified issues with latest version and resolution status |
| GET | `/api/reports` | Get recent ingested raw reports |
| GET | `/api/resolutions/{issueId}` | Get versioned resolution history and evidence for an issue |
| GET | `/api/audit/{issueId}` | Get decision audit log with `stateBefore` and `stateAfter` snapshots |

---

## Running Automated Tests

```bash
cd backend
.\mvnw.cmd test
```

Automated tests include:
1. **Identity Resolution**: Email linking, device fingerprinting, and spatio-temporal proximity within 10 minutes.
2. **Idempotency**: Repeated report ingestion returns identical response.
3. **Duplicate Detection**: Explicit duplicate flag handling (`DUPLICATE`).
4. **Tie-Breaking Precedence**: Mobile > Web, `isResolved: true` > `false`, latest timestamp.
5. **Temporal Conflict Replay**: Late-arriving out-of-order reports trigger state reconciliation.
6. **Batch Replay Determinism**: Verifies identical deterministic output across batches.

---

## Edge Case Test Fixtures

Test fixtures covering **6 PRD edge cases** are located at:
`backend/src/test/resources/fixtures/sample-reports.json`

Edge cases covered:
1. Initial report ingestion (New Issue)
2. Identity aliasing (different `userId`, same `email`)
3. Spatio-temporal identity correlation (< 10 min, same location)
4. Duplicate report detection
5. Out-of-order / late arrival report
6. Source precedence (Mobile > Web) and resolution override (`isResolved: true`)

---

## Features & Implementation Status

- [x] Complete identity resolution (email, device fingerprint, location + time window matching)
- [x] Temporal conflict detection and replay logic for late reports
- [x] Conflict tie-breaking rules (mobile > web, resolved > open, latest timestamp, HTTP 409 on tie)
- [x] Versioned state persistence with evidence tracking
- [x] Immutable decision audit trail with `stateBefore` & `stateAfter`
- [x] Complete query API layer (`GET /api/issues`, `GET /api/reports`, `GET /api/resolutions/{id}`, `GET /api/audit/{id}`)
- [x] Sample fixtures covering 6 edge cases
- [x] Automated integration test suite passing with 100% success
- [x] Interactive React GUI with live Dashboard, Replay Playground, and Audit Inspector

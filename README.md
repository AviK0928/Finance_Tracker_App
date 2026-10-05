# Finance Tracker

A personal finance tracker: an **Android app** (Kotlin, Jetpack Compose) backed by a **Spring Boot REST API** (Java, PostgreSQL).

> **Status:** under active refactoring toward a production-quality codebase. Progress and lessons learned are logged in [`engineering.md`](engineering.md).

## Tech stack

| Layer | Technologies |
|---|---|
| Android app | Kotlin 2.0, Jetpack Compose, Material 3, Hilt, Retrofit/OkHttp, Room, DataStore, Navigation Compose |
| Backend API | Java 17, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, WebSocket (STOMP), OpenPDF, Commons CSV |
| Database | PostgreSQL 16 |
| Dev environment | GitHub Codespaces / Dev Containers, Docker Compose |

## Repository layout

~~~
.
├── Finance_tracker_Backend/Finance_Tracker/    # Spring Boot API (Maven)
├── Finance_tracker_Frontend/Finance_Tracker/   # Android app (Gradle)
├── .devcontainer/                              # Codespaces / Dev Container setup
├── docker-compose.yml                          # Local PostgreSQL
├── .env.example                                # Environment variable template
└── engineering.md                              # Engineering log
~~~

## Quick start (GitHub Codespaces — recommended)

1. On GitHub: **Code → Codespaces → Create codespace on main**.
2. Wait for setup to finish (first time ~5 min). The dev container:
   - provides **JDK 17**,
   - installs the **Android SDK** (platform 35, build-tools 34/35),
   - creates **`.env`** from `.env.example` with a generated `JWT_SECRET`,
   - starts **PostgreSQL** via Docker Compose on every start.

### Run the backend

~~~bash
set -a; source .env; set +a
cd Finance_tracker_Backend/Finance_Tracker
./mvnw spring-boot:run
~~~

The API listens on `http://localhost:8080`. Smoke test: `curl http://localhost:8080/api/test` → `Hello, Anonymous`.

### Build the Android app

~~~bash
cd Finance_tracker_Frontend/Finance_Tracker
./gradlew :app:assembleDebug
~~~

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Local setup (without Codespaces)

Requirements: **JDK 17** (JDK 25+ is not supported by the current toolchain), Docker, and the Android SDK (platform 35) for the app.

~~~bash
cp .env.example .env                       # then set JWT_SECRET (openssl rand -base64 32)
docker compose up -d db                    # PostgreSQL on localhost:5432
set -a; source .env; set +a
(cd Finance_tracker_Backend/Finance_Tracker && ./mvnw spring-boot:run)
~~~

For the Android build, create `Finance_tracker_Frontend/Finance_Tracker/local.properties` containing `sdk.dir=/path/to/android-sdk`.

## Configuration

All secrets and environment-specific values come from environment variables (see [`.env.example`](.env.example)):

| Variable | Required | Purpose |
|---|---|---|
| `DB_URL` | no (defaults to local Postgres) | JDBC URL |
| `DB_USERNAME` | no (defaults to `postgres`) | DB user |
| `DB_PASSWORD` | **yes** | DB password |
| `JWT_SECRET` | **yes** | Base64 HMAC key (≥ 32 bytes) for signing JWTs |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | no | SMTP credentials for password-reset emails |

## Features

- Account registration and login (JWT), forgot/reset password
- Transactions: create, edit, delete, filter, paginate, export to PDF
- Budgets: create, edit, delete, filter, export to PDF
- Dashboard summary, monthly / category / trend reports
- Notifications (REST, with WebSocket push planned)
- Settings, data export/import (ZIP of CSVs)

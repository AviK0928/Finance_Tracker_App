#  Finance Tracker App

A full-stack personal finance tracking application built with:

- **Frontend:** Android (Kotlin, Jetpack Compose, Hilt, Material 3)
- **Backend:** Spring Boot (Java), REST APIs, JWT-based Auth

---

##  Project Structure

```
finance-tracker/
├── backend/        # Spring Boot backend
├── frontend/       # Android app using Jetpack Compose
└── README.md
```

---

##  Getting Started

###  Prerequisites

- **Java 17+**
- **Android Studio Hedgehog or later**
- **Gradle 8+**
- **Maven 3+**
- **Postgres**
- **Git**


---

## Backend Setup (Spring Boot)

```bash
cd backend
./gradlew bootRun
```

- Server starts at `http://localhost:8080`
- Make sure to configure DB credentials in `application.properties`
- Provides REST APIs for auth, transactions, budgets, reports, notifications, settings

---

## Frontend Setup (Android)

```bash
cd frontend
./gradlew assembleDebug
```

- Open the `frontend` folder in **Android Studio**
- Run on emulator or physical device
- Uses Jetpack Compose, Material 3, Navigation, Hilt DI

---

## Authentication

- Login/Register via REST endpoints
- JWT token is stored using `TokenManager`
- Logout/Delete account clears token and resets state

---

## Features

### Frontend (Jetpack Compose)
- [x] Auth screens (Login, Forgot, Reset)
- [x] Dashboard with 3 tabs
- [x] Transactions: create/edit/list
- [x] Budgets: create/edit/list
- [x] Reports: monthly, category, trend
- [x] Notifications (WebSocket)
- [x] Settings: export/import/sync/logout/delete

### Backend (Spring Boot)
- [x] JWT Authentication
- [x] CRUD for all finance models
- [x] PDF export, JSON import
- [x] Real-time notifications via WebSocket
- [x] Settings persistence

---

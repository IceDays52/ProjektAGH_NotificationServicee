# 🚀 NotifyDesk

Nowoczesny system do centralnego zarządzania powiadomieniami z wielu źródeł.

NotifyDesk integruje:

- 📧 Gmail (IMAP)
- 📅 Google Calendar
- ✅ Google Tasks

i prezentuje wszystkie informacje w jednym przejrzystym dashboardzie.

---

# ✨ Funkcjonalności

## 👤 Użytkownicy

- Rejestracja konta
- Logowanie
- Haszowanie haseł (BCrypt)
- Role USER / ADMIN

## 📧 Gmail

- Podłączanie wielu kont Gmail
- Synchronizacja wiadomości przez IMAP
- Przegląd wiadomości w dashboardzie
- Usuwanie kont Gmail

## 📅 Google Calendar

- OAuth2 Google
- Synchronizacja wydarzeń
- Pobieranie kalendarzy użytkownika

## ✅ Google Tasks

- Synchronizacja zadań
- Integracja z Google API

## 🔔 Powiadomienia

- Centralna lista powiadomień
- Filtrowanie po typie:
  - EMAIL
  - CALENDAR
  - TASK

## 🛡️ Panel Administratora

- Liczba użytkowników
- Liczba administratorów
- Liczba kont Gmail
- Liczba kont Google
- Liczba powiadomień
- Lista wszystkich użytkowników

---

# 🏗️ Architektura

## Backend

```text
Spring Boot
Spring Security
Spring Data JPA
Hibernate
PostgreSql
Google APIs
```

## Frontend

```text
React
TypeScript
Vite
CSS
```

---

# 📂 Struktura projektu

```text
src/main/java/org.example.notificationservice

├── admin
├── auth
├── config
├── controllers
├── dto
├── entity
├── google
├── mail
├── notification
├── repository
├── service
├── user
└── NotificationServiceApplication
```

---

# 🗄️ Baza danych

## Users

| Pole | Typ |
|--------|--------|
| id | BIGINT |
| email | VARCHAR |
| password | VARCHAR |
| role | VARCHAR |

---

## Mail Accounts

| Pole | Typ |
|--------|--------|
| id | BIGINT |
| gmail_address | VARCHAR |
| app_password | VARCHAR |
| active | BOOLEAN |
| user_id | BIGINT |

---

## Google Accounts

| Pole | Typ |
|--------|--------|
| id | BIGINT |
| google_email | VARCHAR |
| encrypted_access_token | TEXT |
| encrypted_refresh_token | TEXT |
| expires_at | TIMESTAMP |
| active | BOOLEAN |
| user_id | BIGINT |

---

## Notifications

| Pole | Typ |
|--------|--------|
| id | BIGINT |
| subject | VARCHAR |
| sender | VARCHAR |
| recipient | VARCHAR |
| content | TEXT |
| type | VARCHAR |
| status | VARCHAR |
| received_at | TIMESTAMP |

---

# 🔑 Role

## USER

Może:

- podłączać Gmail
- podłączać Google
- synchronizować dane
- przeglądać własne powiadomienia

---

## ADMIN

Dodatkowo może:

- przeglądać statystyki systemowe
- zarządzać użytkownikami
- monitorować aktywność systemu

---

# ⚙️ Instalacja

## Backend

```bash
git clone https://github.com/twoje-repo/notifydesk.git

cd notifydesk

mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

# 🔐 Konfiguracja Google OAuth

W pliku:

```properties
application.properties
```

uzupełnij:

```properties
google.client.id=YOUR_CLIENT_ID
google.client.secret=YOUR_CLIENT_SECRET
google.redirect.uri=http://localhost:8080/api/google/oauth/callback
```

---

# 📸 Widoki aplikacji

## Dashboard użytkownika

- Powiadomienia
- Gmail
- Google Calendar
- Google Tasks

## Dashboard administratora

- Statystyki systemowe
- Zarządzanie użytkownikami
- Monitoring systemu

---

# 👨‍💻 Autor

**Sebastian Ciula**

Projekt wykonany jako aplikacja do centralizacji powiadomień i integracji usług Google.

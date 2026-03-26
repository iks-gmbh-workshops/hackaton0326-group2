# Hackaton-B – Gruppen- und Aktivitaeten-App

Webanwendung zur Organisation von Gruppen und gemeinsamen Aktivitaeten. Nutzer koennen Gruppen erstellen, Mitglieder einladen und Aktivitaeten planen, zu denen Teilnehmer zu- oder absagen koennen.

---

## Schnellstart

### Voraussetzungen

- Docker und Docker Compose
- (Optional) Node.js 20+, Java 21+ fuer lokale Entwicklung

### Mit Docker Compose starten

```bash
# .env-Datei anlegen
cp .env.example .env

# Services starten
docker compose up --build
```

Danach erreichbar unter:

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| PostgreSQL | localhost:5433 |

### Lokale Entwicklung (ohne Docker)

**Datenbank starten:**

```bash
docker compose up db
```

**Backend starten:**

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend starten:**

```bash
cd frontend
npm install
npm run dev
```

---

## Projektstruktur

```
hackaton0326-group2/
├── backend/             Spring Boot 3 REST-API
├── frontend/            Vue 3 SPA (Vite + TailwindCSS)
├── docker-compose.yml   3 Services (db, backend, frontend)
├── .env.example         Umgebungsvariablen-Vorlage
├── Architecture.md      Technische Architektur
├── docs.md              API-Dokumentation
├── SECURITY.md          Sicherheitskonzept
└── planning.md          Urspruengliche Planungsdokumentation
```

---

## Tech-Stack

| Schicht | Technologien |
|---------|-------------|
| **Frontend** | Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios, TailwindCSS |
| **Backend** | Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, Spring Mail, Flyway |
| **Datenbank** | PostgreSQL (Port 5433) |
| **Infrastruktur** | Docker Compose, Multi-Stage Dockerfiles, Nginx |

---

## Features

### Authentifizierung
- Registrierung mit AGB-Zustimmung
- Login mit JWT-Token

### Gruppen
- Gruppen erstellen, bearbeiten, loeschen
- Mitglieder per E-Mail einladen (mit Einladungstoken)
- Einladungen annehmen/ablehnen
- Gruppenrollen: VERWALTER und MITGLIED

### Aktivitaeten
- Aktivitaeten erstellen und mehreren Gruppen zuordnen
- Zu-/Absage-System fuer Teilnehmer
- Uebersicht ueber kommende Aktivitaeten

### Profilverwaltung
- Profildaten anzeigen und bearbeiten
- Passwort aendern
- Account dauerhaft loeschen (DSGVO)

### Administration (nur ADMIN)
- Alle User, Gruppen und Aktivitaeten verwalten
- User-Rollen aendern
- Ressourcen loeschen

---

## Umgebungsvariablen

Konfiguriert ueber `.env` im Projektstammverzeichnis:

| Variable | Beschreibung | Standard |
|----------|-------------|----------|
| `DB_USERNAME` | PostgreSQL-Benutzername | `hackaton-b` |
| `DB_PASSWORD` | PostgreSQL-Passwort | `hackaton-b` |
| `JWT_SECRET` | Base64-kodierter HMAC-Key (mind. 256 Bit) | – |
| `MAIL_PASSWORD` | SMTP-Passwort fuer E-Mail-Versand | – |

---

## Dokumentation

- **[docs.md](docs.md)** – Vollstaendige API-Referenz mit Request/Response-Beispielen
- **[Architecture.md](Architecture.md)** – Systemarchitektur, Datenmodell, Tech-Stack, Deployment
- **[SECURITY.md](SECURITY.md)** – Sicherheitskonzept, Rollen, DSGVO, bekannte Einschraenkungen
- **[MAINTENANCE.md](MAINTENANCE.md)** – Betriebs-, Wartungs- und Release-Runbook
- **[CONTRIBUTING.md](CONTRIBUTING.md)** – Beitrag, Branching, Commit- und PR-Standards
- **[planning.md](planning.md)** – Urspruengliche Projektplanung und Aufgabenaufteilung
- **[backend/README.md](backend/README.md)** – Backend-spezifische Entwicklung und Betrieb
- **[frontend/README.md](frontend/README.md)** – Frontend-spezifische Dokumentation

---

## Betrieb und Wartung

### Services pruefen

```bash
docker compose ps
```

### Logs anzeigen

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

### Services neu starten

```bash
docker compose restart backend frontend
```

### Datenbank zuruecksetzen (Achtung: Datenverlust)

```bash
docker compose down -v
docker compose up --build
```

Weitere Betriebsdetails: siehe `MAINTENANCE.md`.

---

## Troubleshooting

### Backend startet nicht (JWT-Fehler)

- Ursache: `JWT_SECRET` fehlt oder ist ungueltig.
- Loesung: in `.env` einen gueltigen Base64-String mit mindestens 256 Bit setzen.

### Frontend kann API nicht erreichen

- Pruefen, ob `backend` laeuft: `docker compose ps`
- Bei lokalem Frontend-Start muss `VITE_API_URL` auf `http://localhost:8080/api` zeigen.
- Bei Docker-Frontend wird `/api` ueber Nginx an `backend:8080` weitergeleitet.

### Mailversand funktioniert nicht

- `MAIL_PASSWORD` in `.env` setzen.
- SMTP-Konfiguration in `backend/src/main/resources/application.yml` pruefen.
- Backend-Logs auf `Failed to send invitation email` kontrollieren.

---

## Beitrag leisten

### Empfohlener Ablauf

1. Branch erstellen (`feature/...`, `fix/...`, `docs/...`)
2. Aenderungen lokal testen
3. Relevante Tests ausfuehren
4. Dokumentation aktualisieren, falls Verhalten/Setup geaendert wurde

### Mindest-Checks vor Merge

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm run lint && npm run build
```

---

## Tests

### Backend

```bash
cd backend
./mvnw test
```

Unit-Tests fuer alle Service-Klassen:
- `AuthServiceTest`
- `UserServiceTest`
- `GroupServiceTest`
- `ActivityServiceTest`
- `AdminServiceTest`

---

## Build (Production)

### Backend

```bash
cd backend
./mvnw clean package -DskipTests
```

### Frontend

```bash
cd frontend
npm run build
```

---

## Lizenz

Internes Hackaton-Projekt – iks GmbH.

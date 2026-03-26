# Architektur

Technische Architekturdokumentation fuer die Gruppen- und Aktivitaeten-App (Hackaton-B).

---

## Systemueberblick

```
┌──────────────────┐       ┌───────────────────┐       ┌────────────────┐
│  Vue 3 SPA       │──────>│  Spring Boot API  │──────>│  PostgreSQL    │
│  (Vite + Nginx)  │ REST  │  (JWT Auth)       │  JPA  │  (Port 5433)   │
│  Port 5173       │       │  Port 8080        │       │                │
└──────────────────┘       └───────────────────┘       └────────────────┘
                     Docker Compose (3 Services)
```

---

## Tech-Stack

### Frontend

| Technologie | Zweck |
|-------------|-------|
| Vue 3 | UI-Framework (Composition API) |
| Vite | Build-Tool / Dev-Server |
| TypeScript | Typsicherheit |
| Vue Router | Client-Side Routing mit Auth-Guard |
| Pinia | State Management (installiert) |
| Axios | HTTP-Client mit JWT-Interceptor |
| TailwindCSS | Utility-First CSS |
| Nginx | Production-Webserver + API-Reverse-Proxy |

### Backend

| Technologie | Zweck |
|-------------|-------|
| Spring Boot 3.4 | Application Framework |
| Spring Security | JWT-basierte Authentifizierung |
| Spring Data JPA | Datenbankzugriff (Hibernate) |
| Spring Mail | E-Mail-Versand (Einladungen) |
| Jakarta Validation | Request-Validierung |
| Flyway | Datenbank-Migrationen |
| Lombok | Boilerplate-Reduktion |
| jjwt 0.12 | JWT-Erzeugung und -Validierung |
| Java 21 | Runtime |

### Infrastruktur

| Technologie | Zweck |
|-------------|-------|
| Docker Compose | Orchestrierung (db, backend, frontend) |
| PostgreSQL | Relationale Datenbank |
| Multi-Stage Dockerfiles | Optimierte Container-Images |

---

## Datenmodell

```
┌──────────────┐
│    users      │
├──────────────┤
│ id (PK)       │
│ email (UQ)    │
│ password_hash  │
│ display_name   │
│ role           │  USER | ADMIN
│ agb_accepted   │
│ created_at     │
│ updated_at     │
└──────┬───────┘
       │ 1:n
       v
┌──────────────────┐         ┌──────────────┐
│  group_members    │────────>│   groups      │
├──────────────────┤  n:1    ├──────────────┤
│ user_id (FK)      │         │ id (PK)       │
│ group_id (FK)     │         │ name          │
│ invited_by (FK)   │         │ description   │
│ role              │         │ created_by(FK)│
│ status            │         │ invite_token  │
│ joined_at         │         │ created_at    │
└──────────────────┘         └──────┬───────┘
                                    │ m:n
                          ┌─────────┴─────────┐
                          v                   v
                 ┌──────────────────┐  ┌──────────────────┐
                 │ group_activities  │  │   activities      │
                 ├──────────────────┤  ├──────────────────┤
                 │ group_id (FK)    │  │ id (PK)           │
                 │ activity_id (FK) │  │ title             │
                 └──────────────────┘  │ description       │
                                       │ location          │
┌────────────────────────┐             │ start_time        │
│ activity_participants   │             │ end_time          │
├────────────────────────┤             │ created_by (FK)   │
│ activity_id (FK)        │<────────────│ created_at        │
│ user_id (FK)            │             └──────────────────┘
│ status                  │  PENDING | ACCEPTED | DECLINED
│ responded_at            │
└────────────────────────┘
```

### Beziehungen

- **User <-> Group**: Many-to-Many ueber `group_members` (mit Rolle und Status)
- **Group <-> Activity**: Many-to-Many ueber `group_activities`
- **User <-> Activity**: Many-to-Many ueber `activity_participants` (Zu-/Absage)

### DB-Migrationen (Flyway)

- `V1__initial_schema.sql` – Alle Tabellen, Constraints und Indexes
- `V2__add_invited_by_to_group_members.sql` – Erweitert `group_members` um `invited_by`

---

## Rollenkonzept (zweistufig)

### Globale Rollen (`users.role`)

| Rolle | Rechte |
|-------|--------|
| `USER` | Standard-Nutzer, eigene Ressourcen verwalten |
| `ADMIN` | Voller Zugriff auf alle Ressourcen |

### Gruppenrollen (`group_members.role`)

| Rolle | Rechte |
|-------|--------|
| `VERWALTER` | Gruppe bearbeiten, Mitglieder einladen/entfernen, Aktivitaeten verwalten |
| `MITGLIED` | Aktivitaeten sehen, zu-/absagen |

### Berechtigungsmatrix

```
Aktion                        | ADMIN | VERWALTER | MITGLIED
─────────────────────────────-┼───────┼───────────┼─────────
Gruppe bearbeiten/loeschen     |  Ja   |  Ja       |  Nein
Mitglieder einladen/entfernen  |  Ja   |  Ja       |  Nein
Aktivitaet erstellen           |  Ja   |  Ja       |  Nein
Aktivitaet bearbeiten/loeschen |  Ja   |  Ja(eigen) |  Nein
Zu-/Absage                     |  Ja   |  Ja       |  Ja
Alle Ressourcen verwalten      |  Ja   |  Nein     |  Nein
User-Rollen aendern            |  Ja   |  Nein     |  Nein
```

---

## Security-Architektur

### Dreistufiger Security-Check

1. **JWT-Filter** (`JwtAuthenticationFilter`) – Ist der User authentifiziert?
2. **Global-Role-Check** (`SecurityConfig`) – `/api/admin/**` nur fuer `ROLE_ADMIN`
3. **Group-Role-Check** (Service-Layer) – Ist der User VERWALTER in der betroffenen Gruppe?

### Authentifizierungsfluss

```
Client                    Backend
  │                         │
  │── POST /api/auth/login ─>│
  │                         │── Credentials pruefen (BCrypt)
  │                         │── JWT generieren (HMAC-SHA, 24h)
  │<── { token, role } ─────│
  │                         │
  │── GET /api/... ─────────>│
  │   Authorization: Bearer  │── JwtAuthenticationFilter
  │                         │── Token validieren
  │                         │── SecurityContext setzen
  │<── Response ─────────────│
```

---

## Projektstruktur

### Backend

```
backend/src/main/java/com/hackaton/
├── config/          SecurityConfig, CORS
├── controller/      REST-Controller (Auth, User, Group, Activity, Admin)
├── dto/             Request/Response-Objekte
├── exception/       Custom Exceptions + GlobalExceptionHandler
├── model/           JPA-Entities + Enums
├── repository/      Spring Data JPA Repositories
├── security/        JWT-Utils, Filter, CustomUserDetails
└── service/         Business-Logik
```

### Frontend

```
frontend/src/
├── api/             API-Services (authService, groupService, activityService, adminService)
├── assets/          Globale Styles (TailwindCSS)
├── components/      Wiederverwendbare Komponenten (Dashboard-Cards, Form-Fields)
├── composables/     Vue Composables (useDashboardGroups, useDashboardActivities)
├── router/          Vue Router mit Auth-Guard
├── stores/          Pinia Stores
├── views/           Seiten-Komponenten (Login, Register, Dashboard, Groups, Activities, Profile, Admin)
├── App.vue          Root-Komponente mit Navigation
└── main.ts          Einstiegspunkt
```

---

## Deployment

### Docker Compose

Drei Services werden orchestriert:

| Service | Image | Port |
|---------|-------|------|
| `db` | postgres:16-alpine | 5433 |
| `backend` | Multi-Stage (Eclipse Temurin 21) | 8080 |
| `frontend` | Multi-Stage (Node 20 + Nginx) | 5173 |

### Nginx-Konfiguration

- Statische Dateien aus dem Vue-Build
- `try_files` fuer Vue Router History Mode
- Reverse-Proxy `/api/` -> `http://backend:8080/api/`

### Umgebungsvariablen

Konfiguriert ueber `.env` (siehe `.env.example`):

| Variable | Beschreibung |
|----------|-------------|
| `DB_USERNAME` | PostgreSQL-Benutzername |
| `DB_PASSWORD` | PostgreSQL-Passwort |
| `JWT_SECRET` | Base64-kodierter HMAC-Key (mind. 256 Bit) |
| `MAIL_PASSWORD` | SMTP-Passwort fuer E-Mail-Versand |

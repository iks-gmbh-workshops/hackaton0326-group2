#  Gruppen- & Aktivitäten-App

## Phase 1 – MVP (Kernfunktionalität)

Eine Webanwendung zur Organisation von Gruppen und gemeinsamen Aktivitäten. Nutzer können Gruppen erstellen, Mitglieder einladen und Aktivitäten planen, zu denen Teilnehmer zu- oder absagen können.

---

## 1. Architektur-Überblick

```
┌──────────────┐       ┌──────────────────┐       ┌────────────┐
│  Vue 3 SPA   │──────▶│  Spring Boot API  │──────▶│ PostgreSQL │
│  (Vite)      │ REST  │  (JWT Auth)       │  JPA  │            │
└──────────────┘       └──────────────────┘       └────────────┘
        └── Nginx ──┘          └── Port 8080 ──┘
                    Docker Compose
```

- **Frontend**: Vue 3 + Vite, Vue Router, Pinia (State), Axios (HTTP), TailwindCSS
- **Backend**: Spring Boot 3, Spring Security (JWT), Spring Data JPA, Spring Mail
- **DB**: PostgreSQL 16
- **Deployment**: Docker Compose mit 3 Services (`frontend`, `backend`, `db`)

---

## 2. Datenmodell

```
┌──────────────────┐
│      User         │
├──────────────────┤
│ id (PK)           │
│ email (unique)    │
│ password_hash     │
│ display_name      │
│ role (USER/ADMIN) │  ◀── globale Rolle: ADMIN kann alles verwalten
│ agb_accepted      │
│ created_at        │
│ updated_at        │
└──────────────────┘
        │
        │ 1:n
        ▼
┌──────────────────┐         ┌──────────────┐
│   GroupMember     │────────▶│    Group      │
├──────────────────┤  n:1    ├──────────────┤
│ user_id (FK)      │         │ id (PK)       │
│ group_id (FK)     │         │ name          │
│ role (VERWALTER/  │         │ description   │
│       MITGLIED)   │         │ created_by(FK)│
│ status (PENDING/  │         │ created_at    │
│   ACTIVE/LEFT)    │         │ invite_token  │
│ joined_at         │         └──────────────┘
└──────────────────┘                │
                                    │ m:n
                          ┌─────────┴─────────┐
                          ▼                   ▼
                 ┌──────────────────┐  ┌──────────────────┐
                 │ GroupActivity     │  │    Activity       │
                 │ (Join-Table)     │  ├──────────────────┤
                 ├──────────────────┤  │ id (PK)           │
                 │ group_id (FK)    │  │ title             │
                 │ activity_id (FK) │  │ description       │
                 └──────────────────┘  │ location          │
                                       │ start_time        │
                                       │ end_time          │
┌──────────────────────┐               │ created_by (FK)   │
│ ActivityParticipant   │               │ created_at        │
├──────────────────────┤               └──────────────────┘
│ activity_id (FK)      │◀──────────────────────┘
│ user_id (FK)          │
│ status (ACCEPTED/     │
│   DECLINED/PENDING)   │
│ responded_at          │
└──────────────────────┘
```

### Beziehungen

- **User ↔ Group**: Many-to-Many über `GroupMember` (mit Rolle und Status)
- **Group ↔ Activity**: Many-to-Many über `GroupActivity` (eine Aktivität kann mehreren Gruppen zugeordnet sein)
- **User ↔ Activity**: Many-to-Many über `ActivityParticipant` (mit Zu-/Absage-Status)

### Rollenkonzept (zweistufig)

| Ebene | Rolle | Rechte |
|-------|-------|--------|
| **Global (User.role)** | `ADMIN` | Kann alle Gruppen, Aktivitäten und User verwalten |
| **Global (User.role)** | `USER` | Standard-Nutzer, nur eigene Ressourcen |
| **Gruppe (GroupMember.role)** | `VERWALTER` | Gruppe bearbeiten, Mitglieder einladen/entfernen, Aktivitäten verwalten |
| **Gruppe (GroupMember.role)** | `MITGLIED` | Aktivitäten sehen, zu-/absagen |

---

## 3. API-Endpunkte (REST)

### Auth

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| POST | `/api/auth/register` | Registrierung (inkl. AGB-Zustimmung) |
| POST | `/api/auth/login` | Login → JWT |
| POST | `/api/auth/logout` | Token invalidieren |

### User / Profil

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| GET | `/api/users/me` | Eigenes Profil |
| PUT | `/api/users/me` | Profil bearbeiten |
| DELETE | `/api/users/me` | Account + Daten löschen |

### Gruppen

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| POST | `/api/groups` | Gruppe erstellen |
| GET | `/api/groups` | Meine Gruppen |
| GET | `/api/groups/{id}` | Gruppendetails |
| PUT | `/api/groups/{id}` | Gruppe bearbeiten |
| DELETE | `/api/groups/{id}` | Gruppe löschen |
| POST | `/api/groups/{id}/invite` | Mitglied einladen (E-Mail/Token) |
| POST | `/api/groups/{id}/join` | Einladung annehmen (per Token) |
| GET | `/api/groups/{id}/members` | Mitgliederliste |
| DELETE | `/api/groups/{id}/members/{uid}` | Mitglied entfernen / austreten |
| GET | `/api/groups/{gid}/activities` | Aktivitäten einer Gruppe |

### Aktivitäten

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| POST | `/api/activities` | Aktivität erstellen (mit Liste von `groupIds`) |
| GET | `/api/activities/upcoming` | Alle kommenden Aktivitäten des Users |
| GET | `/api/activities/{id}` | Aktivität-Details inkl. zugeordnete Gruppen |
| PUT | `/api/activities/{id}` | Aktivität bearbeiten |
| DELETE | `/api/activities/{id}` | Aktivität löschen |
| POST | `/api/activities/{id}/groups` | Gruppen zur Aktivität hinzufügen |
| DELETE | `/api/activities/{id}/groups/{gid}` | Gruppe von Aktivität entfernen |
| POST | `/api/activities/{id}/respond` | Zu-/Absage (`{status: ACCEPTED\|DECLINED}`) |
| GET | `/api/activities/{id}/participants` | Teilnehmerliste mit Status |

### Admin-Bereich (nur für User.role == ADMIN)

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| GET | `/api/admin/users` | Alle User auflisten |
| PUT | `/api/admin/users/{id}/role` | User-Rolle ändern (USER ↔ ADMIN) |
| DELETE | `/api/admin/users/{id}` | User löschen |
| GET | `/api/admin/groups` | Alle Gruppen auflisten |
| DELETE | `/api/admin/groups/{id}` | Beliebige Gruppe löschen |
| GET | `/api/admin/activities` | Alle Aktivitäten auflisten |
| DELETE | `/api/admin/activities/{id}` | Beliebige Aktivität löschen |

---

## 4. Berechtigungsmatrix

```
Aktion                        │ ADMIN │ VERWALTER │ MITGLIED
──────────────────────────────┼───────┼───────────┼─────────
Gruppe bearbeiten/löschen      │  ✓    │  ✓        │  ✗
Mitglieder einladen/entfernen  │  ✓    │  ✓        │  ✗
Aktivität erstellen            │  ✓    │  ✓        │  ✗
Aktivität bearbeiten/löschen   │  ✓    │  ✓ (eigene)│  ✗
Zu-/Absage                     │  ✓    │  ✓        │  ✓
Alle Ressourcen verwalten      │  ✓    │  ✗        │  ✗
User-Rollen ändern             │  ✓    │  ✗        │  ✗
```

Dreistufiger Security-Check im Backend:
1. **JWT-Filter** → Ist der User authentifiziert?
2. **Global-Role-Check** → Ist `User.role == ADMIN`? → Alles erlaubt
3. **Group-Role-Check** → Ist der User `VERWALTER` in der betroffenen Gruppe? → Gruppenspezifische Verwaltung erlaubt

---

## 5. Frontend – Seitenstruktur (Vue Router)

| Route | View | Beschreibung |
|-------|------|-------------|
| `/login` | LoginView | Login-Formular |
| `/register` | RegisterView | Registrierung + AGB |
| `/dashboard` | DashboardView | Übersicht: Gruppen + kommende Aktivitäten |
| `/profile` | ProfileView | Profil bearbeiten, Account löschen |
| `/groups/:id` | GroupDetailView | Gruppe mit Mitgliedern + Aktivitäten |
| `/groups/:id/activities/new` | ActivityFormView | Aktivität anlegen |
| `/activities/:id` | ActivityDetailView | Details + Zu-/Absage |
| `/invite/:token` | InviteAcceptView | Einladung annehmen |
| `/admin` | AdminPanelView | User-/Gruppen-/Aktivitäten-Verwaltung (nur ADMIN) |

---

## 6. Aufgabenaufteilung

### Backend (Spring Boot)

| # | Aufgabe | Abhängig von |
|---|---------|-------------|
| B1 | **Projekt-Setup**: Spring Boot Starter, Gradle/Maven, Docker Compose, PostgreSQL-Config | – |
| B2 | **Entity-Klassen + DB-Migration**: User, Group, GroupMember, GroupActivity, Activity, ActivityParticipant (Flyway/Liquibase) | B1 |
| B3 | **Auth: Registrierung + Login** (Spring Security, JWT, Password-Encoding, AGB-Flag, User.role) | B2 |
| B4 | **Profilverwaltung**: GET/PUT/DELETE `/users/me` inkl. Datenlöschung (DSGVO) | B3 |
| B5 | **Gruppen-CRUD**: Erstellen, Lesen, Bearbeiten, Löschen (nur Verwalter/Admin) | B3 |
| B6 | **Einladungssystem**: E-Mail-Versand (Spring Mail) + Token-basierter Join | B5 |
| B7 | **Mitgliederverwaltung**: Liste, Entfernen, Austreten, Rollen (Verwalter/Mitglied) | B5 |
| B8 | **Aktivitäten-CRUD**: Erstellen mit `groupIds`-Liste, Lesen, Bearbeiten, Löschen | B5 |
| B9 | **Gruppen-Aktivitäten-Zuordnung**: Gruppen zu Aktivitäten hinzufügen/entfernen | B8 |
| B10 | **Zu-/Absage-System**: Respond-Endpoint + Teilnehmerliste mit Status | B8 |
| B11 | **Upcoming-Activities-Endpoint**: Aggregation über alle Gruppen des Users | B8 |
| B12 | **Admin-Endpoints**: `/api/admin/**` mit `@PreAuthorize("hasRole('ADMIN')")` | B3 |
| B13 | **Globale Fehlerbehandlung + Validierung** (ControllerAdvice, Jakarta Validation) | B1 |
| B14 | **Docker-Image** (Multi-Stage Dockerfile für Backend) | B1 |

### Frontend (Vue 3)

| # | Aufgabe | Abhängig von |
|---|---------|-------------|
| F1 | **Projekt-Setup**: Vite + Vue 3, Router, Pinia, Axios, TailwindCSS, Dockerfile | – |
| F2 | **Auth-Pages**: Login + Registrierung (mit AGB-Checkbox), JWT im Store + Axios-Interceptor | F1 |
| F3 | **Layout**: Navbar, Auth-Guard (Route Protection), Responsive Shell | F2 |
| F4 | **Dashboard**: Übersicht mit Gruppenliste + kommende Aktivitäten | F3 |
| F5 | **Profil-Page**: Anzeige, Bearbeitung, Account-Löschung mit Bestätigung | F3 |
| F6 | **Gruppen-Detailseite**: Mitgliederliste (mit Rollen), Aktivitätenliste, Einladungs-Button | F4 |
| F7 | **Einladungs-Flow**: Einladen-Dialog (E-Mail) + Token-Accept-Seite | F6 |
| F8 | **Aktivität erstellen/bearbeiten**: Formular mit Multi-Select für Gruppen-Zuordnung | F6 |
| F9 | **Aktivität-Detailseite**: Infos + Zu-/Absage-Buttons + Teilnehmerliste | F6 |
| F10 | **Admin-Panel**: User-Liste, Gruppen-/Aktivitäten-Übersicht, Lösch-Funktionen (nur ADMIN) | F3 |
| F11 | **Docker-Image** (Nginx + Vue Build) | F1 |

### Infrastruktur

| # | Aufgabe |
|---|---------|
| I1 | **Docker Compose**: 3 Services (`db`, `backend`, `frontend`), Netzwerk, Volumes, Env-Variablen |
| I2 | **CORS-Konfiguration** im Backend |
| I3 | **Umgebungsvariablen**: DB-Credentials, JWT-Secret, Mail-Config |

---

## 7. Empfohlene Arbeitsreihenfolge (parallel)

```
Woche 1:  B1 + F1 + I1  →  B2 + B3  |  F2 + F3
Woche 2:  B5 + B6 + B13 |  F4 + F5 + F6
Woche 3:  B7 + B8 + B9 + B10  |  F7 + F8 + F9
Woche 4:  B11 + B12 + B14     |  F10 + F11 + Integration + Bugfixes
```

Backend und Frontend können **parallel** entwickelt werden, wenn man sich auf die API-Contracts (Endpunkte + DTOs) vorher einigt. Ein gemeinsames OpenAPI-Dokument oder ein abgestimmter API-Contract ist dafür der Schlüssel.

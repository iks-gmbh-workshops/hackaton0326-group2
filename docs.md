# API-Dokumentation

Vollstaendige REST-API-Referenz fuer das Hackaton-B Backend (Spring Boot 3).

Basis-URL: `http://localhost:8080/api`

---

## Authentifizierung

Alle Endpunkte ausser `/api/auth/**` erfordern einen gueltigen JWT-Token im Header:

```
Authorization: Bearer <token>
```

---

## Auth

| Methode | Pfad | Beschreibung | Auth |
|---------|------|--------------|------|
| POST | `/api/auth/register` | Neuen Account erstellen | Nein |
| POST | `/api/auth/login` | Login, gibt JWT zurueck | Nein |

### POST `/api/auth/register`

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "geheim123",
  "displayName": "Max Mustermann",
  "agbAccepted": true
}
```

**Response (201):**

```json
{
  "token": "eyJhbGciOi...",
  "email": "user@example.com",
  "displayName": "Max Mustermann",
  "role": "USER"
}
```

### POST `/api/auth/login`

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "geheim123"
}
```

**Response (200):** Gleiches Format wie Register-Response.

---

## User / Profil

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/api/users/me` | Eigenes Profil abrufen |
| PUT | `/api/users/me` | Profil bearbeiten |
| DELETE | `/api/users/me` | Account dauerhaft loeschen (DSGVO) |

### PUT `/api/users/me`

**Request Body:**

```json
{
  "email": "neu@example.com",
  "displayName": "Neuer Name",
  "password": "neuesPasswort"
}
```

Alle Felder sind optional. Nur uebergebene Felder werden aktualisiert.

---

## Gruppen

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/api/groups` | Gruppe erstellen (Ersteller wird automatisch VERWALTER) |
| GET | `/api/groups` | Eigene Gruppen auflisten |
| GET | `/api/groups/{id}` | Gruppendetails |
| PUT | `/api/groups/{id}` | Gruppe bearbeiten (nur VERWALTER/ADMIN) |
| DELETE | `/api/groups/{id}` | Gruppe loeschen (nur VERWALTER/ADMIN) |

### POST `/api/groups`

```json
{
  "name": "Wandergruppe",
  "description": "Gemeinsam wandern gehen"
}
```

**Response (201):**

```json
{
  "id": 1,
  "name": "Wandergruppe",
  "description": "Gemeinsam wandern gehen",
  "createdById": 1,
  "createdByName": "Max Mustermann",
  "createdAt": "2025-03-26T10:00:00",
  "inviteToken": "abc-123-def",
  "memberCount": 1
}
```

---

## Mitgliederverwaltung

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/api/groups/{id}/members` | Mitgliederliste |
| POST | `/api/groups/{id}/invite` | Mitglied per E-Mail einladen (nur VERWALTER/ADMIN) |
| POST | `/api/groups/{id}/join?token=...` | Gruppe per Einladungstoken beitreten |
| PUT | `/api/groups/{id}/members/{uid}/role` | Mitgliedsrolle aendern (VERWALTER/MITGLIED) |
| DELETE | `/api/groups/{id}/members/me` | Gruppe selbst verlassen |
| DELETE | `/api/groups/{id}/members/{uid}` | Mitglied entfernen (nur VERWALTER/ADMIN) |

### POST `/api/groups/{id}/invite`

```json
{
  "email": "einladung@example.com"
}
```

---

## Einladungen

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/api/groups/invitations` | Eigene ausstehende Einladungen |
| POST | `/api/groups/invitations/{id}/accept` | Einladung annehmen |
| POST | `/api/groups/invitations/{id}/decline` | Einladung ablehnen |

---

## Aktivitaeten

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/api/activities` | Aktivitaet erstellen (nur VERWALTER/ADMIN) |
| GET | `/api/activities/upcoming` | Kommende Aktivitaeten des Users |
| GET | `/api/activities/{id}` | Aktivitaet-Details |
| PUT | `/api/activities/{id}` | Aktivitaet bearbeiten |
| DELETE | `/api/activities/{id}` | Aktivitaet loeschen |
| GET | `/api/groups/{id}/activities` | Aktivitaeten einer Gruppe |

### POST `/api/activities`

```json
{
  "title": "Wanderung Eifel",
  "description": "Tageswanderung",
  "location": "Eifel",
  "startTime": "2025-04-10T09:00:00",
  "endTime": "2025-04-10T17:00:00",
  "groupIds": [1, 2]
}
```

---

## Gruppen-Aktivitaeten-Zuordnung

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/api/activities/{id}/groups` | Gruppen zur Aktivitaet hinzufuegen |
| DELETE | `/api/activities/{id}/groups/{gid}` | Gruppe von Aktivitaet entfernen |

### POST `/api/activities/{id}/groups`

```json
{
  "groupIds": [3, 4]
}
```

---

## Zu-/Absage (Teilnahme)

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/api/activities/{id}/respond` | Zu- oder Absage senden |
| GET | `/api/activities/{id}/participants` | Teilnehmerliste mit Status |

### POST `/api/activities/{id}/respond`

```json
{
  "status": "ACCEPTED"
}
```

Moegliche Werte: `ACCEPTED`, `DECLINED`

---

## Admin-Bereich

Alle Admin-Endpunkte erfordern `User.role == ADMIN`.

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/api/admin/users` | Alle User auflisten |
| PUT | `/api/admin/users/{id}/role` | User-Rolle aendern |
| DELETE | `/api/admin/users/{id}` | User loeschen |
| GET | `/api/admin/groups` | Alle Gruppen auflisten |
| DELETE | `/api/admin/groups/{id}` | Gruppe loeschen |
| GET | `/api/admin/activities` | Alle Aktivitaeten auflisten |
| DELETE | `/api/admin/activities/{id}` | Aktivitaet loeschen |

### PUT `/api/admin/users/{id}/role`

```json
{
  "role": "ADMIN"
}
```

Moegliche Werte: `USER`, `ADMIN`

---

## Fehler-Responses

Alle Fehler folgen dem gleichen Format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Beschreibung des Fehlers",
  "fieldErrors": {
    "email": "darf nicht leer sein"
  }
}
```

| HTTP-Status | Bedeutung |
|-------------|-----------|
| 400 | Ungueltige Eingabe / Validierungsfehler |
| 401 | Nicht authentifiziert / Falsche Credentials |
| 403 | Keine Berechtigung |
| 404 | Ressource nicht gefunden |
| 409 | Konflikt (z.B. E-Mail bereits vergeben) |
| 500 | Interner Serverfehler |

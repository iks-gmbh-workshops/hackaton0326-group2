# Sicherheitskonzept

Dokumentation der Sicherheitsmassnahmen und -architektur fuer Hackaton-B.

---

## Authentifizierung

### JWT (JSON Web Token)

- **Algorithmus:** HMAC-SHA (symmetrisch) ueber `jjwt 0.12`
- **Gueltigkeitsdauer:** 24 Stunden (`jwt.expiration-ms: 86400000`)
- **Secret:** Base64-kodiert, mindestens 256 Bit, konfiguriert ueber Umgebungsvariable `JWT_SECRET`
- **Transport:** `Authorization: Bearer <token>` Header
- **Speicherung (Frontend):** `localStorage` unter dem Key `auth_token`

### Passwort-Hashing

- **Algorithmus:** BCrypt (via `BCryptPasswordEncoder`)
- Passwoerter werden **niemals** im Klartext gespeichert

### Session-Management

- **Stateless:** Keine Server-Sessions (`SessionCreationPolicy.STATELESS`)
- **CSRF:** Deaktiviert (nicht noetig bei JWT + SPA)

---

## Autorisierung

### Dreistufiger Security-Check

| Stufe | Komponente | Pruefung |
|-------|-----------|----------|
| 1 | `JwtAuthenticationFilter` | Ist der JWT gueltig und nicht abgelaufen? |
| 2 | `SecurityConfig` (URL-basiert) | `/api/auth/**` offen, `/api/admin/**` nur `ROLE_ADMIN`, Rest authentifiziert |
| 3 | Service-Layer (programmatisch) | Gruppenrolle pruefen (VERWALTER/MITGLIED) |

### Rollen

- **Globale Rollen** (`User.role`): `USER`, `ADMIN`
- **Gruppenrollen** (`GroupMember.role`): `VERWALTER`, `MITGLIED`
- Admin-Endpunkte sind ueber `hasRole("ADMIN")` in der Security-Config geschuetzt

### Zugriffskontrollen im Service-Layer

- `checkMemberOrAdmin()` – User muss aktives Mitglied oder ADMIN sein
- `checkVerwalterOrAdmin()` – User muss VERWALTER in der Gruppe oder ADMIN sein
- `checkActivityEditPermission()` – Ersteller, VERWALTER oder ADMIN
- `checkActivityAccess()` – Mitglied in mindestens einer zugeordneten Gruppe

---

## Datenschutz (DSGVO)

### Account-Loeschung

- `DELETE /api/users/me` loescht den Account und alle zugehoerigen Daten
- Kaskaden-Loeschung ueber JPA `CascadeType.ALL` + `orphanRemoval`
- Betrifft: Gruppenmitgliedschaften, Aktivitaetsteilnahmen
- Frontend zeigt eine Sicherheitsabfrage vor dem Loeschen

### AGB-Zustimmung

- Registrierung erfordert `agbAccepted: true`
- Backend validiert das Flag und verweigert die Registrierung ohne Zustimmung

---

## CORS

Konfiguriert in `SecurityConfig`:

- **Erlaubte Origins:** `http://localhost:5173`, `http://localhost:3000`
- **Erlaubte Methoden:** GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Erlaubte Header:** Alle (`*`)
- **Credentials:** Erlaubt

---

## E-Mail-Sicherheit

- SMTP ueber SSL (Port 465) zu `mail.iks-gmbh.com`
- Passwort ueber Umgebungsvariable `MAIL_PASSWORD` (nicht im Code)
- Absender: `no-reply@iks-gmbh.com`

---

## Eingabevalidierung

- **Jakarta Validation** (`@Valid`) auf allen Request-Bodies in Controllern
- **`GlobalExceptionHandler`** (`@RestControllerAdvice`) faengt Validierungsfehler ab und gibt strukturierte Fehlermeldungen zurueck
- Custom Exceptions: `BadRequestException`, `ForbiddenException`, `ResourceNotFoundException`, `ConflictException`

---

## Bekannte Einschraenkungen

| Thema | Status | Beschreibung |
|-------|--------|-------------|
| JWT-Blacklist | Nicht implementiert | Kein Logout-Endpoint, Token bleibt bis Ablauf gueltig |
| Rate Limiting | Nicht implementiert | Kein Schutz gegen Brute-Force auf Login |
| JWT-Claims | Minimal | Token enthaelt nur `subject` (E-Mail), keine Rolle |
| HTTPS | Nicht konfiguriert | Nur HTTP in der aktuellen Konfiguration |
| CORS Origins | Hardcoded | Nur localhost, muss fuer Production angepasst werden |

---

## Empfehlungen fuer Production

1. **JWT-Secret** durch einen sicheren, zufaellig generierten Key ersetzen
2. **HTTPS** aktivieren (TLS-Terminierung via Reverse-Proxy oder Spring Boot)
3. **CORS-Origins** auf die tatsaechliche Domain einschraenken
4. **Rate Limiting** fuer Auth-Endpunkte einbauen
5. **Rolle in JWT-Claims** aufnehmen, um localStorage-Manipulation zu verhindern
6. **E-Mail Invite-Link** ueber Umgebungsvariable konfigurierbar machen

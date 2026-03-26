# Maintenance Runbook

Dieses Dokument beschreibt den Betrieb, die Wartung und den Release-Ablauf fuer das Projekt.

## 1) Betrieb

### 1.1 Services starten

```bash
docker compose up --build -d
```

### 1.2 Service-Status

```bash
docker compose ps
```

### 1.3 Logs

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

## 2) Health-Check

- Frontend erreichbar: `http://localhost:5173`
- Backend erreichbar: `http://localhost:8080/api`
- Datenbank-Port offen: `localhost:5433`

Optionaler API-Schnelltest:

```bash
curl -i http://localhost:8080/api/auth/login
```

(Hinweis: erwartet `405` oder `400`, aber nicht `5xx`.)

## 3) Datenbank und Migrationen

- Migrationen liegen unter `backend/src/main/resources/db/migration`
- Flyway laeuft automatisch beim Backend-Start

### Datenbank zuruecksetzen (lokal)

```bash
docker compose down -v
docker compose up --build
```

Achtung: Der Befehl loescht das Docker-Volume `postgres-data`.

## 4) Konfiguration

Relevante Variablen in `.env`:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `MAIL_PASSWORD`

Empfehlungen:

- In Production niemals Default-Werte verwenden
- `JWT_SECRET` als starken Base64-Key setzen (mind. 256 Bit)
- Secrets nicht in Git committen

## 5) Release-Checkliste

Vor jedem Release:

1. Pull/Branch synchronisieren
2. Backend-Tests ausfuehren
3. Frontend-Lint + Build ausfuehren
4. Dokumentation anpassen (falls API/Setup geaendert)
5. Docker-Build lokal pruefen

Empfohlene Kommandos:

```bash
# Backend
cd backend
./mvnw test

# Frontend
cd ../frontend
npm run lint
npm run build

# Gesamtstack
cd ..
docker compose up --build
```

## 6) Hauefige Stoerungen

### Backend startet nicht wegen JWT

- `JWT_SECRET` pruefen
- Logs pruefen: `docker compose logs backend`

### Frontend zeigt keine Daten

- API-URL und Proxy pruefen (`frontend/nginx.conf`)
- Backend-Status pruefen (`docker compose ps`)

### Einladungs-E-Mails kommen nicht an

- `MAIL_PASSWORD` gesetzt?
- SMTP-Erreichbarkeit/Firewall pruefen
- Backend-Logs auf Mail-Fehler pruefen

## 7) Verantwortlichkeiten

- API-Verhalten: Backend-Team
- UI/UX und Routing: Frontend-Team
- Compose, Laufzeit, Env-Handling: DevOps/Projektverantwortliche

Bei Architektur-/Sicherheitsaenderungen auch `Architecture.md` und `SECURITY.md` aktualisieren.

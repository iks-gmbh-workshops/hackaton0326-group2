# Backend (Spring Boot)

Backend-Service fuer Benutzer-, Gruppen-, Aktivitaets- und Admin-Funktionen.

## Stack

- Java 21
- Spring Boot 3.4
- Spring Security (JWT)
- Spring Data JPA + Hibernate
- Flyway
- PostgreSQL
- Spring Mail

## Start (lokal)

Voraussetzungen:

- Java 21
- PostgreSQL auf Port `5433` (oder via Docker Compose)

Start:

```bash
./mvnw spring-boot:run
```

## Build und Tests

```bash
# Unit Tests
./mvnw test

# Jar bauen
./mvnw clean package -DskipTests
```

## Konfiguration

Zentrale Konfiguration: `src/main/resources/application.yml`

Wichtige Properties:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret`
- `jwt.expiration-ms`
- `spring.mail.*`

Die sensiblen Werte kommen aus Umgebungsvariablen (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_PASSWORD`).

## Datenbank

Migrationen:

- `V1__initial_schema.sql`
- `V2__add_invited_by_to_group_members.sql`

Flyway wird beim Start automatisch ausgefuehrt.

## Security

### Endpunkt-Schutz

- `/api/auth/**` ist offen
- `/api/admin/**` nur fuer `ROLE_ADMIN`
- Alle anderen `/api/**` Endpunkte sind authentifiziert

### JWT-Flow

1. Login ueber `/api/auth/login`
2. Backend liefert JWT
3. Token wird bei Requests als `Bearer` gesendet
4. `JwtAuthenticationFilter` validiert Token und setzt SecurityContext

## Modulstruktur

```text
src/main/java/com/hackaton/
|-- config/       Security- und Infrastrukturkonfiguration
|-- controller/   REST-Endpunkte
|-- dto/          Request/Response DTOs
|-- exception/    Fehlerklassen + GlobalExceptionHandler
|-- model/        JPA Entities + Enums
|-- repository/   Spring Data Repositories
|-- security/     JWT- und UserDetails-Implementierung
`-- service/      Business-Logik
```

## Debugging-Tipps

- Security-Probleme: zuerst `JwtAuthenticationFilter` und `SecurityConfig` pruefen
- Rechte-Probleme: Service-Checks (`checkMemberOrAdmin`, `checkVerwalterOrAdmin`) pruefen
- DB-Fehler: Flyway-Status + SQL-Constraints pruefen
- Mail-Fehler: `MAIL_PASSWORD` und SMTP-Konfiguration pruefen

## API-Referenz

Die vollstaendige API-Dokumentation liegt im Root unter `../docs.md`.

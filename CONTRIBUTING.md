# Contributing Guide

Danke fuers Mitwirken an Hackaton-B.
Dieses Dokument beschreibt den empfohlenen Entwicklungs- und Review-Prozess.

## 1) Branching

Nutze kurze, aussagekraeftige Branchnamen:

- `feature/<kurze-beschreibung>`
- `fix/<kurze-beschreibung>`
- `docs/<kurze-beschreibung>`
- `chore/<kurze-beschreibung>`

Beispiele:

- `feature/group-invite-token-flow`
- `fix/navbar-admin-link`
- `docs/maintenance-runbook`

## 2) Commits

Empfohlene Commit-Namenskonvention:

- `feat: ...`
- `fix: ...`
- `docs: ...`
- `refactor: ...`
- `test: ...`
- `chore: ...`

Beispiele:

- `feat: add invite landing route for token join`
- `fix: remove duplicate admin navigation link`
- `docs: add backend maintenance runbook`

## 3) Coding-Regeln

- Kleine, fokussierte Aenderungen statt grosser Sammel-PRs
- Keine unnoetigen Refactorings in fachfremden Dateien
- Bestehende Namenskonventionen und Projektstruktur einhalten
- Keine Secrets hardcoden (JWT, Passwoerter, Tokens)
- Bei API-Aenderungen immer passende Doku aktualisieren (`docs.md`, `README.md`)

## 4) Lokale Checks vor Push

### Backend

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
```

### Optional E2E

```bash
cd frontend
npm run test:e2e
```

## 5) Pull-Request-Checkliste

Jeder PR sollte enthalten:

- [ ] Problem und Ziel kurz beschrieben
- [ ] Aenderungen mit Fokus auf Verhalten/Scope erklaert
- [ ] Tests/Checks lokal ausgefuehrt
- [ ] Doku aktualisiert (falls relevant)
- [ ] Keine sensiblen Daten im Diff

## 6) PR-Beschreibung (Template)

```md
## Ziel

## Was wurde geaendert?

## Wie wurde getestet?

## Risiken / Hinweise

## Screenshots (falls UI)
```

## 7) Wann Review blockiert werden sollte

Ein PR sollte nicht gemerged werden, wenn:

- Builds oder Tests fehlschlagen
- Sicherheitsrelevante Aenderungen ohne Security-Review enthalten sind
- API-Verhalten geaendert wurde, aber Dokumentation fehlt
- Scope zu gross/unklar ist und in kleinere PRs aufgeteilt werden sollte

## 8) Dokumentationspflicht

Wenn du an einem dieser Bereiche arbeitest, update die passende Datei:

- API-Verhalten -> `docs.md`
- Architektur/Flows -> `Architecture.md`
- Security-relevante Aenderungen -> `SECURITY.md`
- Betrieb/Release -> `MAINTENANCE.md`
- Setup/Quickstart -> `README.md`

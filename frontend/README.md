# Frontend - Vue 3 + Vite

Single-Page-Application fuer Hackaton B (Gruppen, Aktivitaeten, Profilverwaltung).

## Features

- Dashboard mit:
  - Meine Gruppen
  - Gruppeneinladungen (annehmen/ablehnen)
  - Meine Aktivitaeten
  - Ausstehende Aktivitaeten (zusagen/absagen)
- Profilverwaltung:
  - Eigene Profildaten anzeigen und bearbeiten
  - Passwortaenderung mit Passwort-Bestaetigung
  - Profil dauerhaft loeschen mit Sicherheitsabfrage
- Responsive UI (Desktop, Tablet, Mobile)
- TypeScript + Vue 3 Composition API

## Technologie-Stack

- Vue 3
- Vite
- TypeScript
- TailwindCSS
- Vue Router
- Axios

## Installation und Start

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
npm run preview
```

## E2E Tests mit Playwright

Einmalige Einrichtung:

```bash
npm install
npx playwright install
```

Tests ausfuehren:

```bash
npm run test:e2e
```

Nuetzliche Modi:

```bash
npm run test:e2e:ui
npm run test:e2e:headed
npm run test:e2e:debug
```

## Projektstruktur

```text
frontend/
|-- src/
|   |-- api/              # API Services
|   |-- assets/           # Styles, Bilder
|   |-- components/       # Wiederverwendbare Komponenten
|   |-- router/           # Routen
|   |-- views/            # Seiten (Dashboard, Profile, ...)
|   |-- App.vue           # Root-Komponente
|   `-- main.ts           # Einstiegspunkt
|-- public/
|-- index.html
|-- vite.config.ts
|-- tsconfig.json
|-- tailwind.config.js
`-- package.json
```

## API Integration

Das Frontend spricht mit dem Backend ueber REST-Endpunkte.

Gruppen und Aktivitaeten (Auszug):

```text
GET  /api/groups/my-groups
GET  /api/groups/invitations
POST /api/groups/invitations/:id/accept
POST /api/groups/invitations/:id/decline
GET  /api/activities/my-activities
GET  /api/activities/pending
POST /api/activities/:id/accept
POST /api/activities/:id/decline
```

Profilverwaltung:

```text
GET    /api/users/me
PUT    /api/users/me
DELETE /api/users/me
```

## Profil dauerhaft loeschen

In der Profilverwaltung gibt es unterhalb von "Speichern" den Button "Profil dauerhaft loeschen".

Ablauf:

1. Klick auf den Button.
2. Browser-Bestaetigung:
   "Moechten Sie Ihre Mitgliedschaft wirklich beenden? Mit Beendigung Ihrer Mitgliedschaft werden umgehend alle Ihre Daten aus drumdibum geloescht."
3. Bei "OK": `DELETE /api/users/me`, lokale Auth-Daten werden entfernt, danach Weiterleitung auf `/login`.
4. Bei "Abbrechen": Keine Aenderung, Nutzer bleibt auf der Profilseite.

## Authentifizierung

- JWT Token liegt in `localStorage` (`auth_token`)
- Der Token wird automatisch als `Authorization: Bearer <token>` gesetzt

## Umgebungsvariablen

`.env.development`

```env
VITE_API_URL=http://localhost:8080/api
```

`.env.production`

```env
VITE_API_URL=/api
```

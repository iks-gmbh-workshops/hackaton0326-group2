# REme Frontend - Vue 3 + Vite

Eine moderne, responsive Single-Page-Application für die REme Gruppen- und Aktivitäten-Management-Plattform.

## 🎯 Features

- **Dashboard**: Persönlicher Überblick mit:
  - Aktuelle Gruppen
  - Gruppeneinladungen (mit Akzeptieren/Ablehnen)
  - Meine Aktivitäten (zukünftig)
  - Ausstehende Aktivitäten (mit Zusagen/Absagen)

- **Responsive Design**: Funktioniert perfekt auf Desktop, Tablet und Mobile
- **Moderne UI**: TailwindCSS für ein sauberes, professionelles Design
- **Type-Safe**: Vollständig in TypeScript geschrieben

## 🚀 Technologie-Stack

- **Vue 3** - Progressive JavaScript Framework
- **Vite** - Nächste Generation Frontend Tooling
- **TypeScript** - Statische Typisierung
- **TailwindCSS** - Utility-first CSS Framework
- **Vue Router** - Routing
- **Pinia** - State Management (optional)
- **Axios** - HTTP Client

## 📦 Installation

```bash
# Dependencies installieren
npm install

# Development Server starten
npm run dev

# Production Build erstellen
npm run build

# Build testen
npm run preview
```

## 📁 Projektstruktur

```
frontend/
├── src/
│   ├── api/              # API Services (groupService, activityService)
│   ├── assets/           # Styles und Bilder
│   ├── components/       # Vue Komponenten
│   │   └── Dashboard/    # Dashboard-spezifische Komponenten
│   ├── composables/      # Vue Composables (Logik)
│   ├── router/           # Vue Router Konfiguration
│   ├── stores/           # Pinia Store (optional)
│   ├── views/            # Page-Views
│   ├── App.vue           # Root Komponente
│   └── main.ts           # Entry Point
├── public/               # Static Assets
├── index.html            # HTML Template
├── vite.config.ts        # Vite Konfiguration
├── tsconfig.json         # TypeScript Konfiguration
├── tailwind.config.js    # TailwindCSS Konfiguration
└── package.json          # Dependencies
```

## 🎨 Dashboard-Komponenten

### MyGroupsCard
Zeigt alle Gruppen des Benutzers an mit:
- Gruppennamen und Beschreibung
- Anzahl der Mitglieder
- Erstellungsdatum
- Klickbar für Details

### GroupInvitationsCard
Zeigt Gruppeneinladungen mit:
- Gruppennamen
- Eingeladen von
- Akzeptieren/Ablehnen Buttons
- Status-Badge

### MyActivitiesCard
Zeigt zukünftige Aktivitäten mit:
- Titel und Beschreibung
- Datum und Zeit
- Ort
- Gruppenzugehörigkeit

### PendingActivitiesCard
Zeigt Aktivitäten, die Antwort erforderlich:
- Titel und Zeit
- Zusagen/Absagen Buttons
- Rote Hervorhebung für Sichtbarkeit

## 🌐 API Integration

Die App kommuniziert mit dem Spring Boot Backend über Rest-APIs:

```
GET  /api/groups/my-groups           # Meine Gruppen
GET  /api/groups/invitations         # Einladungen
POST /api/groups/invitations/:id/accept
POST /api/groups/invitations/:id/decline
GET  /api/activities/my-activities   # Meine Aktivitäten
GET  /api/activities/pending         # Ausstehende
POST /api/activities/:id/accept
POST /api/activities/:id/decline
```

## 🔐 Authentifizierung

Token wird im localStorage gespeichert und automatisch zu allen Requests hinzugefügt:
- Header: `Authorization: Bearer <token>`

## 📱 Responsive Breakpoints

Die App nutzt TailwindCSS Breakpoints:
- **Mobile**: < 640px (sm)
- **Tablet**: 640px - 1024px (md, lg)
- **Desktop**: > 1024px (xl, 2xl)

Grid-Layouts passen sich automatisch an:
- 1 Spalte auf Mobile
- 2 Spalten auf Tablet
- 2-4 Spalten auf Desktop

## 🛠️ Development

```bash
# Im Development Mode mit Hot Reload
npm run dev

# Linting
npm run lint

# Type-Check
npm run type-check
```

## 📝 Umgebungsvariablen

`.env.development`:
```
VITE_API_URL=http://localhost:8080/api
```

`.env.production`:
```
VITE_API_URL=/api
```

## 🤝 Zusammenarbeit

Die Komponenten sind modular aufgebaut und können leicht erweitert werden:
- Neue Views einfach in `src/views/` hinzufügen
- Neue Komponenten in `src/components/` erstellen
- API-Services in `src/api/` erweitern
- Composables für Logik in `src/composables/` hinzufügen

## 📄 Lizenz

MIT

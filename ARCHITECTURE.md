# Architekturdokumentation

## Architekturentscheidung

Für Pawsitters wurde eine **klassische Schichtenarchitektur (MVC)** in einem
Monolith gewählt — keine Microservices.

### Begründung

| Kriterium | Schichtenarchitektur | Microservices |
|---|---|---|
| Team-Größe (3 Personen) | ✅ Gut handhabbar | ❌ Overhead zu hoch |
| Domänenkopplung | ✅ Entitäten stark verknüpft | ❌ Verteilte Transaktionen nötig |
| Projektdauer | ✅ Schnell aufzusetzen | ❌ Mehr Infrastruktur |
| CI-Aufwand | ✅ Eine Pipeline | ❌ Pro Service eine Pipeline |
| Deployment-Komplexität | ✅ Eine JAR | ❌ Mehrere Services + Orchestrierung |

Die Domäne ist stark relational: ein Owner besitzt Pets, ein Pet gehört zu
Requests, ein Request bekommt Offers von Hosts. Diese starken Beziehungen
würden bei einer Microservice-Aufteilung sofort verteilte Transaktionen oder
Eventual Consistency erzwingen — beides für ein Studienprojekt zu aufwändig
und ohne fachlichen Mehrwert.

## Schichten

```
┌──────────────────────────────────────────────────────────┐
│  Präsentationsschicht (Thymeleaf Templates + CSS)        │
│  - templates/owners/, hosts/, pets/, requests/, offers/  │
└──────────────────────────────────────────────────────────┘
                          ↑
┌──────────────────────────────────────────────────────────┐
│  Controller-Schicht (Spring MVC)                         │
│  - PetOwnerController, HostController, PetController     │
│  - CareRequestController, OfferController                │
│  - HomeController                                        │
│  → nimmt HTTP-Requests an, validiert, ruft Services auf  │
└──────────────────────────────────────────────────────────┘
                          ↑
┌──────────────────────────────────────────────────────────┐
│  Service-Schicht (Geschäftslogik)                        │
│  - PetOwnerService, HostService, PetService              │
│  - CareRequestService, OfferService                      │
│  → kapselt Regeln: Matching, Statusübergänge, Validierung│
└──────────────────────────────────────────────────────────┘
                          ↑
┌──────────────────────────────────────────────────────────┐
│  Repository-Schicht (Spring Data JPA)                    │
│  - PetOwnerRepository, HostRepository, …                 │
│  → CRUD, abstrahiert den DB-Zugriff                      │
└──────────────────────────────────────────────────────────┘
                          ↑
┌──────────────────────────────────────────────────────────┐
│  Persistenz (H2 In-Memory)                               │
└──────────────────────────────────────────────────────────┘
```

## Domänenmodell

```
        ┌──────────┐   1:N   ┌──────┐   N:1   ┌─────────────┐
        │ PetOwner │─────────│ Pet  │─────────│ CareRequest │
        └──────────┘         └──────┘         └─────────────┘
                                                     │ 1:N
                                                     ▼
                              ┌──────┐   1:N    ┌────────┐
                              │ Host │──────────│ Offer  │
                              └──────┘          └────────┘
```

### Entities

| Entity | Verantwortung |
|---|---|
| `PetOwner` | Tierhalter mit Profil, kann mehrere Haustiere besitzen |
| `Host` | Gastgeber mit akzeptierten Tierarten, Verfügbarkeit, Preis/Woche |
| `Pet` | Haustier, gehört zu einem Owner, hat eine `AnimalType` |
| `CareRequest` | Betreuungswunsch eines Owners für einen Zeitraum |
| `Offer` | Angebot eines Hosts zu einer konkreten Anfrage |

### Statusübergänge

**RequestStatus:**
```
OPEN ──(erstes Angebot)──→ IN_PROGRESS ──(Annahme)──→ MATCHED
  └──(Abbruch)──→ CANCELLED            └──(Abbruch)──→ CANCELLED
```

**OfferStatus:**
```
PENDING ──(annehmen)──→ ACCEPTED
   └────(ablehnen oder Sibling angenommen)──→ REJECTED
```

## Designentscheidungen im Detail

### 1. Constructor Injection statt `@Autowired`-Field-Injection

Alle Services und Controller verwenden Constructor Injection. Vorteile: Felder
können `final` sein, die Klassen sind ohne Spring testbar (im Unit-Test wird
einfach der Konstruktor mit Mocks gerufen).

### 2. Matching-Logik in der Entity (`Host.canAccommodate`)

Die Prüfung „kann dieser Host die Tierart und den Zeitraum?" steht direkt
auf `Host`, nicht im Service. Das ist eine Tell-Don't-Ask-Entscheidung: die
Daten und die Regel über sie gehören zusammen. Der Service iteriert nur
über die Hosts und ruft die Methode auf.

### 3. Transaktion auf `OfferService.acceptOffer`

`acceptOffer` führt mehrere Schreibvorgänge aus (Offer aktualisieren, Siblings
ablehnen, Request auf MATCHED setzen). Mit `@Transactional` wird das atomar.
Bricht ein Schritt ab, wird alles zurückgerollt.

### 4. H2 In-Memory statt PostgreSQL/MySQL

H2 läuft in derselben JVM. Vorteile fürs Projekt: kein Setup, keine externen
Abhängigkeiten, Tests starten ohne Konfiguration. Nachteil: Daten gehen beim
Neustart verloren — für eine Demo akzeptabel. Für Produktion würde man auf
PostgreSQL wechseln (nur `application.properties` ändern).

### 5. Thymeleaf statt REST + getrenntem Frontend

Die Anforderung sagt „einfache Benutzeroberfläche, kein komplexes Design".
Thymeleaf rendert serverseitig — keine zweite Codebase, keine JS-Toolchain.
Spring Boot integriert es out-of-the-box.

### 6. Bean Validation am Modell

Annotations wie `@NotBlank`, `@Email`, `@PositiveOrZero` machen die Regeln
im Modell sichtbar. Die JPA-Layer setzt sie zusätzlich als NOT-NULL-
Constraints in der DB durch.

## Erweiterungspotenzial

Bewusst nicht implementiert, weil nicht von der Aufgabe gefordert:

- Authentifizierung & Sessions (siehe Security-Konzept)
- E-Mail-Versand bei Angebots-Eingang
- Bewertungen für Hosts nach abgeschlossener Betreuung
- Bezahlung & Treuhand
- Mehrsprachigkeit

# 🐾 Pawsitters - Pet Holiday Platform

DHBW Projektarbeit. Plattform, die Tierhalter mit Gastgebern verbindet, die
deren Haustiere während eines Urlaubs gegen Bezahlung betreuen.

## Inhaltsverzeichnis

- [Funktionale Anforderungen](#funktionale-anforderungen)
- [Technologie-Stack](#technologie-stack)
- [Voraussetzungen](#voraussetzungen)
- [Setup & Start](#setup--start)
- [Projektstruktur](#projektstruktur)
- [Benutzung](#benutzung)
- [Tests ausführen](#tests-ausführen)
- [Weitere Dokumentation](#weitere-dokumentation)

## Funktionale Anforderungen

Alle Anforderungen aus der Aufgabenstellung sind umgesetzt:

| # | Anforderung | Umsetzung |
|---|---|---|
| 1 | Profile für Tierhalter | `PetOwner` + automatische Anlage bei Registrierung |
| 2 | Profile für Gastgeber | `Host` + Profil-Editor im Dashboard |
| 3 | Registrierung von Haustieren | `Pet` + `/pets` |
| 4 | Erstellung einer Betreuungsanfrage (Zeitraum) | `CareRequest` + `/requests` |
| 5 | Anzeige passender Angebote | `/requests/{id}` (Match-Logik + Angebotsliste) |
| 6 | Versenden von Angeboten durch Gastgeber | `OfferController.create` |
| 7 | Annahme eines Angebots | `OfferService.acceptOffer` |
| 8 | Ablehnung weiterer Angebote | Geschieht automatisch bei Annahme, oder manuell |
| 9 | Aktualisierung des Anfragestatus | `OPEN → IN_PROGRESS → MATCHED / CANCELLED` |

## Zusätzliche Features (über die Mindestanforderung hinaus)

- **Authentifizierung** mit Spring Security 6 (BCrypt-Passwort-Hashes)
- **Multi-Role-User**: Ein Nutzer kann gleichzeitig Tierhalter und Gastgeber sein
- **Persönliches Dashboard** mit Rollen-spezifischer Ansicht
- **Modernes Frontend** mit Design-System, SVG-Icons, Responsive-Layout
- **Autorisierung**: Nutzer können nur eigene Tiere / Anfragen / Angebote verwalten

## Demo-Accounts

Beim ersten Start wird die Datenbank mit Demo-Daten gefüllt
(siehe `config/DataInitializer.java`):

| Username | Passwort | Rollen |
|---|---|---|
| `tom` | `demo1234` | Tierhalter mit Hund Bello |
| `anna` | `demo1234` | Gastgeberin für Hunde |
| `ben` | `demo1234` | Gastgeber für Hunde + Katzen |
| `clara` | `demo1234` | Tierhalterin **und** Gastgeberin |
| `admin` | `admin1234` | Plattform-Admin (sieht alle Owner) |

## Technologie-Stack

- **Java 17**
- **Spring Boot 3.3.4** (Web, JPA, Validation, Thymeleaf, Security)
- **Spring Security 6** (Authentifizierung, Autorisierung, CSRF, BCrypt)
- **Maven** (Build)
- **H2** (In-Memory-Datenbank, keine Installation nötig)
- **Thymeleaf** (HTML-Templates mit Layout-Fragmenten und Security-Extras)
- **JUnit 5 + Mockito** (Tests)

## Voraussetzungen

Auf dem Entwicklungsrechner installiert sein müssen:

- **JDK 17 oder neuer** — Prüfung: `java -version`
  Falls nicht: [Eclipse Temurin 17](https://adoptium.net/) herunterladen
- **Maven 3.6+** — Prüfung: `mvn -version`
  Bei Windows: `winget install Apache.Maven`
- **Git**

VS Code mit dem **Extension Pack for Java** und der **Spring Boot Extension**
genügt; eine extra IDE ist nicht nötig. H2 läuft im Speicher und braucht
keinen DB-Server.

## Setup & Start

```bash
# Repository klonen
git clone <repo-url>
cd pawsitters

# Anwendung starten
mvn spring-boot:run
```

Anschließend Browser öffnen: <http://localhost:8080>

Optionale Datenbank-Konsole (zur Inspektion): <http://localhost:8080/h2-console>
- JDBC URL: `jdbc:h2:mem:pawsitters`
- User: `sa` (kein Passwort)

## Projektstruktur

```
pawsitters/
├── pom.xml                          Maven Build-Konfiguration
├── README.md                        Diese Datei
├── ARCHITECTURE.md                  Architekturdokumentation
├── TEST_DOCUMENTATION.md            Testdokumentation
├── SECURITY_CONCEPT.md              Security-Konzept + Shift Security Left
├── DEVELOPMENT_PROCESS.md           Team-Organisation & Reflexion
├── KI_PROMPTS.md                    Eingesetzte KI-Tools & Prompts
├── .github/workflows/ci.yml         CI-Pipeline (führt Tests bei Push aus)
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/pawsitters/
    │   │   ├── PawsittersApplication.java   Spring-Boot-Einstiegspunkt
    │   │   ├── config/                      DataInitializer (Demo-Daten)
    │   │   ├── model/                       JPA-Entities + Enums
    │   │   │   ├── PetOwner.java, Host.java, Pet.java, CareRequest.java, Offer.java
    │   │   │   ├── User.java, UserRole.java
    │   │   │   └── AnimalType.java, RequestStatus.java, OfferStatus.java
    │   │   ├── repository/                  Spring Data JPA Repositories
    │   │   ├── service/                     Geschäftslogik
    │   │   ├── security/                    Spring Security Config + UserDetailsService
    │   │   └── controller/                  Spring MVC Controller
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/style.css         Design-System (Custom Properties)
    │       └── templates/                   Thymeleaf HTML
    │           ├── index.html, dashboard.html
    │           ├── fragments/layout.html    Wiederverwendbares Header/Footer/Icon-Sprite
    │           ├── auth/                    login.html, register.html
    │           ├── owners/, hosts/, pets/, requests/, offers/
    └── test/
        └── java/com/pawsitters/
            ├── service/                     Unit-Tests
            └── integration/                 Integrations- und Security-Tests
```

## Benutzung

Typischer Ablauf in der Anwendung:

1. **Registrieren** unter `/register` (Rolle Tierhalter und/oder Gastgeber wählen)
   — alternativ einen Demo-Account aus der Tabelle oben nehmen
2. **Anmelden** unter `/login`
3. Als Tierhalter im **Dashboard**: Tier hinzufügen → neue Anfrage erstellen
4. Anfrage öffnen → passende Gastgeber sehen → warten auf Angebote
5. Als Gastgeber im Dashboard: Profil komplettieren (Tierarten, Verfügbarkeit, Preis)
6. Offene Anfragen unter `/requests` ansehen → Angebot abgeben
7. Als Tierhalter: Angebot annehmen → alle anderen werden automatisch abgelehnt

## Tests ausführen

```bash
# Alle Tests
mvn test

# Nur Unit-Tests (Service-Layer)
mvn test -Dtest='*ServiceTest'

# Nur Integrationstests
mvn test -Dtest='*IntegrationTest'
```

Erwartung: **35 Tests, 0 Fehler** (26 Unit + 9 Integrations- und Security-Tests).
Details in [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md).

## Weitere Dokumentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — Begründung der Schichtenarchitektur
- [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) — alle Tests im Detail
- [SECURITY_CONCEPT.md](SECURITY_CONCEPT.md) — Sicherheitskonzept + Shift Security Left
- [DEVELOPMENT_PROCESS.md](DEVELOPMENT_PROCESS.md) — Zusammenarbeit, Git-Workflow, Reflexion
- [KI_PROMPTS.md](KI_PROMPTS.md) — eingesetzte KI-Tools

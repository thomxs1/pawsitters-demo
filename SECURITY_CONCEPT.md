# Security-Konzept

Pflichtdokument laut Projektbeschreibung. Beschreibt sensible Daten, Risiken,
Maßnahmen und das Prinzip „Shift Security Left".

## 1. Sensible Daten im System

| Datum | Sensibilität | Begründung |
|---|---|---|
| Name (Owner/Host) | mittel | Personenbezogenes Datum nach DSGVO |
| E-Mail | mittel | Identifikator, Phishing-Ziel, DSGVO |
| Beschreibungstexte | niedrig–mittel | Können Adresshinweise, Kontaktdaten enthalten |
| Verfügbarkeit + Adressbezug eines Hosts | hoch | Lässt Rückschlüsse auf Abwesenheit zu — Einbruchsrisiko |
| Preise / Zahlungsinformationen | hoch (in Produktion) | Im aktuellen Prototyp gibt es nur Preise; reale Bezahlung ist nicht implementiert |
| Beziehung Owner ↔ Pet ↔ Zeitraum | hoch | Ein bevorstehender Urlaub kombiniert mit Adresse ist ein Einbruchsindikator |

## 2. Identifizierte Risiken

### R1 — Fehlende Authentifizierung
**Mitigiert** durch Spring Security 6: alle Routen außer `/`, `/login`,
`/register` und statischen Ressourcen erfordern eine angemeldete Session.
Passwörter werden mit **BCrypt** gehasht; Klartext landet nie in der
Datenbank. Konfiguration: `security/SecurityConfig.java`.

### R2 — Fehlende Autorisierung
**Mitigiert** durch rollenbasierte Endpunkte und explizite Ownership-Checks
in den Controllern:
- Nur ein Tierhalter kann Tiere registrieren (`@AuthenticationPrincipal` in `PetController`)
- Nur der Besitzer einer Anfrage kann Angebote annehmen/ablehnen (`OfferController`)
- Nur der Host selbst kann sein Host-Profil bearbeiten (`HostController`)
- `/owners`-Liste sehen nur Nutzer mit Rolle `ADMIN`

Bei unberechtigtem Zugriff wird `AccessDeniedException` geworfen — Spring
liefert dann automatisch HTTP 403.

### R3 — SQL-Injection
**Mitigiert** durch JPA/Hibernate mit parametrierten Queries. Keine
String-Konkatenation in DB-Queries.

### R4 — Cross-Site Scripting (XSS)
**Mitigiert** durch Thymeleaf: Standard-Escaping `th:text` (nicht `th:utext`)
wird konsequent eingesetzt. Anwenderinput in Beschreibungsfeldern wird
HTML-encoded ausgegeben.

### R5 — Cross-Site Request Forgery (CSRF)
**Mitigiert.** Spring Security CSRF-Schutz ist aktiv und fügt automatisch
Tokens in alle POST-Forms ein (Thymeleaf macht das transparent über
`<form action="..." method="post">`). Nur für die H2-Konsole ist CSRF
deaktiviert, weil sie kein Spring-rendered Form ist.

### R6 — Brute Force gegen Login / Account Enumeration
Relevant **nachdem** Auth eingebaut ist. Maßnahmen: Rate-Limiting,
einheitliche Fehlermeldungen („Login fehlgeschlagen" statt „User existiert
nicht").

### R7 — Insecure Direct Object Reference (IDOR)
URLs wie `/requests/42` zeigen direkt die ID. Ohne Autorisierung kann jeder
die Detail-Seite einer fremden Anfrage öffnen. Mitigation: Authentifizierung
+ Besitzcheck im Controller.

### R8 — Unverschlüsselter Transport
Anwendung läuft auf HTTP. In Produktion zwingend HTTPS mit gültigem
Zertifikat (z. B. Let's Encrypt) und HSTS-Header.

### R9 — Datenbank-Konsole offen
`spring.h2.console.enabled=true` ist im aktuellen Profil aktiv. **Niemals
in Produktion!** Sollte über Profile (`@Profile("dev")`) gesteuert werden.

### R10 — Logging sensibler Daten
Aktuell wird kein gefährlicher Inhalt geloggt. Bei Erweiterung darauf
achten, keine E-Mails, Passwörter oder vollständige Personendaten in
Logfiles zu schreiben.

### R11 — Missbrauch der Plattform (fachliches Risiko)
Vortäuschen falscher Identitäten (Host gibt sich als seriös aus, ist es
aber nicht). Lösung in einer realen Plattform: Identitätsprüfung,
Bewertungen, gemeldete Profile, Treuhand-Zahlungsprozess.

## 3. Bereits umgesetzte Maßnahmen

- ✅ **Bean Validation** (`@NotBlank`, `@Email`, `@PositiveOrZero`) — Daten
  werden vor dem Speichern validiert
- ✅ **JPA mit parametrierten Queries** — kein SQL-Injection-Vektor
- ✅ **Thymeleaf Auto-Escaping** — XSS-Schutz bei der Ausgabe
- ✅ **Eindeutigkeitsprüfung der E-Mail** auf Service- und DB-Ebene (Unique
  Constraint)
- ✅ **Negative Werte abgewiesen** (Preis, Zeitraum-Plausibilität)
- ✅ **Statusübergänge gekapselt** im Service — keine ungültigen
  Zustandsänderungen über die UI möglich
- ✅ **Validierung der Eingabe-IDs** mit Existenzprüfung vor Verwendung

## 4. Geplante Maßnahmen für eine nächste Version

| Priorität | Maßnahme | Aufwand |
|---|---|---|
| 1 | **Spring Security** mit Form-Login, BCrypt-Passwörtern | 1–2 Tage |
| 2 | **Autorisierung**: Owner darf nur eigene Daten, Host nur eigene Angebote | 1 Tag |
| 3 | **HTTPS** + HSTS in Produktion | wenige Stunden |
| 4 | **H2-Konsole** über Profil deaktivieren | 30 min |
| 5 | **CSRF-Tokens** in allen Formularen (kommt mit Spring Security) | inklusive |
| 6 | **Rate-Limiting** an Login-Endpunkt (z. B. Bucket4j) | halber Tag |
| 7 | **Audit-Log** für sicherheitsrelevante Aktionen | 1 Tag |
| 8 | **Persistente DB** (PostgreSQL) mit Backups statt H2 | 1 Tag |
| 9 | **OWASP Dependency-Check** als Maven-Plugin in der CI | halber Tag |
| 10 | **DSGVO-konforme Datenlöschung** (Soft-Delete + Anonymisierung) | 2 Tage |

## 5. Shift Security Left

### Was bedeutet das Prinzip?

„Shift Security Left" beschreibt, dass Sicherheit **so früh wie möglich**
im Entwicklungsprozess berücksichtigt wird — also **nach links** auf der
typischen Zeitachse einer Software:

```
Anforderungen → Design → Implementierung → Test → Deployment → Betrieb
   ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
                       Security shiftet hier hin
```

Klassisch wurde Security erst am Ende geprüft (Pentest, Security-Review
kurz vor Release). Probleme zu diesem Zeitpunkt sind **teuer**: das Design
oder die DB-Struktur müssen geändert werden. Wird Security früh
mitgedacht, sind die Kosten zur Behebung deutlich niedriger.

### Anwendung auf Pawsitters

| Phase | Was wir konkret tun (würden) |
|---|---|
| **Anforderungen** | Bedrohungsmodell erstellen: Wer sind unsere Angreifer? Welche Daten sind sensibel? (Punkt 1 oben). Datenschutz-Folgenabschätzung. |
| **Design** | Architekturentscheidung „Auth ist eine Querschnittssorge, gehört vor die Controller". Datenmodell so wählen, dass IDs nicht ratbar sind (UUIDs statt fortlaufenden Longs in Produktion). |
| **Implementierung** | Sichere Defaults wählen: Thymeleaf-Auto-Escaping aktiv, JPA statt rohem SQL, Bean Validation an Modellen. Linter / IDE-Warnings ernst nehmen. |
| **Code Review** | Jeder Pull Request wird von einem zweiten Teammitglied gereviewt; Security-Checkliste (Input validiert? Autorisierung geprüft? Logging unbedenklich?). |
| **Test** | Unit-Tests prüfen explizit Edge Cases wie „Aktion auf abgeschlossenem Objekt", „negative Werte" — also genau die Fälle, die Security-Lücken erzeugen. Geplant: **OWASP ZAP** als automatisierter Scan in der CI. |
| **CI/CD** | Geplant: **OWASP Dependency-Check** als Maven-Plugin → bekannte CVEs in Dependencies werden vor jedem Merge gemeldet. **SpotBugs** mit FindSecBugs-Plugin für statische Code-Analyse. |
| **Betrieb** | Logging mit Korrelation-IDs, Monitoring auf untypisches Verhalten (viele 401-Antworten = möglicher Angriff). |

### Konkrete Beispiele aus diesem Projekt, die Shift-Security-Left zeigen

1. **`Host.canAccommodate` validiert vor jedem Angebot** — die Regel ist im
   Modell verankert. Dadurch kann kein Controller versehentlich die Prüfung
   weglassen.
2. **Statusübergänge sind im Service gekapselt**. Es ist nicht möglich,
   ein bereits abgelehntes Angebot aus der UI heraus „nachträglich
   anzunehmen" — das wäre ein Geschäftsregel-Bypass.
3. **JPA + Bean Validation** sind sichere Defaults, die SQL-Injection und
   ungültige Daten von Anfang an verhindern.
4. **Tests sind im selben Repository und Bestandteil der CI** — jede
   Änderung wird automatisch geprüft, nicht erst kurz vor Abgabe.

## 6. Risiken durch KI im Sicherheitskontext

Da KI-Tools beim Code-Schreiben eingesetzt werden (siehe `KI_PROMPTS.md`):

- **Veraltete Patterns**: KI schlägt manchmal deprecated Crypto-Funktionen vor
- **Plausible, aber falsche Annahmen**: z. B. „Spring Security ist standardmäßig
  aktiv" — ist es nicht
- **Code-Halluzination**: Methoden, die in der Realität nicht existieren
- **Secrets im Prompt**: Niemals echte Passwörter oder Schlüssel in
  KI-Prompts geben
- **Über-Vertrauen**: Generierter Code muss immer geprüft, getestet und
  verstanden werden

Diese Risiken wurden begegnet, indem wir generierten Code zeilenweise
geprüft, durch Tests verifiziert und gegebenenfalls umgeschrieben haben.
Siehe `KI_PROMPTS.md`.

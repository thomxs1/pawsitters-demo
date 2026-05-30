# Entwicklungsprozess und Zusammenarbeit

> **Hinweis für die Gruppe:** Diese Datei ist ein Template. Tragt die
> tatsächlichen Namen, Aufgabenverteilungen und Reflexionspunkte eures
> Teams ein. Die Struktur erfüllt die Anforderungen der Aufgabenstellung
> („Aufteilung der Aufgaben, verwendeter Entwicklungsansatz, Git-Nutzung,
> Reflexion").

## 1. Team

| Person | Schwerpunkt |
|---|---|
| [Name 1] | Backend (Service-/Repository-Schicht, Tests) |
| [Name 2] | Frontend (Thymeleaf-Templates, CSS) und Controller |
| [Name 3] | Architektur, Security-Konzept, CI/CD, Dokumentation |

Jedes Mitglied hat in **allen** Bereichen kleinere Beiträge geleistet
(Pair-Programming für komplexe Stellen wie `OfferService.acceptOffer`), die
obigen Schwerpunkte beschreiben die Hauptverantwortlichkeiten.

## 2. Entwicklungsansatz

Wir haben uns für ein **Kanban-orientiertes Vorgehen** entschieden:

- **GitHub Projects Kanban-Board** mit den Spalten `Backlog`, `In Progress`,
  `In Review`, `Done`
- Tickets werden als **GitHub Issues** angelegt und am Board gezogen
- Maximal 2 Tickets gleichzeitig in `In Progress` pro Person (WIP-Limit)
- Wöchentliches kurzes Sync-Meeting (15 Minuten), um Blocker zu klären

**Warum Kanban statt Sprints?** Drei Personen mit gleichzeitig laufendem
Studium → unregelmäßige Verfügbarkeit. Sprints mit festen Zeitfenstern
hätten wir oft nicht eingehalten. Kanban erlaubt kontinuierlichen Fluss.

## 3. Aufgabenaufteilung

| Aufgabengruppe | Verantwortlich | Bemerkung |
|---|---|---|
| Projekt-Setup, Maven, Spring Boot Skeleton | [Name 3] | |
| Entities + Enums | [Name 1] | |
| Repositories | [Name 1] | |
| Services (PetOwner, Pet, Host) | [Name 1] | |
| Service `CareRequest` | [Name 1] und [Name 2] (Pair) | |
| Service `Offer` (Kernlogik) | komplettes Team (Pair) | Hier liegt die kritischste Logik (Annahme + Auto-Reject) |
| Controller | [Name 2] | |
| Thymeleaf-Templates | [Name 2] | |
| Unit-Tests | [Name 1] und [Name 3] | |
| Integrationstests | [Name 3] | |
| Architekturdokumentation | [Name 3] | |
| Security-Konzept | [Name 3] | |
| CI-Pipeline | [Name 3] | |
| KI-Prompts-Dokumentation | gemeinsam | jeder dokumentiert eigene Prompts |
| Präsentation | gemeinsam | |

Beiträge sind über Git-Commits und Pull-Request-Historie nachvollziehbar
(`git log --author="..."`).

## 4. Git-Workflow

Wir verwenden den **Feature-Branch-Workflow**:

```
main  ──●────●─────────●─────●─────●────→
         \    \         \     \     \
          \    \         \     \     feature/security-concept
           \    \         \     feature/integration-tests
            \    \         feature/offer-service
             \    feature/templates
              feature/entities
```

- `main` ist immer lauffähig und enthält nur reviewten Code
- Pro Feature ein eigener Branch: `feature/<kurze-beschreibung>`
- Pull Request gegen `main`, **Review durch mindestens ein anderes
  Teammitglied** erforderlich
- CI muss grün sein (siehe `.github/workflows/ci.yml`)
- Nach Merge wird der Feature-Branch gelöscht

### Commit-Konventionen

Wir orientieren uns lose an Conventional Commits:

```
feat: add OfferService.acceptOffer with auto-reject of siblings
fix: prevent negative price in offer creation
test: add edge case for past start date
docs: expand security concept with shift-left section
chore: configure GitHub Actions CI pipeline
```

## 5. CI-Pipeline

Datei: `.github/workflows/ci.yml`

Automatisch ausgelöst bei jedem Push und Pull Request auf `main`:

1. Checkout des Codes
2. JDK 17 einrichten (Temurin)
3. Maven Dependencies cachen
4. `mvn -B verify` ausführen (kompiliert + Tests)
5. Test-Report archivieren

Damit ist die Anforderung „automatische Ausführung von Tests über CI"
erfüllt.

## 6. Reflexion

### Was lief gut

- **Frühe Festlegung der Architektur** verhinderte Streit über Strukturen
- **Pair-Programming am `OfferService`** sparte am Ende Zeit; die
  Statusübergänge sind die fehleranfälligste Stelle
- **Konsequenter Test-First-Ansatz** beim `OfferService`: Auto-Reject-Logik
  wurde durch den Test #18 abgesichert, bevor sie als „funktionierend"
  galt
- **CI von Anfang an** zeigte Brüche sofort

### Was war schwierig

- **Thymeleaf-Lernkurve**: zwei von drei Personen hatten vorher keine
  Erfahrung. Workaround: ein Mitglied baute den ersten Template-Satz, die
  anderen konnten dann per Copy-Paste-Anpassung produktiv werden.
- **JPA-Beziehungen**: `@OneToMany` mit Cascade und Lazy/Eager hat
  anfangs Verwirrung gestiftet. Hilfreich war, früh Integrationstests
  zu schreiben, die das echte Persistenzverhalten zeigen.
- **Zeitkoordination**: drei Stundenpläne synchronisieren ist mühsam.
  Asynchrone PR-Reviews haben das entschärft.

### Was würden wir beim nächsten Projekt anders machen

- Früher die **CI-Pipeline aufsetzen** — wir hatten sie erst in Woche 3
- **OpenAPI/REST-Endpunkte** statt rein server-rendered HTML, falls das
  Frontend später ausgetauscht werden soll
- **Spring Security von Anfang an** als Skeleton einbauen (deaktiviert
  während der Entwicklung), damit es am Ende kein nachträglicher Eingriff ist

## 7. Tooling

| Werkzeug | Zweck |
|---|---|
| **GitHub** | Repository + Issues + Actions |
| **GitHub Projects** | Kanban-Board |
| **VS Code + Extension Pack for Java** | IDE |
| **Maven** | Build |
| **JUnit 5, Mockito** | Tests |
| **Spring Boot DevTools** | Hot Reload während der Entwicklung |
| **Discord / WhatsApp** | Tagesgeschäft |
| **KI-Tool** | Code-Skeleton, Dokumentations-Entwürfe (siehe `KI_PROMPTS.md`) |

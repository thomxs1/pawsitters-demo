# Dokumentation des KI-Einsatzes

> **Hinweis für die Gruppe:** Diese Datei dokumentiert ehrlich, wo KI
> eingesetzt wurde. Passt sie an, wenn ihr weitere Prompts dazugibt oder
> andere Tools verwendet. Das Reviewer-Team erwartet **Transparenz**, nicht
> „möglichst wenig KI". Wichtig ist, dass ihr jeden Code verstehen und
> erklären könnt.

## 1. Verwendete Tools

| Tool | Modell / Version | Eingesetzt für |
|---|---|---|
| Claude (Anthropic) | Claude Opus 4.7 | Code-Skeleton, Dokumentations-Entwürfe, Test-Ideen |

*(Tragt hier weitere Tools ein, falls verwendet — z. B. GitHub Copilot,
ChatGPT, Gemini.)*

## 2. Eingesetzt für

- **Erstgenerierung des Projekt-Skeletts** (`pom.xml`, Spring-Boot-Hauptklasse,
  Verzeichnisstruktur)
- **Vorschläge für Domänenmodell und Entity-Beziehungen** (Owner ↔ Pet,
  Request ↔ Offer ↔ Host)
- **Test-Stubs**: Welche Edge Cases sind interessant?
- **Entwürfe der Dokumentations-Dateien** (README, ARCHITECTURE,
  SECURITY_CONCEPT)
- **Erklärung von JPA-Annotations** in einzelnen Fragen

**Nicht** für: blindes Übernehmen ganzer Funktionen ohne Review, geheime
Daten, fertige Lösungen für die Präsentation.

## 3. Verwendete Prompts (Auszug)

Die folgenden Prompts wurden in iterierter Form verwendet. Die jeweilige
Antwort wurde anschließend gelesen, kritisch geprüft und angepasst.

### Prompt 1 — Projekt-Skeleton
> „Erstelle ein Maven-`pom.xml` für ein Spring-Boot-3-Projekt mit Java 17.
> Dependencies: Web, Thymeleaf, JPA, Validation, H2 (runtime), Test-Starter."

**Anpassungen:** Versionen verifiziert (Spring Boot 3.3.4 stabil),
H2-Console-Property überprüft.

### Prompt 2 — Domänenmodell
> „Ich baue eine Pet-Sitting-Plattform. Entities: PetOwner, Host, Pet,
> CareRequest, Offer. Skizziere die JPA-Beziehungen und welche Felder
> validiert werden sollten."

**Anpassungen:** Wir haben die Beziehung `Pet ↔ CareRequest` von
`@OneToMany` auf `@ManyToOne` an `CareRequest` reduziert — ein Pet kann
viele Anfragen haben, das andersrum ist nicht relevant.

### Prompt 3 — Statuslogik
> „Modelliere die Statusübergänge: Anfrage ist OPEN, wird IN_PROGRESS
> sobald Angebote vorliegen, wird MATCHED bei Annahme, kann CANCELLED
> werden. Wo soll diese Logik liegen?"

**Anpassungen:** Wir wollten die Übergänge **nicht** in der Entity, sondern
im Service, weil sie mit Sibling-Updates kombiniert sind und Transaktionen
brauchen. Das diskutierten wir und entschieden uns bewusst dagegen, was die
KI vorschlug.

### Prompt 4 — Test-Ideen
> „Welche Edge Cases sollte ich für `OfferService.acceptOffer` testen?"

**Antwort enthielt u. a.:** doppelte Annahme, Annahme bereits abgelehnter
Offers, Race-Condition zwischen zwei Annahmen.

**Anpassungen:** Die Race-Condition haben wir bewusst aus dem Scope
genommen (würde Optimistic Locking erfordern, ist im Konzept beschrieben).
Die anderen Cases sind in den Tests #18 und #19 umgesetzt.

### Prompt 5 — Thymeleaf-Form mit Listen-Bind
> „Wie binde ich in Thymeleaf eine `Set<Enum>` an Checkboxes in einem
> POST-Formular?"

**Anpassungen:** Die KI gab eine Lösung mit `th:field` und Iteration über
das Enum. Wir verwenden stattdessen `name="animals"` mit normalen
Checkboxes plus `@RequestParam List<AnimalType>` im Controller — robuster
gegen Spring-Binding-Edge-Cases.

### Prompt 6 — Security-Konzept (Entwurf)
> „Liste typische Sicherheitsrisiken für eine Java-Webanwendung mit
> Spring Boot, Thymeleaf, JPA, H2. Bitte mit Bezug auf OWASP Top 10."

**Anpassungen:** Wir haben die generische Liste auf unser Projekt
zugeschnitten — das Adressrisiko bei Hosts (Einbruchsindikator durch
Abwesenheit des Owners) ist ein domänenspezifisches Risiko, das wir
ergänzt haben.

### Prompt 7 — Shift Security Left Erklärung
> „Erkläre Shift Security Left in einem Absatz, ohne Buzzwords, und gib
> konkrete Maßnahmen pro Phase eines Software-Lifecycles."

**Anpassungen:** Die Tabelle haben wir gegen unser Projekt gespiegelt —
welche Maßnahme **gehört zu welchem unserer Schritte**, nicht nur generisch.

## 4. Wie wir Ergebnisse überprüft haben

| Schritt | Beschreibung |
|---|---|
| **Lesen** | Jedes generierte Snippet wird vor Übernahme komplett gelesen |
| **Compile-Check** | `mvn compile` muss durchlaufen |
| **Test-Check** | Generierter Code wird mit Tests abgesichert; rote Tests = Code wird angepasst |
| **Review im Team** | Pull Requests werden gegenseitig geprüft, generierter Code wird im Kommentar markiert |
| **Verständnis-Check** | Vor Abgabe-Präsentation kann jedes Teammitglied alle Stellen erklären, auch die KI-generierten |

## 5. Reflexion: Einfluss von KI auf Softwareentwicklung, Testing, Security

### Auf Softwareentwicklung

**Positiv:**
- Skeleton-Code, Boilerplate (Getter/Setter, Repositories) sehr schnell
- Erinnerungsstütze für selten genutzte Frameworks (JPA-Annotations,
  Thymeleaf-Syntax)
- Refactoring-Vorschläge öffnen oft neue Perspektiven

**Negativ:**
- Risiko des oberflächlichen Verstehens: man übernimmt Code, der „läuft",
  ohne die Implikationen zu kennen
- KI tendiert zu Generalisierungen, die im konkreten Projekt unnötig
  komplex sind (Pattern-Overkill)
- Veraltete API-Vorschläge (alte Spring-Boot-Versionen, deprecated
  Annotations)

### Auf Testing

**Positiv:**
- Hilft beim Brainstormen von Edge Cases
- Generiert Test-Datenfixtures schnell
- Erklärt fehlschlagende Tests durch Stack-Trace-Analyse

**Negativ:**
- KI-generierte Tests testen oft **die Implementierung statt das Verhalten**
  (Tautologien). Beispiel: Test prüft, dass `save` aufgerufen wurde, aber
  nicht, ob das Richtige gespeichert wurde
- „Grüne Tests" geben Sicherheit, die nicht immer berechtigt ist

### Auf Security

**Positiv:**
- KI kennt OWASP Top 10 und kann sie auf Code-Snippets anwenden
- Schnelles Lernen über neue Angriffsvektoren

**Negativ und gefährlich:**
- KI kann **plausibel falsche** Security-Aussagen machen
  („Spring schützt automatisch gegen X" — oft nicht ohne Konfiguration)
- Generierte Crypto-Routinen können veraltet sein (MD5, schwache Modes)
- KI sieht keinen ganzheitlichen Anwendungskontext und unterschätzt
  Kombinationsrisiken
- Verleitet zur **falschen Sicherheit**: „Die KI sagt, das ist sicher" ist
  kein Sicherheitsnachweis

### Risiken durch KI generell

1. **Halluzination**: API-Methoden, die nicht existieren
2. **Plagiats- und Lizenzrisiken**: generierter Code kann auf
   GPL-Schnipseln basieren
3. **Datenschutz**: Was wir in den Prompt geben, könnte zur Modellverbesserung
   verwendet werden — keine echten personenbezogenen Daten oder Secrets
4. **Skill-Atrophie**: Wer nur prompt, übt nicht das Programmieren
5. **Verlust von Architektur-Kompetenz**: KI gibt Lösungen, aber selten
   den langen Bogen über Designentscheidungen

## 6. Eigenleistung

Trotz KI-Unterstützung stammen vom Team eigenständig:

- Architekturentscheidung Monolith vs. Microservices und deren Begründung
- Domänen-Sonderfälle (Auto-Reject anderer Angebote bei Annahme)
- Statusmodell und seine Übergänge
- Aufteilung der Verantwortlichkeiten zwischen Service und Entity
- Alle Test-Auswahlen und konkreten Test-Daten
- Das Security-Risiko „Host-Verfügbarkeit als Einbruchsindikator"
- Alle Bewertungen, Reflexionen und Anpassungen der KI-Vorschläge
- Die finale Codeform jedes Files (Review-zyklisch geprüft und überarbeitet)

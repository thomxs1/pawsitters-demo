# Testdokumentation

## Übersicht

| Test-Klasse | Anzahl | Typ |
|---|---|---|
| `PawsittersApplicationTests` | 1 | Smoke (Context-Load) |
| `PetOwnerServiceTest` | 3 | Unit |
| `HostServiceTest` | 3 | Unit |
| `PetServiceTest` | 3 | Unit |
| `CareRequestServiceTest` | 4 | Unit |
| `OfferServiceTest` | 7 | Unit |
| `UserServiceTest` | 7 | Unit (neu in v1.1) |
| `PawsittersIntegrationTest` | 2 | Integration |
| `SecurityIntegrationTest` | 5 | Integration (neu in v1.1) |
| **Summe** | **35** | 1 Smoke + 27 Unit + 7 Integration |

Vorgabe: mindestens 10 Unit-Tests. Integrationstests gelten als Bonus.

Ausführen mit:
```bash
mvn test
```

## Vorgehensweise

**Unit-Tests** verwenden Mockito, um Repositories zu mocken. Dadurch ist
die Service-Logik isoliert testbar, schnell und unabhängig von der Datenbank.

**Integrationstests** laden den vollständigen Spring-Kontext und arbeiten
gegen die H2-In-Memory-DB. Sie verifizieren das Zusammenspiel aller Schichten.

## Unit-Tests

### `PetOwnerServiceTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 1 | `create_validOwner_savesAndReturnsOwner` | Gültiger Tierhalter wird gespeichert | Repository.save() wird mit dem Owner aufgerufen, Owner zurückgegeben | Normalfall |
| 2 | `create_duplicateEmail_throwsException` | Anlegen mit bereits existierender E-Mail | `IllegalArgumentException` mit Hinweis "existiert bereits", kein Save | Edge Case |
| 3 | `create_blankEmail_throwsException` | Anlegen mit leerer E-Mail | `IllegalArgumentException` | Edge Case |

### `HostServiceTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 4 | `create_endDateBeforeStart_throwsException` | Verfügbarkeitsende liegt vor dem Anfang | `IllegalArgumentException` | Edge Case |
| 5 | `findMatchingHosts_returnsOnlyMatchingHosts` | Drei Hosts, einer matcht | Nur der passende Host wird zurückgegeben | Normalfall |
| 6 | `canAccommodate_periodPartiallyOutside_returnsFalse` | Anfragezeitraum endet nach Host-Verfügbarkeit | `canAccommodate` liefert `false` | Edge Case |

### `PetServiceTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 7 | `register_validPet_setsOwnerAndSaves` | Gültiges Haustier mit existierendem Owner | Owner wird gesetzt, Pet gespeichert | Normalfall |
| 8 | `register_unknownOwner_throwsException` | Pet-Registrierung mit nicht existierender Owner-ID | `IllegalArgumentException`, kein Save | Edge Case |
| 9 | `register_missingAnimalType_throwsException` | Pet ohne `AnimalType` | `IllegalArgumentException` | Edge Case |

### `CareRequestServiceTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 10 | `create_validRequest_setsStatusOpen` | Gültige Anfrage erstellen | Status auf OPEN, Daten gesetzt | Normalfall |
| 11 | `create_endBeforeStart_throwsException` | Enddatum liegt vor Startdatum | `IllegalArgumentException` | Edge Case |
| 12 | `create_startInPast_throwsException` | Startdatum in der Vergangenheit | `IllegalArgumentException` | Edge Case |
| 13 | `updateStatus_setsNewStatus` | Status einer Anfrage ändern | Neuer Status wird gespeichert | Normalfall |

### `OfferServiceTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 14 | `createOffer_validOffer_savesAndUpdatesRequestStatus` | Erstes Angebot zu einer offenen Anfrage | Offer mit PENDING, Request wechselt zu IN_PROGRESS | Normalfall |
| 15 | `createOffer_animalTypeMismatch_throwsException` | Host akzeptiert die geforderte Tierart nicht | `IllegalArgumentException` | Edge Case |
| 16 | `createOffer_requestAlreadyMatched_throwsException` | Angebot zu bereits gematchter Anfrage | `IllegalArgumentException` | Edge Case |
| 17 | `createOffer_negativePrice_throwsException` | Negativer Preis | `IllegalArgumentException` | Edge Case |
| 18 | `acceptOffer_acceptsAndRejectsSiblings` | 3 Angebote, eines wird angenommen | 1× ACCEPTED, 2× REJECTED, Request MATCHED | Normalfall (Kernlogik) |
| 19 | `acceptOffer_alreadyRejected_throwsException` | Annahme eines bereits abgelehnten Angebots | `IllegalArgumentException` | Edge Case |
| 20 | `rejectOffer_setsStatusToRejected` | Einzelnes Angebot ablehnen | Status REJECTED | Normalfall |

## Integrationstests

### `PawsittersIntegrationTest`

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 21 | `completeFlow_ownerCreatesRequest_hostsSendOffers_ownerAcceptsOne` | End-to-End-Flow: Owner anlegen → Pet registrieren → 2 Hosts anlegen → Anfrage stellen → beide Hosts geben Angebot ab → Owner nimmt eines an | Alles persistiert, finale Stati: Offer1 ACCEPTED, Offer2 REJECTED, Request MATCHED | Integration / Normalfall |
| 22 | `findMatchingHosts_returnsOnlyHostsThatFitAnimalAndPeriod` | Matching-Logik durch alle Schichten | Cat-Sitter wird für Hund-Anfrage nicht zurückgegeben, Dog-Sitter schon | Integration |

## Abdeckung der funktionalen Anforderungen

| Anforderung | Abdeckende Tests |
|---|---|
| Profile für Tierhalter | #1, #2, #3 |
| Profile für Gastgeber | #4, #5 |
| Registrierung von Haustieren | #7, #8, #9 |
| Erstellung einer Anfrage | #10, #11, #12 |
| Anzeige passender Angebote | #5, #22 |
| Versenden von Angeboten | #14, #15, #16, #17, #21 |
| Annahme eines Angebots | #18, #21 |
| Automatische Ablehnung | #18 (Siblings), #21 |
| Statusaktualisierung | #13, #14 (OPEN→IN_PROGRESS), #18 (→MATCHED) |

## Edge Cases im Überblick

Insgesamt **11 explizite Edge-Case-Tests**:

- Doppelte E-Mail (#2)
- Leere/fehlende Pflichtfelder (#3, #9)
- Ungültige Zeiträume (#4, #11, #12)
- Räumlich-zeitlicher Mismatch beim Matching (#6, #15)
- Operationen auf bereits abgeschlossenen Entitäten (#16, #19)
- Negative Preise (#17)
- Verweise auf nicht existierende Entitäten (#8)

## Neue Tests in v1.1 (Auth & Security)

### `UserServiceTest` (Unit, 7 Tests)

| # | Test | Was wird getestet | Erwartetes Ergebnis | Kategorie |
|---|---|---|---|---|
| 24 | `register_asOwnerOnly_createsOwnerProfile` | Registrierung nur als Tierhalter | OWNER-Rolle gesetzt, `ownerProfile` angelegt, `hostProfile == null` | Normalfall |
| 25 | `register_asHostOnly_createsHostProfile` | Registrierung nur als Gastgeber | HOST-Rolle gesetzt, `hostProfile` angelegt, `ownerProfile == null` | Normalfall |
| 26 | `register_asBoth_createsBothProfiles` | Multi-Role-Registrierung | Beide Rollen, beide Profile angelegt | Normalfall |
| 27 | `register_withoutAnyRole_throwsException` | Keine Rolle gewählt | `IllegalArgumentException`, kein Save | Edge Case |
| 28 | `register_withDuplicateUsername_throwsException` | Username bereits vergeben | `IllegalArgumentException`, kein Save | Edge Case |
| 29 | `register_withDuplicateEmail_throwsException` | E-Mail bereits vergeben | `IllegalArgumentException`, kein Save | Edge Case |
| 30 | `register_hashesPasswordWithEncoder` | Passwort-Hash | `PasswordEncoder.encode()` wird aufgerufen, Klartext nicht persistiert | Security |

### `SecurityIntegrationTest` (Integration, 5 Tests)

| # | Test | Was wird getestet | Erwartetes Ergebnis |
|---|---|---|---|
| 31 | `publicRoutes_areAccessibleWithoutLogin` | `/`, `/login`, `/register` | HTTP 200 ohne Authentifizierung |
| 32 | `protectedRoutes_redirectToLoginWhenNotAuthenticated` | `/dashboard`, `/pets`, `/requests` | HTTP 3xx Redirect auf `/login` |
| 33 | `registrationAndLogin_workEndToEnd` | Registrierung + Login | User wird angelegt, kann sich einloggen, wird zu `/dashboard` weitergeleitet |
| 34 | `registration_withDuplicateUsername_showsError` | Doppelte Registrierung | Bleibt auf Register-Page mit Fehlermeldung |
| 35 | `registration_withoutAnyRole_showsError` | Keine Rolle gewählt | Validierungsfehler |

Diese Tests laufen im Profil `test`, in dem `DataInitializer` deaktiviert ist —
damit starten sie mit leerer Datenbank.

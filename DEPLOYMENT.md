# Deployment auf Render

Anleitung, um Pawsitters auf [Render](https://render.com) kostenlos
zu deployen, damit Freunde darauf zugreifen können.

## Was du bekommst

- HTTPS-URL wie `https://pawsitters-XXXX.onrender.com`
- PostgreSQL-Datenbank (kostenloser Tier, 1 GB)
- Auto-Deploy bei jedem Push auf `main`
- Logs und Metriken im Render-Dashboard

## Hinweise zum Free-Tier

- **Schlafmodus:** Der Web Service legt sich nach 15 Minuten ohne Traffic
  schlafen. Der erste Request danach braucht ~30 Sekunden zum Aufwachen.
  Für Hobby-Demos okay, für „immer verfügbar" nicht.
- **DB läuft 90 Tage:** Im kostenlosen Tier wird die PostgreSQL nach
  90 Tagen automatisch gelöscht. Du bekommst vorher eine E-Mail. Wenn du
  länger laufen willst, brauchst du das Starter-Plan ab ~$7/Monat.
- **Free-Tier-Details ändern sich öfter** — vor dem Loslegen kurz auf
  [render.com/pricing](https://render.com/pricing) prüfen.

## Vorbereitung im Repo

Diese Dateien sind bereits enthalten:
- `Dockerfile` — Multi-Stage-Build mit Maven + JRE
- `.dockerignore` — schlankes Image
- `render.yaml` — Infrastructure-as-Code für Render
- `src/main/resources/application-prod.properties` — Produktions-Settings
- Aktualisierte `pom.xml` mit PostgreSQL-Driver
- `DataInitializer` liest Passwörter aus Env-Vars

Du musst nichts mehr am Code tun. Stelle nur sicher, dass dein
GitHub-Repo aktuell ist (`git push`).

## Schritt 1: Render-Account anlegen

1. Auf [render.com](https://render.com) → „Get Started"
2. Mit GitHub einloggen
3. Render bittet um Zugriff auf deine Repos — gewähre den Zugriff
   auf das Pawsitters-Repo

## Schritt 2: Blueprint deployen

Render liest `render.yaml` aus dem Repo und legt alles automatisch an.

1. Im Dashboard: **„New +" → „Blueprint"**
2. Pawsitters-Repo wählen
3. Render zeigt, was angelegt wird:
   - Web Service `pawsitters` (aus Dockerfile)
   - PostgreSQL `pawsitters-db`
4. Render fragt nach den beiden geheimen Env-Vars:
   - `DEMO_PASSWORD` — generiere etwas Starkes,
     z. B. mit `openssl rand -base64 24`
   - `ADMIN_PASSWORD` — auch generieren
   **Schreib dir beide Werte irgendwo auf!** Sonst kommst du nicht mehr
   in deinen Admin-Account.
5. „Apply" klicken

Render baut jetzt den Docker-Container. Das dauert beim ersten Mal
**5–10 Minuten** (Maven lädt alle Dependencies). Folgende Deploys sind
schneller dank Layer-Caching.

## Schritt 3: Verifizieren

1. Im Service-Dashboard auf den **„Logs"-Tab** klicken
2. Du solltest am Ende sehen:
   ```
   === Pawsitters Demo-Daten geladen ===
     tom    (Tierhalter mit Hund Bello)
     anna   (Gastgeberin fuer Hunde)
     ...
   ```
3. Oben rechts auf die URL klicken — die App öffnet sich
4. Mit `tom` und dem `DEMO_PASSWORD` einloggen, das du in Schritt 2 gesetzt hast

## Schritt 4: An Freunde verteilen

- Schick ihnen die URL
- Sag ihnen, sie sollen sich unter `/register` selbst registrieren
  (eigenes Konto), oder nutze die Demo-Accounts mit deinem `DEMO_PASSWORD`

## Updates ausrollen

Sobald du auf `main` pushst, deployed Render automatisch. Du siehst den
Build im „Events"-Tab. Wenn was schiefgeht, sieht man's in den Logs.

## Wenn was nicht funktioniert

### Build schlägt fehl
- Logs unter „Events" → fehlgeschlagenen Deploy anklicken
- Häufig: Dependencies-Probleme, Maven-Build-Errors

### App startet aber 500 / Whitelabel-Error
- „Logs"-Tab öffnen, Stacktrace lesen
- Häufig: DB-Connection-Probleme. Prüfe, ob alle `DATABASE_*`-Env-Vars
  in den Service-Settings da sind. Postgres muss laufen, bevor der Service startet.

### Login funktioniert nicht
- Check ob `DEMO_PASSWORD` und `ADMIN_PASSWORD` als Env-Vars gesetzt sind
- Wenn du sie vergessen hast: Im Service-Dashboard unter „Environment"
  überschreiben, dann „Manual Deploy" — beim Neustart werden die Demo-User
  aber NICHT neu angelegt, weil schon User in der DB sind. Du müsstest
  die Passwörter direkt in der DB setzen (siehe unten) oder die DB resetten.

### Demo-User-Passwort später ändern
Render-Web-Shell unter „Shell"-Tab:
```bash
# In den Container einloggen, dann:
# (geht aktuell nur in bezahlten Plans — sonst über die DB direkt)
```
Oder über die PostgreSQL-Konsole direkt einen BCrypt-Hash setzen.
Am einfachsten: Datenbank zurücksetzen, Service neu starten,
DataInitializer läuft erneut mit den neuen Werten.

## Alternativen, falls Render nervt

- **Fly.io** — ähnliches Konzept, `flyctl launch` und go. Generöser
  Free-Tier, aber CLI-basiert.
- **Railway** — sehr ähnlich zu Render, hat seit 2023 keinen echten Free-Tier
  mehr, aber 5 USD Startguthaben.
- **Eigener VPS** (Hetzner, Contabo, etc.) — ab 4 €/Monat. Mehr Aufwand,
  aber maximale Kontrolle.

## Sicherheits-Erinnerung

Pawsitters ist ein **Studienprojekt**. Wenn die App öffentlich erreichbar
ist, gibt's keine Brute-Force-Protection, keine Rate-Limits, keine
Captcha-Validierung bei der Registrierung. Für drei Freunde okay.
Für „dauerhaft online" solltest du mindestens:

- Spring Security Rate-Limiting ergänzen (z. B. mit Bucket4j)
- Brute-Force-Schutz auf der Login-Route
- E-Mail-Verifizierung bei der Registrierung
- HTTPS via Caddy/Let's Encrypt (Render macht das für dich)

Aber das ist Stoff für eine separate Iteration.

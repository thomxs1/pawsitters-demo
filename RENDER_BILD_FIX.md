# Fix: Bild-Upload-Fehler auf Render (500 / Whitelabel Error)

## Was war das Problem?

Lokal (H2) funktionierte der Bild-Upload, auf Render (PostgreSQL) nicht.

Ursache: Die alte Annotation `@Lob` auf dem `byte[]`-Feld hat Hibernate dazu
gebracht, das Bild bei PostgreSQL als **Large Object** (`oid`-Typ) zu speichern.
Large Objects brauchen eine aktive Transaktion zum Lesen — beim Streamen des
Bildes über den `/pets/{id}/image`-Endpunkt war die oft schon geschlossen,
daher der 500-Fehler. H2 kennt diesen Mechanismus nicht, deshalb trat der
Fehler lokal nie auf.

Der Code nutzt jetzt `@JdbcTypeCode(SqlTypes.VARBINARY)`, was bei PostgreSQL
zum normalen `bytea`-Typ führt (kein Large Object, keine Transaktions-Falle).

## WICHTIG: Die Datenbank muss einmalig angepasst werden

`spring.jpa.hibernate.ddl-auto=update` legt nur **neue** Spalten/Tabellen an.
Es ändert **keine bestehenden Spaltentypen**. Die `image_data`-Spalte in deiner
Render-Datenbank ist also noch vom alten Typ (`oid`) und muss einmalig
korrigiert werden.

Du hast zwei Möglichkeiten:

### Option A — Datenbank zurücksetzen (am einfachsten)

Wenn dir die Demo-Daten egal sind (es sind ja nur Test-Tiere und -Konten),
ist das der schnellste Weg:

1. Render-Dashboard → deine PostgreSQL-Instanz `pawsitters-db`
2. Tab **"Info"** → unten **"Delete Database"** ODER einfacher:
   Verbinde dich über den **"Connect" → "External Connection"**-Befehl
   mit `psql` und führe aus:
   ```sql
   DROP TABLE pets CASCADE;
   ```
3. Den neuen Code deployen (Git push). Beim Start legt Hibernate die
   `pets`-Tabelle mit dem korrekten `bytea`-Typ neu an, und der
   DataInitializer befüllt die Demo-Daten erneut.

### Option B — Nur die Spalte migrieren (Daten bleiben erhalten)

Wenn du bereits echte Daten hast, die du behalten willst:

1. Mit der Datenbank verbinden (Render-Dashboard → `pawsitters-db` →
   "Connect" → den `psql`-Befehl unter "External Connection" kopieren
   und im Terminal ausführen)
2. Folgendes SQL ausführen:
   ```sql
   -- Alte Large-Object-Spalte entfernen (Bilder gehen dabei verloren)
   ALTER TABLE pets DROP COLUMN IF EXISTS image_data;
   ```
3. Den neuen Code deployen. Hibernate legt die `image_data`-Spalte mit
   dem korrekten `bytea`-Typ neu an. Bestehende Tiere bleiben erhalten,
   nur ihre (alten) Bilder sind weg und müssen neu hochgeladen werden.

> Hinweis: In beiden Fällen gehen die bereits hochgeladenen Bilder verloren,
> weil sie im alten Large-Object-Format vorliegen. Die Tiere selbst bleibt
> bei Option B erhalten. Nach dem Fix kannst du Bilder ganz normal neu
> hochladen — dann liegen sie im korrekten `bytea`-Format und werden
> fehlerfrei angezeigt.

## Danach

Nach dem Deploy und der DB-Anpassung sollte der Bild-Upload auf Render
genauso funktionieren wie lokal. Teste es mit einem frisch hochgeladenen Bild.

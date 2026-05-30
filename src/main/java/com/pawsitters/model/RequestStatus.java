package com.pawsitters.model;

/**
 * Status einer Betreuungsanfrage.
 *
 * OPEN        - Anfrage wurde erstellt und ist offen für Angebote
 * IN_PROGRESS - Mindestens ein Angebot wurde gesendet, noch keins angenommen
 * MATCHED     - Tierhalter hat ein Angebot angenommen
 * CANCELLED   - Anfrage wurde abgebrochen
 */
public enum RequestStatus {
    OPEN,
    IN_PROGRESS,
    MATCHED,
    CANCELLED
}

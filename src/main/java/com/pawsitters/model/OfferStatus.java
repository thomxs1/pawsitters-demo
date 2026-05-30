package com.pawsitters.model;

/**
 * Status eines Angebots eines Gastgebers an einen Tierhalter.
 *
 * PENDING  - Angebot wurde gesendet, noch keine Entscheidung getroffen
 * ACCEPTED - Angebot wurde vom Tierhalter angenommen
 * REJECTED - Angebot wurde vom Tierhalter abgelehnt
 */
public enum OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

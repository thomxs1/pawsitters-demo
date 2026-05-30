package com.pawsitters.model;

/**
 * Rolle eines registrierten Nutzers.
 * Ein Nutzer kann mehrere Rollen gleichzeitig haben (z. B. selbst Tierhalter
 * sein UND Gastgeber). ADMIN ist für Plattform-Administration reserviert.
 */
public enum UserRole {
    OWNER,   // Tierhalter
    HOST,    // Gastgeber
    ADMIN    // Plattform-Admin
}

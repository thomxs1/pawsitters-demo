package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Betreuungsanfrage (CareRequest).
 * Wird von einem Tierhalter erstellt und legt fest, welches Haustier
 * in welchem Zeitraum betreut werden soll.
 */
@Entity
@Table(name = "care_requests")
public class CareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Haustier muss ausgewählt sein")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @NotNull(message = "Startdatum erforderlich")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Enddatum erforderlich")
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.OPEN;

    // Alle Angebote, die zu dieser Anfrage gesendet wurden
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offer> offers = new ArrayList<>();

    public CareRequest() {}

    public CareRequest(Pet pet, LocalDate startDate, LocalDate endDate) {
        this.pet = pet;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = RequestStatus.OPEN;
    }

    /**
     * Validiert, dass das Enddatum nach dem Startdatum liegt.
     */
    public boolean isValidPeriod() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }

    // Getter und Setter

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public List<Offer> getOffers() { return offers; }
    public void setOffers(List<Offer> offers) { this.offers = offers; }
}

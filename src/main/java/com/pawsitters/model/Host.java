package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Gastgeber (Host).
 * Ein Gastgeber legt fest, welche Tierarten er aufnehmen kann,
 * in welchem Zeitraum er verfügbar ist und welchen Preis er pro Woche verlangt.
 */
@Entity
@Table(name = "hosts")
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 1000)
    private String description;

    // Welche Tierarten der Gastgeber aufnimmt
    @ElementCollection(targetClass = AnimalType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "host_accepted_animals", joinColumns = @JoinColumn(name = "host_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type")
    private Set<AnimalType> acceptedAnimals = new HashSet<>();

    // Verfügbarkeitszeitraum
    private LocalDate availableFrom;
    private LocalDate availableUntil;

    @PositiveOrZero(message = "Preis darf nicht negativ sein")
    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerWeek;

    public Host() {}

    public Host(String name, String email, String description,
                Set<AnimalType> acceptedAnimals,
                LocalDate availableFrom, LocalDate availableUntil,
                BigDecimal pricePerWeek) {
        this.name = name;
        this.email = email;
        this.description = description;
        this.acceptedAnimals = acceptedAnimals;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.pricePerWeek = pricePerWeek;
    }

    /**
     * Prüft, ob der Gastgeber den gewünschten Zeitraum komplett abdeckt
     * und die geforderte Tierart akzeptiert.
     */
    public boolean canAccommodate(AnimalType animalType, LocalDate from, LocalDate until) {
        if (acceptedAnimals == null || !acceptedAnimals.contains(animalType)) {
            return false;
        }
        if (availableFrom == null || availableUntil == null) {
            return false;
        }
        // Verfügbarkeit muss den Anfragezeitraum vollständig einschliessen
        return !from.isBefore(availableFrom) && !until.isAfter(availableUntil);
    }

    // Getter und Setter

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<AnimalType> getAcceptedAnimals() { return acceptedAnimals; }
    public void setAcceptedAnimals(Set<AnimalType> acceptedAnimals) { this.acceptedAnimals = acceptedAnimals; }

    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }

    public LocalDate getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(LocalDate availableUntil) { this.availableUntil = availableUntil; }

    public BigDecimal getPricePerWeek() { return pricePerWeek; }
    public void setPricePerWeek(BigDecimal pricePerWeek) { this.pricePerWeek = pricePerWeek; }
}

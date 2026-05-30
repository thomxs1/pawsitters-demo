package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Tierhalter (Pet Owner).
 * Ein Tierhalter hat ein Profil, kann Haustiere registrieren und
 * Betreuungsanfragen erstellen.
 */
@Entity
@Table(name = "pet_owners")
public class PetOwner {

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

    // Ein Tierhalter kann mehrere Haustiere besitzen
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    public PetOwner() {}

    public PetOwner(String name, String email, String description) {
        this.name = name;
        this.email = email;
        this.description = description;
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

    public List<Pet> getPets() { return pets; }
    public void setPets(List<Pet> pets) { this.pets = pets; }
}

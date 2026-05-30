package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Haustier (Pet).
 * Jedes Haustier gehört genau einem Tierhalter.
 */
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name des Haustiers darf nicht leer sein")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Tierart muss angegeben werden")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalType animalType;

    private Integer age;

    @Column(length = 1000)
    private String notes;

    // Besitzer des Haustiers
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private PetOwner owner;

    public Pet() {}

    public Pet(String name, AnimalType animalType, Integer age, String notes, PetOwner owner) {
        this.name = name;
        this.animalType = animalType;
        this.age = age;
        this.notes = notes;
        this.owner = owner;
    }

    // Getter und Setter

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AnimalType getAnimalType() { return animalType; }
    public void setAnimalType(AnimalType animalType) { this.animalType = animalType; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public PetOwner getOwner() { return owner; }
    public void setOwner(PetOwner owner) { this.owner = owner; }
}

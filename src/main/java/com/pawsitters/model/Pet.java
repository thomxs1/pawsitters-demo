package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    // Optionales Profilbild des Tiers.
    // WICHTIG: @JdbcTypeCode(VARBINARY) statt @Lob! Bei PostgreSQL wuerde @Lob auf
    // byte[] den "Large Object"-Mechanismus (oid) nutzen, der eine aktive Transaktion
    // zum Lesen braucht - das fuehrt beim Bild-Streaming zu 500-Fehlern. VARBINARY
    // mappt sauber auf bytea (PostgreSQL) bzw. VARBINARY (H2) - ohne columnDefinition,
    // damit Hibernate pro Datenbank den passenden Typ waehlt.
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "image_data", length = 10_000_000)
    private byte[] imageData;

    @Column(name = "image_content_type", length = 64)
    private String imageContentType;

    public Pet() {}

    public Pet(String name, AnimalType animalType, Integer age, String notes, PetOwner owner) {
        this.name = name;
        this.animalType = animalType;
        this.age = age;
        this.notes = notes;
        this.owner = owner;
    }

    /**
     * True, wenn das Pet ein Profilbild hat. Prueft nur den ContentType,
     * damit nicht unnoetig die Bild-Bytes inspiziert werden muessen.
     */
    public boolean hasImage() {
        return imageContentType != null && !imageContentType.isBlank();
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

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
}

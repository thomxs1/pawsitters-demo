package com.pawsitters.service;

import com.pawsitters.model.Pet;
import com.pawsitters.model.PetOwner;
import com.pawsitters.repository.PetOwnerRepository;
import com.pawsitters.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service fuer die Verwaltung von Haustieren.
 */
@Service
public class PetService {

    /** Maximale Bild-Groesse: 5 MB */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /** Erlaubte Bild-Formate */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    public PetService(PetRepository petRepository, PetOwnerRepository petOwnerRepository) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Registriert ein neues Haustier fuer einen bestehenden Tierhalter.
     */
    public Pet register(Pet pet, Long ownerId) {
        PetOwner owner = petOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Tierhalter mit ID " + ownerId + " nicht gefunden"));
        if (pet.getName() == null || pet.getName().isBlank()) {
            throw new IllegalArgumentException("Name des Haustiers ist erforderlich");
        }
        if (pet.getAnimalType() == null) {
            throw new IllegalArgumentException("Tierart muss angegeben werden");
        }
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    /**
     * Registriert ein Pet mit Bild. Validiert Groesse und Format des Bildes.
     */
    public Pet registerWithImage(Pet pet, Long ownerId, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            validateImage(image);
            pet.setImageData(image.getBytes());
            pet.setImageContentType(image.getContentType());
        }
        return register(pet, ownerId);
    }

    /**
     * Aktualisiert das Bild eines existierenden Pets.
     */
    public Pet updateImage(Long petId, MultipartFile image) throws IOException {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Tier nicht gefunden"));
        validateImage(image);
        pet.setImageData(image.getBytes());
        pet.setImageContentType(image.getContentType());
        return petRepository.save(pet);
    }

    /**
     * Entfernt das Bild eines Pets.
     */
    public Pet removeImage(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Tier nicht gefunden"));
        pet.setImageData(null);
        pet.setImageContentType(null);
        return petRepository.save(pet);
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Bilddatei ist leer");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Bild ist zu gross (maximal 5 MB erlaubt)");
        }
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Nur JPG, PNG, WEBP und GIF werden unterstuetzt");
        }
    }

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public List<Pet> findByOwner(Long ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }

    public Optional<Pet> findById(Long id) {
        return petRepository.findById(id);
    }

    public void deleteById(Long id) {
        petRepository.deleteById(id);
    }
}

package com.pawsitters.service;

import com.pawsitters.model.PetOwner;
import com.pawsitters.repository.PetOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service für Tierhalter (PetOwner).
 * Kapselt die Geschäftslogik für Erstellung, Aenderung und Abfrage von Tierhaltern.
 */
@Service
public class PetOwnerService {

    private final PetOwnerRepository petOwnerRepository;

    public PetOwnerService(PetOwnerRepository petOwnerRepository) {
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Erstellt einen neuen Tierhalter.
     * @throws IllegalArgumentException wenn die E-Mail bereits existiert
     */
    public PetOwner create(PetOwner owner) {
        if (owner.getEmail() == null || owner.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-Mail ist erforderlich");
        }
        if (petOwnerRepository.findByEmail(owner.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ein Tierhalter mit dieser E-Mail existiert bereits");
        }
        return petOwnerRepository.save(owner);
    }

    public List<PetOwner> findAll() {
        return petOwnerRepository.findAll();
    }

    public Optional<PetOwner> findById(Long id) {
        return petOwnerRepository.findById(id);
    }

    public void deleteById(Long id) {
        petOwnerRepository.deleteById(id);
    }
}

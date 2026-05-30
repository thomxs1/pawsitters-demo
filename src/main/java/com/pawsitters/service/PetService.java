package com.pawsitters.service;

import com.pawsitters.model.Pet;
import com.pawsitters.model.PetOwner;
import com.pawsitters.repository.PetOwnerRepository;
import com.pawsitters.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service für die Verwaltung von Haustieren.
 */
@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    public PetService(PetRepository petRepository, PetOwnerRepository petOwnerRepository) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Registriert ein neues Haustier für einen bestehenden Tierhalter.
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

package com.pawsitters.service;

import com.pawsitters.model.AnimalType;
import com.pawsitters.model.Pet;
import com.pawsitters.model.PetOwner;
import com.pawsitters.repository.PetOwnerRepository;
import com.pawsitters.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für PetService.
 */
class PetServiceTest {

    private PetRepository petRepository;
    private PetOwnerRepository ownerRepository;
    private PetService service;

    @BeforeEach
    void setUp() {
        petRepository = mock(PetRepository.class);
        ownerRepository = mock(PetOwnerRepository.class);
        service = new PetService(petRepository, ownerRepository);
    }

    @Test
    void register_validPet_setsOwnerAndSaves() {
        PetOwner owner = new PetOwner("Carla", "c@example.com", "");
        owner.setId(1L);
        Pet pet = new Pet("Rex", AnimalType.DOG, 3, "Lieb", null);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(petRepository.save(pet)).thenReturn(pet);

        Pet result = service.register(pet, 1L);

        assertEquals(owner, result.getOwner());
        verify(petRepository).save(pet);
    }

    @Test
    void register_unknownOwner_throwsException() {
        Pet pet = new Pet("Rex", AnimalType.DOG, 3, null, null);
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.register(pet, 99L));
        verify(petRepository, never()).save(any());
    }

    @Test
    void register_missingAnimalType_throwsException() {
        PetOwner owner = new PetOwner("Carla", "c@example.com", "");
        Pet pet = new Pet("Rex", null, 3, null, null);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () -> service.register(pet, 1L));
    }
}

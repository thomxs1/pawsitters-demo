package com.pawsitters.service;

import com.pawsitters.model.PetOwner;
import com.pawsitters.repository.PetOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für PetOwnerService.
 */
class PetOwnerServiceTest {

    private PetOwnerRepository repository;
    private PetOwnerService service;

    @BeforeEach
    void setUp() {
        repository = mock(PetOwnerRepository.class);
        service = new PetOwnerService(repository);
    }

    @Test
    void create_validOwner_savesAndReturnsOwner() {
        PetOwner owner = new PetOwner("Anna", "anna@example.com", "Liebt Hunde");
        when(repository.findByEmail("anna@example.com")).thenReturn(Optional.empty());
        when(repository.save(owner)).thenReturn(owner);

        PetOwner result = service.create(owner);

        assertEquals("Anna", result.getName());
        verify(repository).save(owner);
    }

    @Test
    void create_duplicateEmail_throwsException() {
        PetOwner existing = new PetOwner("Bob", "bob@example.com", "");
        PetOwner duplicate = new PetOwner("Other Bob", "bob@example.com", "");
        when(repository.findByEmail("bob@example.com")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(duplicate)
        );
        assertTrue(ex.getMessage().contains("existiert bereits"));
        verify(repository, never()).save(any());
    }

    @Test
    void create_blankEmail_throwsException() {
        PetOwner owner = new PetOwner("NoEmail", "", "");
        assertThrows(IllegalArgumentException.class, () -> service.create(owner));
    }
}

package com.pawsitters.service;

import com.pawsitters.model.*;
import com.pawsitters.repository.CareRequestRepository;
import com.pawsitters.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für CareRequestService.
 */
class CareRequestServiceTest {

    private CareRequestRepository requestRepository;
    private PetRepository petRepository;
    private HostService hostService;
    private CareRequestService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(CareRequestRepository.class);
        petRepository = mock(PetRepository.class);
        hostService = mock(HostService.class);
        service = new CareRequestService(requestRepository, petRepository, hostService);
    }

    @Test
    void create_validRequest_setsStatusOpen() {
        PetOwner owner = new PetOwner("Test", "t@example.com", "");
        Pet pet = new Pet("Bello", AnimalType.DOG, 2, null, owner);
        pet.setId(1L);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(requestRepository.save(any(CareRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(20);

        CareRequest result = service.create(1L, start, end);

        assertEquals(RequestStatus.OPEN, result.getStatus());
        assertEquals(start, result.getStartDate());
    }

    @Test
    void create_endBeforeStart_throwsException() {
        PetOwner owner = new PetOwner("Test", "t@example.com", "");
        Pet pet = new Pet("Bello", AnimalType.DOG, 2, null, owner);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(1L,
                        LocalDate.now().plusDays(20),
                        LocalDate.now().plusDays(10)));
    }

    @Test
    void create_startInPast_throwsException() {
        PetOwner owner = new PetOwner("Test", "t@example.com", "");
        Pet pet = new Pet("Bello", AnimalType.DOG, 2, null, owner);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(1L,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(5)));
    }

    @Test
    void updateStatus_setsNewStatus() {
        CareRequest request = new CareRequest();
        request.setId(7L);
        request.setStatus(RequestStatus.OPEN);

        when(requestRepository.findById(7L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        CareRequest result = service.updateStatus(7L, RequestStatus.CANCELLED);

        assertEquals(RequestStatus.CANCELLED, result.getStatus());
    }
}

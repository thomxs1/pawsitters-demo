package com.pawsitters.service;

import com.pawsitters.model.*;
import com.pawsitters.repository.CareRequestRepository;
import com.pawsitters.repository.HostRepository;
import com.pawsitters.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für OfferService.
 * Hier liegt die Kernlogik:
 *   - Senden eines Angebots
 *   - Annahme (alle anderen werden REJECTED, Anfrage MATCHED)
 *   - Ablehnung
 */
class OfferServiceTest {

    private OfferRepository offerRepository;
    private HostRepository hostRepository;
    private CareRequestRepository requestRepository;
    private OfferService service;

    private Host host;
    private CareRequest request;

    @BeforeEach
    void setUp() {
        offerRepository = mock(OfferRepository.class);
        hostRepository = mock(HostRepository.class);
        requestRepository = mock(CareRequestRepository.class);
        service = new OfferService(offerRepository, hostRepository, requestRepository);

        // Gemeinsames Setup: Tierhalter, Hund, Anfrage, Gastgeber
        PetOwner owner = new PetOwner("Owner", "o@example.com", "");
        Pet dog = new Pet("Bello", AnimalType.DOG, 3, "", owner);

        request = new CareRequest(dog,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 20));
        request.setId(100L);

        host = new Host("Sitter", "s@example.com", "",
                Set.of(AnimalType.DOG),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                new BigDecimal("100"));
        host.setId(1L);

        when(hostRepository.findById(1L)).thenReturn(Optional.of(host));
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(inv -> {
                    Offer o = inv.getArgument(0);
                    if (o.getId() == null) o.setId(System.currentTimeMillis());
                    return o;
                });
        when(requestRepository.save(any(CareRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOffer_validOffer_savesAndUpdatesRequestStatus() {
        Offer result = service.createOffer(1L, 100L, new BigDecimal("150"), "Gerne!");

        assertEquals(OfferStatus.PENDING, result.getStatus());
        assertEquals(host, result.getHost());
        // Beim ersten Angebot soll Status von OPEN auf IN_PROGRESS gehen
        assertEquals(RequestStatus.IN_PROGRESS, request.getStatus());
    }

    @Test
    void createOffer_animalTypeMismatch_throwsException() {
        // Gastgeber akzeptiert nur Katzen
        host.setAcceptedAnimals(Set.of(AnimalType.CAT));

        assertThrows(IllegalArgumentException.class,
                () -> service.createOffer(1L, 100L, new BigDecimal("150"), ""));
    }

    @Test
    void createOffer_requestAlreadyMatched_throwsException() {
        request.setStatus(RequestStatus.MATCHED);

        assertThrows(IllegalArgumentException.class,
                () -> service.createOffer(1L, 100L, new BigDecimal("150"), ""));
    }

    @Test
    void createOffer_negativePrice_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createOffer(1L, 100L, new BigDecimal("-1"), ""));
    }

    @Test
    void acceptOffer_acceptsAndRejectsSiblings() {
        // Drei Angebote zur selben Anfrage
        Offer accepted = new Offer(host, request, new BigDecimal("150"), "");
        accepted.setId(10L);
        accepted.setStatus(OfferStatus.PENDING);

        Offer other1 = new Offer(host, request, new BigDecimal("160"), "");
        other1.setId(11L);
        other1.setStatus(OfferStatus.PENDING);

        Offer other2 = new Offer(host, request, new BigDecimal("170"), "");
        other2.setId(12L);
        other2.setStatus(OfferStatus.PENDING);

        when(offerRepository.findById(10L)).thenReturn(Optional.of(accepted));
        when(offerRepository.findByRequestId(100L))
                .thenReturn(List.of(accepted, other1, other2));

        Offer result = service.acceptOffer(10L);

        assertEquals(OfferStatus.ACCEPTED, result.getStatus());
        assertEquals(OfferStatus.REJECTED, other1.getStatus());
        assertEquals(OfferStatus.REJECTED, other2.getStatus());
        assertEquals(RequestStatus.MATCHED, request.getStatus());
    }

    @Test
    void acceptOffer_alreadyRejected_throwsException() {
        Offer offer = new Offer(host, request, new BigDecimal("100"), "");
        offer.setId(20L);
        offer.setStatus(OfferStatus.REJECTED);
        when(offerRepository.findById(20L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.acceptOffer(20L));
    }

    @Test
    void rejectOffer_setsStatusToRejected() {
        Offer offer = new Offer(host, request, new BigDecimal("100"), "");
        offer.setId(30L);
        offer.setStatus(OfferStatus.PENDING);
        when(offerRepository.findById(30L)).thenReturn(Optional.of(offer));

        Offer result = service.rejectOffer(30L);

        assertEquals(OfferStatus.REJECTED, result.getStatus());
    }
}

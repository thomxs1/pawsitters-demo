package com.pawsitters.integration;

import com.pawsitters.model.*;
import com.pawsitters.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest: lädt den vollständigen Spring-Kontext und
 * verifiziert den End-to-End-Fluss über alle Services und die H2-DB.
 *
 * Bonuspunkte für Integrationstests laut Projektbeschreibung.
 *
 * Im "test"-Profil ist der DataInitializer deaktiviert, damit der Test
 * mit einer leeren Datenbank startet und Email-Eindeutigkeit nicht kollidiert.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PawsittersIntegrationTest {

    @Autowired
    private PetOwnerService ownerService;
    @Autowired
    private HostService hostService;
    @Autowired
    private PetService petService;
    @Autowired
    private CareRequestService requestService;
    @Autowired
    private OfferService offerService;

    @Test
    void completeFlow_ownerCreatesRequest_hostsSendOffers_ownerAcceptsOne() {
        // 1. Tierhalter anlegen
        PetOwner owner = ownerService.create(
                new PetOwner("Erika", "erika@example.com", "Reist gerne"));

        // 2. Haustier registrieren
        Pet pet = petService.register(
                new Pet("Bello", AnimalType.DOG, 4, "Lieb", null),
                owner.getId());

        // 3. Zwei Gastgeber anlegen
        Set<AnimalType> dogs = new HashSet<>();
        dogs.add(AnimalType.DOG);

        Host host1 = hostService.create(new Host("Sitter A", "a@example.com", "",
                dogs,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(60),
                new BigDecimal("100")));

        Host host2 = hostService.create(new Host("Sitter B", "b@example.com", "",
                new HashSet<>(Set.of(AnimalType.DOG)),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(60),
                new BigDecimal("120")));

        // 4. Anfrage erstellen
        CareRequest request = requestService.create(
                pet.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(20));

        assertEquals(RequestStatus.OPEN, request.getStatus());

        // 5. Beide Gastgeber senden ein Angebot
        Offer offer1 = offerService.createOffer(host1.getId(), request.getId(),
                new BigDecimal("150"), "Gerne!");
        Offer offer2 = offerService.createOffer(host2.getId(), request.getId(),
                new BigDecimal("170"), "Auch dabei");

        // Anfrage muss jetzt IN_PROGRESS sein
        CareRequest updated = requestService.findById(request.getId()).orElseThrow();
        assertEquals(RequestStatus.IN_PROGRESS, updated.getStatus());

        // 6. Tierhalter nimmt Angebot 1 an
        offerService.acceptOffer(offer1.getId());

        // 7. Verifikation:
        //    - offer1 ist ACCEPTED
        //    - offer2 ist automatisch REJECTED
        //    - request ist MATCHED
        Offer reloadedOffer1 = offerService.findById(offer1.getId()).orElseThrow();
        Offer reloadedOffer2 = offerService.findById(offer2.getId()).orElseThrow();
        CareRequest reloadedRequest = requestService.findById(request.getId()).orElseThrow();

        assertEquals(OfferStatus.ACCEPTED, reloadedOffer1.getStatus());
        assertEquals(OfferStatus.REJECTED, reloadedOffer2.getStatus());
        assertEquals(RequestStatus.MATCHED, reloadedRequest.getStatus());
    }

    @Test
    void findMatchingHosts_returnsOnlyHostsThatFitAnimalAndPeriod() {
        Host catSitter = hostService.create(new Host("Cat Lady", "cats@example.com", "",
                new HashSet<>(Set.of(AnimalType.CAT)),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(60),
                new BigDecimal("80")));

        Host dogSitter = hostService.create(new Host("Dog Dude", "dogs@example.com", "",
                new HashSet<>(Set.of(AnimalType.DOG)),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(60),
                new BigDecimal("100")));

        // Tierhalter mit Hund erstellt Anfrage
        PetOwner owner = ownerService.create(new PetOwner("Max", "max@example.com", ""));
        Pet dog = petService.register(
                new Pet("Rex", AnimalType.DOG, 5, "", null),
                owner.getId());
        CareRequest req = requestService.create(dog.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15));

        List<Host> matches = requestService.findMatchingHostsForRequest(req.getId());

        // Nur Dog-Sitter darf zurückkommen
        assertTrue(matches.stream().anyMatch(h -> h.getEmail().equals("dogs@example.com")));
        assertFalse(matches.stream().anyMatch(h -> h.getEmail().equals("cats@example.com")));
    }
}

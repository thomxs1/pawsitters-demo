package com.pawsitters.service;

import com.pawsitters.model.*;
import com.pawsitters.repository.CareRequestRepository;
import com.pawsitters.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service für Betreuungsanfragen.
 * Verantwortlich für Erstellung, Abfrage und Statusaktualisierung.
 */
@Service
public class CareRequestService {

    private final CareRequestRepository requestRepository;
    private final PetRepository petRepository;
    private final HostService hostService;

    public CareRequestService(CareRequestRepository requestRepository,
                              PetRepository petRepository,
                              HostService hostService) {
        this.requestRepository = requestRepository;
        this.petRepository = petRepository;
        this.hostService = hostService;
    }

    /**
     * Erstellt eine neue Betreuungsanfrage für ein bestehendes Haustier.
     *
     * @throws IllegalArgumentException bei ungültigem Zeitraum oder unbekanntem Tier
     */
    public CareRequest create(Long petId, LocalDate startDate, LocalDate endDate) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Haustier mit ID " + petId + " nicht gefunden"));
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start- und Enddatum sind erforderlich");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Enddatum darf nicht vor dem Startdatum liegen");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Startdatum darf nicht in der Vergangenheit liegen");
        }
        CareRequest request = new CareRequest(pet, startDate, endDate);
        return requestRepository.save(request);
    }

    public List<CareRequest> findAll() {
        return requestRepository.findAll();
    }

    public List<CareRequest> findOpenRequests() {
        return requestRepository.findByStatus(RequestStatus.OPEN);
    }

    public Optional<CareRequest> findById(Long id) {
        return requestRepository.findById(id);
    }

    /**
     * Aktualisiert den Status einer Anfrage.
     */
    public CareRequest updateStatus(Long requestId, RequestStatus newStatus) {
        CareRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden: " + requestId));
        request.setStatus(newStatus);
        return requestRepository.save(request);
    }

    /**
     * Findet alle Gastgeber, die zu einer Anfrage passen (Tierart + Zeitraum).
     */
    public List<Host> findMatchingHostsForRequest(Long requestId) {
        CareRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden: " + requestId));
        return hostService.findMatchingHosts(
                request.getPet().getAnimalType(),
                request.getStartDate(),
                request.getEndDate()
        );
    }

    public CareRequest save(CareRequest request) {
        return requestRepository.save(request);
    }
}

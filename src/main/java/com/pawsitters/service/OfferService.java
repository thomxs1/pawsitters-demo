package com.pawsitters.service;

import com.pawsitters.model.*;
import com.pawsitters.repository.CareRequestRepository;
import com.pawsitters.repository.HostRepository;
import com.pawsitters.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service für Angebote (Offers).
 *
 * Kernlogik:
 *  - Ein Gastgeber kann Angebote an Tierhalter senden
 *  - Der Tierhalter kann ein Angebot annehmen; alle anderen Angebote
 *    zur gleichen Anfrage werden automatisch abgelehnt
 *  - Beim Annehmen wird der Status der Anfrage auf MATCHED gesetzt
 */
@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final HostRepository hostRepository;
    private final CareRequestRepository requestRepository;

    public OfferService(OfferRepository offerRepository,
                        HostRepository hostRepository,
                        CareRequestRepository requestRepository) {
        this.offerRepository = offerRepository;
        this.hostRepository = hostRepository;
        this.requestRepository = requestRepository;
    }

    /**
     * Ein Gastgeber sendet ein Angebot zu einer offenen Anfrage.
     *
     * @throws IllegalArgumentException wenn die Anfrage nicht mehr offen ist
     *         oder Tierart/Zeitraum nicht passen.
     */
    @Transactional
    public Offer createOffer(Long hostId, Long requestId, BigDecimal totalPrice, String message) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new IllegalArgumentException("Gastgeber nicht gefunden: " + hostId));
        CareRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden: " + requestId));

        if (request.getStatus() == RequestStatus.MATCHED) {
            throw new IllegalArgumentException("Anfrage hat bereits einen passenden Gastgeber");
        }
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new IllegalArgumentException("Anfrage wurde abgebrochen");
        }
        if (!host.canAccommodate(request.getPet().getAnimalType(),
                                  request.getStartDate(),
                                  request.getEndDate())) {
            throw new IllegalArgumentException(
                    "Gastgeber kann die Tierart oder den Zeitraum nicht abdecken");
        }
        if (totalPrice == null || totalPrice.signum() < 0) {
            throw new IllegalArgumentException("Preis darf nicht negativ sein");
        }

        Offer offer = new Offer(host, request, totalPrice, message);
        Offer saved = offerRepository.save(offer);

        // Sobald mindestens ein Angebot existiert: Status auf IN_PROGRESS
        if (request.getStatus() == RequestStatus.OPEN) {
            request.setStatus(RequestStatus.IN_PROGRESS);
            requestRepository.save(request);
        }
        return saved;
    }

    /**
     * Tierhalter nimmt ein Angebot an.
     * Alle anderen Angebote der gleichen Anfrage werden auf REJECTED gesetzt.
     * Die Anfrage selbst erhält den Status MATCHED.
     */
    @Transactional
    public Offer acceptOffer(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Angebot nicht gefunden: " + offerId));

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalArgumentException("Nur ausstehende Angebote können angenommen werden");
        }

        CareRequest request = offer.getRequest();
        if (request.getStatus() == RequestStatus.MATCHED) {
            throw new IllegalArgumentException("Diese Anfrage hat bereits ein angenommenes Angebot");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        // Alle anderen Angebote zu derselben Anfrage automatisch ablehnen
        List<Offer> siblings = offerRepository.findByRequestId(request.getId());
        for (Offer other : siblings) {
            if (!other.getId().equals(offer.getId()) && other.getStatus() == OfferStatus.PENDING) {
                other.setStatus(OfferStatus.REJECTED);
                offerRepository.save(other);
            }
        }

        // Anfragestatus aktualisieren
        request.setStatus(RequestStatus.MATCHED);
        requestRepository.save(request);

        return offer;
    }

    /**
     * Tierhalter lehnt ein einzelnes Angebot ab (ohne anderes anzunehmen).
     */
    @Transactional
    public Offer rejectOffer(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Angebot nicht gefunden: " + offerId));
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalArgumentException("Nur ausstehende Angebote können abgelehnt werden");
        }
        offer.setStatus(OfferStatus.REJECTED);
        return offerRepository.save(offer);
    }

    public List<Offer> findAll() {
        return offerRepository.findAll();
    }

    public List<Offer> findByRequest(Long requestId) {
        return offerRepository.findByRequestId(requestId);
    }

    public Optional<Offer> findById(Long id) {
        return offerRepository.findById(id);
    }
}

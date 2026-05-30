package com.pawsitters.controller;

import com.pawsitters.model.Offer;
import com.pawsitters.model.RequestStatus;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.CareRequestService;
import com.pawsitters.service.OfferService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;
    private final CareRequestService requestService;

    public OfferController(OfferService offerService,
                           CareRequestService requestService) {
        this.offerService = offerService;
        this.requestService = requestService;
    }

    /**
     * Formular für den eingeloggten Host, um ein Angebot zu einer Anfrage zu senden.
     */
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                          @RequestParam Long requestId, Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.HOST) || user.getHostProfile() == null) {
            throw new AccessDeniedException("Nur Gastgeber können Angebote senden.");
        }
        var request = requestService.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden"));
        if (request.getStatus() != RequestStatus.OPEN && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Anfrage akzeptiert keine Angebote mehr.");
        }
        model.addAttribute("requestId", requestId);
        model.addAttribute("request", request);
        model.addAttribute("hostProfile", user.getHostProfile());
        return "offers/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @RequestParam Long requestId,
                         @RequestParam BigDecimal totalPrice,
                         @RequestParam(required = false) String message,
                         Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.HOST) || user.getHostProfile() == null) {
            throw new AccessDeniedException("Nur Gastgeber können Angebote senden.");
        }
        Long hostId = user.getHostProfile().getId();
        try {
            offerService.createOffer(hostId, requestId, totalPrice, message);
            return "redirect:/requests/" + requestId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("requestId", requestId);
            model.addAttribute("request", requestService.findById(requestId).orElse(null));
            model.addAttribute("hostProfile", user.getHostProfile());
            return "offers/form";
        }
    }

    /**
     * Annahme eines Angebots durch den Tierhalter.
     * Alle anderen Angebote zur gleichen Anfrage werden automatisch abgelehnt.
     */
    @PostMapping("/{id}/accept")
    public String accept(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Angebot nicht gefunden"));
        var user = principal.getUser();
        // Nur der Owner der zugehörigen Anfrage darf annehmen
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getOwnerProfile() == null
                    || !user.getOwnerProfile().getId().equals(offer.getRequest().getPet().getOwner().getId())) {
                throw new AccessDeniedException("Nur der Besitzer kann Angebote annehmen.");
            }
        }
        Offer accepted = offerService.acceptOffer(id);
        return "redirect:/requests/" + accepted.getRequest().getId();
    }

    /**
     * Manuelle Ablehnung eines Angebots durch den Tierhalter.
     */
    @PostMapping("/{id}/reject")
    public String reject(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Angebot nicht gefunden"));
        var user = principal.getUser();
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getOwnerProfile() == null
                    || !user.getOwnerProfile().getId().equals(offer.getRequest().getPet().getOwner().getId())) {
                throw new AccessDeniedException("Nur der Besitzer kann Angebote ablehnen.");
            }
        }
        Offer rejected = offerService.rejectOffer(id);
        return "redirect:/requests/" + rejected.getRequest().getId();
    }
}

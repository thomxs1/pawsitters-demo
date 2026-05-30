package com.pawsitters.controller;

import com.pawsitters.model.CareRequest;
import com.pawsitters.model.Offer;
import com.pawsitters.model.User;
import com.pawsitters.model.UserRole;
import com.pawsitters.repository.CareRequestRepository;
import com.pawsitters.repository.OfferRepository;
import com.pawsitters.repository.UserRepository;
import com.pawsitters.security.PawsittersUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Persönliches Dashboard für eingeloggte Nutzer.
 * Zeigt je nach Rolle die eigenen Haustiere, Anfragen und Angebote an.
 */
@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final CareRequestRepository requestRepository;
    private final OfferRepository offerRepository;

    public DashboardController(UserRepository userRepository,
                               CareRequestRepository requestRepository,
                               OfferRepository offerRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        // User in der aktuellen Transaction frisch laden, damit alle Lazy-Loads
        // (Pets, Offers, ...) gegen einen managed Entity laufen.
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht in DB gefunden"));
        model.addAttribute("user", user);

        if (user.hasRole(UserRole.OWNER) && user.getOwnerProfile() != null) {
            var owner = user.getOwnerProfile();
            model.addAttribute("ownerProfile", owner);
            model.addAttribute("ownerPets", owner.getPets());

            // Alle Anfragen, deren Pet diesem Owner gehört
            List<CareRequest> ownerRequests = requestRepository.findAll().stream()
                .filter(r -> r.getPet() != null
                          && r.getPet().getOwner() != null
                          && owner.getId().equals(r.getPet().getOwner().getId()))
                .toList();
            model.addAttribute("ownerRequests", ownerRequests);
        }

        if (user.hasRole(UserRole.HOST) && user.getHostProfile() != null) {
            var host = user.getHostProfile();
            model.addAttribute("hostProfile", host);

            // Alle Angebote dieses Hosts
            List<Offer> hostOffers = offerRepository.findAll().stream()
                .filter(o -> o.getHost() != null && host.getId().equals(o.getHost().getId()))
                .toList();
            model.addAttribute("hostOffers", hostOffers);
        }

        return "dashboard";
    }
}

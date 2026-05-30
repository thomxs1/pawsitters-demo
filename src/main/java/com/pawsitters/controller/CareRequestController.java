package com.pawsitters.controller;

import com.pawsitters.model.CareRequest;
import com.pawsitters.model.RequestStatus;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.CareRequestService;
import com.pawsitters.service.PetService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class CareRequestController {

    private final CareRequestService requestService;
    private final PetService petService;

    public CareRequestController(CareRequestService requestService, PetService petService) {
        this.requestService = requestService;
        this.petService = petService;
    }

    /**
     * Liste aller Anfragen. Hosts sehen offene Anfragen (Marketplace),
     * Owner sehen ihre eigenen, Admins sehen alle.
     */
    @GetMapping
    public String list(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        var user = principal.getUser();
        List<CareRequest> requests;
        if (user.hasRole(UserRole.ADMIN)) {
            requests = requestService.findAll();
        } else if (user.hasRole(UserRole.HOST)) {
            // Hosts sehen alle offenen + in Bearbeitung befindlichen Anfragen als Marktplatz
            requests = requestService.findAll().stream()
                    .filter(r -> r.getStatus() == RequestStatus.OPEN
                              || r.getStatus() == RequestStatus.IN_PROGRESS)
                    .toList();
        } else if (user.hasRole(UserRole.OWNER) && user.getOwnerProfile() != null) {
            var ownerId = user.getOwnerProfile().getId();
            requests = requestService.findAll().stream()
                    .filter(r -> r.getPet() != null && r.getPet().getOwner() != null
                              && ownerId.equals(r.getPet().getOwner().getId()))
                    .toList();
        } else {
            requests = List.of();
        }
        model.addAttribute("requests", requests);
        return "requests/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.OWNER) || user.getOwnerProfile() == null) {
            throw new AccessDeniedException("Nur Tierhalter können Anfragen erstellen.");
        }
        model.addAttribute("pets", petService.findByOwner(user.getOwnerProfile().getId()));
        return "requests/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @RequestParam Long petId,
                         @RequestParam String startDate,
                         @RequestParam String endDate,
                         Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.OWNER) || user.getOwnerProfile() == null) {
            throw new AccessDeniedException("Nur Tierhalter können Anfragen erstellen.");
        }
        // Prüfe ob das Pet wirklich dem User gehört
        var pet = petService.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Tier nicht gefunden"));
        if (!user.getOwnerProfile().getId().equals(pet.getOwner().getId())) {
            throw new AccessDeniedException("Du kannst nur für eigene Tiere Anfragen erstellen.");
        }
        try {
            requestService.create(petId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pets", petService.findByOwner(user.getOwnerProfile().getId()));
            return "requests/form";
        }
    }

    /**
     * Detailansicht einer Anfrage: zeigt vorhandene Angebote
     * und passende Gastgeber an.
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id, Model model) {
        CareRequest request = requestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden"));
        var user = principal.getUser();
        model.addAttribute("request", request);
        model.addAttribute("matchingHosts", requestService.findMatchingHostsForRequest(id));

        // Ist eingeloggter User der Owner dieser Anfrage?
        boolean isOwner = user.getOwnerProfile() != null
                && request.getPet() != null
                && request.getPet().getOwner() != null
                && user.getOwnerProfile().getId().equals(request.getPet().getOwner().getId());
        model.addAttribute("isOwnerOfRequest", isOwner);

        // Ist eingeloggter User Host und passt zur Anfrage?
        boolean canMakeOffer = user.hasRole(UserRole.HOST)
                && user.getHostProfile() != null
                && (request.getStatus() == RequestStatus.OPEN
                    || request.getStatus() == RequestStatus.IN_PROGRESS);
        model.addAttribute("canMakeOffer", canMakeOffer);
        if (canMakeOffer) {
            model.addAttribute("myHostId", user.getHostProfile().getId());
            // Hat User bereits ein Angebot zu dieser Anfrage abgegeben?
            boolean alreadyOffered = request.getOffers().stream()
                    .anyMatch(o -> o.getHost() != null
                            && user.getHostProfile().getId().equals(o.getHost().getId()));
            model.addAttribute("alreadyOffered", alreadyOffered);
        }
        return "requests/detail";
    }

    /**
     * Aktualisiert den Status einer Anfrage (z. B. CANCELLED).
     */
    @PostMapping("/{id}/status")
    public String updateStatus(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                               @PathVariable Long id,
                               @RequestParam RequestStatus status) {
        var user = principal.getUser();
        var request = requestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden"));
        // Nur der Owner darf den Status ändern (oder Admin)
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getOwnerProfile() == null
                    || !user.getOwnerProfile().getId().equals(request.getPet().getOwner().getId())) {
                throw new AccessDeniedException("Nur der Besitzer kann den Status ändern.");
            }
        }
        requestService.updateStatus(id, status);
        return "redirect:/requests/" + id;
    }
}

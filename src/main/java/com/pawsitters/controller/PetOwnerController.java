package com.pawsitters.controller;

import com.pawsitters.model.PetOwner;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.PetOwnerService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Verwaltung der Tierhalter-Profile.
 * Liste und Detail sind nur für Admins sichtbar (Plattform-Sicht).
 * Owner sehen ihr eigenes Profil im Dashboard.
 */
@Controller
@RequestMapping("/owners")
public class PetOwnerController {

    private final PetOwnerService service;

    public PetOwnerController(PetOwnerService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        if (!principal.getUser().hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Nur Admins sehen alle Tierhalter.");
        }
        model.addAttribute("owners", service.findAll());
        return "owners/list";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id, Model model) {
        var user = principal.getUser();
        PetOwner owner = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tierhalter nicht gefunden"));
        // Eigentum oder Admin
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getOwnerProfile() == null || !user.getOwnerProfile().getId().equals(owner.getId())) {
                throw new AccessDeniedException("Du kannst nur dein eigenes Profil sehen.");
            }
        }
        model.addAttribute("owner", owner);
        return "owners/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        if (!principal.getUser().hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Nur Admins können Tierhalter löschen.");
        }
        service.deleteById(id);
        return "redirect:/owners";
    }
}

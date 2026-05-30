package com.pawsitters.controller;

import com.pawsitters.model.AnimalType;
import com.pawsitters.model.Pet;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.PetService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * Liste der eigenen Tiere (oder aller für Admin).
     */
    @GetMapping
    public String list(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        var user = principal.getUser();
        List<Pet> pets;
        if (user.hasRole(UserRole.ADMIN)) {
            pets = petService.findAll();
        } else if (user.hasRole(UserRole.OWNER) && user.getOwnerProfile() != null) {
            pets = petService.findByOwner(user.getOwnerProfile().getId());
        } else {
            pets = List.of();
        }
        model.addAttribute("pets", pets);
        return "pets/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal PawsittersUserPrincipal principal, Model model) {
        if (!principal.getUser().hasRole(UserRole.OWNER)) {
            throw new AccessDeniedException("Nur Tierhalter können Tiere registrieren.");
        }
        model.addAttribute("pet", new Pet());
        model.addAttribute("animalTypes", AnimalType.values());
        return "pets/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @ModelAttribute Pet pet,
                         Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.OWNER) || user.getOwnerProfile() == null) {
            throw new AccessDeniedException("Nur Tierhalter können Tiere registrieren.");
        }
        try {
            petService.register(pet, user.getOwnerProfile().getId());
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pet", pet);
            model.addAttribute("animalTypes", AnimalType.values());
            return "pets/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        var user = principal.getUser();
        var pet = petService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tier nicht gefunden"));
        // Nur eigener Owner oder Admin
        if (!user.hasRole(UserRole.ADMIN)
                && (user.getOwnerProfile() == null
                    || !user.getOwnerProfile().getId().equals(pet.getOwner().getId()))) {
            throw new AccessDeniedException("Du kannst nur eigene Tiere löschen.");
        }
        petService.deleteById(id);
        return "redirect:/dashboard";
    }
}

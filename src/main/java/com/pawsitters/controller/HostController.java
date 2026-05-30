package com.pawsitters.controller;

import com.pawsitters.model.AnimalType;
import com.pawsitters.model.Host;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.HostService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/hosts")
public class HostController {

    private final HostService service;

    public HostController(HostService service) {
        this.service = service;
    }

    /**
     * Verzeichnis aller Gastgeber - öffentlich-ähnlich, jeder eingeloggte User darf es sehen.
     * Optionaler Filter nach Tierart via ?animal=DOG
     */
    @GetMapping
    public String list(@RequestParam(required = false) AnimalType animal, Model model) {
        var allHosts = service.findAll();
        if (animal != null) {
            allHosts = allHosts.stream()
                    .filter(h -> h.getAcceptedAnimals() != null && h.getAcceptedAnimals().contains(animal))
                    .toList();
        }
        model.addAttribute("hosts", allHosts);
        model.addAttribute("activeAnimal", animal);
        model.addAttribute("animalTypes", AnimalType.values());
        return "hosts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Host host = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gastgeber nicht gefunden"));
        model.addAttribute("host", host);
        return "hosts/detail";
    }

    /**
     * Edit-Form für das eigene Host-Profil.
     * Nur für den Host selbst oder Admin.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                           @PathVariable Long id, Model model) {
        var user = principal.getUser();
        Host host = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gastgeber nicht gefunden"));
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getHostProfile() == null || !user.getHostProfile().getId().equals(host.getId())) {
                throw new AccessDeniedException("Du kannst nur dein eigenes Host-Profil bearbeiten.");
            }
        }
        model.addAttribute("host", host);
        model.addAttribute("animalTypes", AnimalType.values());
        return "hosts/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) List<AnimalType> animals,
                         @RequestParam(required = false) String availableFrom,
                         @RequestParam(required = false) String availableUntil,
                         @RequestParam BigDecimal pricePerWeek,
                         Model model) {
        var user = principal.getUser();
        Host host = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gastgeber nicht gefunden"));
        if (!user.hasRole(UserRole.ADMIN)) {
            if (user.getHostProfile() == null || !user.getHostProfile().getId().equals(host.getId())) {
                throw new AccessDeniedException("Du kannst nur dein eigenes Host-Profil bearbeiten.");
            }
        }
        try {
            host.setName(name);
            host.setDescription(description);
            host.setAcceptedAnimals(animals == null ? new HashSet<>() : new HashSet<>(animals));
            host.setAvailableFrom(availableFrom == null || availableFrom.isEmpty() ? null : LocalDate.parse(availableFrom));
            host.setAvailableUntil(availableUntil == null || availableUntil.isEmpty() ? null : LocalDate.parse(availableUntil));
            host.setPricePerWeek(pricePerWeek);
            service.update(host);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("host", host);
            model.addAttribute("animalTypes", AnimalType.values());
            return "hosts/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        if (!principal.getUser().hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Nur Admins können Gastgeber löschen.");
        }
        service.deleteById(id);
        return "redirect:/hosts";
    }
}

package com.pawsitters.controller;

import com.pawsitters.model.AnimalType;
import com.pawsitters.model.Pet;
import com.pawsitters.model.UserRole;
import com.pawsitters.security.PawsittersUserPrincipal;
import com.pawsitters.service.PetService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * Liste der eigenen Tiere (oder aller fuer Admin).
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
            throw new AccessDeniedException("Nur Tierhalter koennen Tiere registrieren.");
        }
        model.addAttribute("pet", new Pet());
        model.addAttribute("animalTypes", AnimalType.values());
        return "pets/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @ModelAttribute Pet pet,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Model model) {
        var user = principal.getUser();
        if (!user.hasRole(UserRole.OWNER) || user.getOwnerProfile() == null) {
            throw new AccessDeniedException("Nur Tierhalter koennen Tiere registrieren.");
        }
        try {
            petService.registerWithImage(pet, user.getOwnerProfile().getId(), image);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pet", pet);
            model.addAttribute("animalTypes", AnimalType.values());
            return "pets/form";
        }
    }

    /**
     * Streamt das Profilbild eines Pets. Wird vom <img>-Tag in Templates aufgerufen.
     * Cache-Control fuer 1h damit der Browser nicht jedes Mal neu laedt.
     */
    @GetMapping("/{id}/image")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Pet pet = petService.findById(id).orElse(null);
        if (pet == null || !pet.hasImage()) {
            return ResponseEntity.notFound().build();
        }
        MediaType contentType;
        try {
            contentType = MediaType.parseMediaType(pet.getImageContentType());
        } catch (Exception e) {
            contentType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(pet.getImageData());
    }

    /**
     * Bild eines existierenden Pets aktualisieren.
     */
    @PostMapping("/{id}/image")
    public String uploadImage(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                              @PathVariable Long id,
                              @RequestParam("image") MultipartFile image) {
        ensureOwnerOf(principal, id);
        try {
            petService.updateImage(id, image);
        } catch (IOException | IllegalArgumentException e) {
            // Bei Fehler einfach redirect mit Fehler-Flag - reicht fuer Demo
            return "redirect:/dashboard?imageError";
        }
        return "redirect:/dashboard";
    }

    /**
     * Bild eines Pets entfernen.
     */
    @PostMapping("/{id}/image/delete")
    public String deleteImage(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                              @PathVariable Long id) {
        ensureOwnerOf(principal, id);
        petService.removeImage(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal PawsittersUserPrincipal principal,
                         @PathVariable Long id) {
        ensureOwnerOf(principal, id);
        petService.deleteById(id);
        return "redirect:/dashboard";
    }

    /**
     * Prueft, ob der eingeloggte User der Besitzer des Pets ist (oder Admin).
     */
    private void ensureOwnerOf(PawsittersUserPrincipal principal, Long petId) {
        var user = principal.getUser();
        var pet = petService.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Tier nicht gefunden"));
        if (!user.hasRole(UserRole.ADMIN)
                && (user.getOwnerProfile() == null
                    || !user.getOwnerProfile().getId().equals(pet.getOwner().getId()))) {
            throw new AccessDeniedException("Du kannst nur eigene Tiere aendern.");
        }
    }
}

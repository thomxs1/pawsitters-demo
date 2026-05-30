package com.pawsitters.service;

import com.pawsitters.model.*;
import com.pawsitters.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Kapselt die Geschäftslogik rund um Nutzer-Registrierung und -Lookup.
 * Verantwortlich für:
 *   - Anlegen neuer User inkl. zugehöriger Profile (PetOwner, Host)
 *   - Prüfung auf Eindeutigkeit von Username und E-Mail
 *   - Hashen des Passworts mit BCrypt
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registriert einen neuen User und legt automatisch das passende Profil
     * (PetOwner und/oder Host) an, je nach gewählten Rollen.
     *
     * Mindestens eine Rolle (OWNER oder HOST) muss gewählt sein.
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Benutzername bereits vergeben: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-Mail bereits vergeben: " + request.email());
        }
        if (!request.becomeOwner() && !request.becomeHost()) {
            throw new IllegalArgumentException("Mindestens eine Rolle (Tierhalter oder Gastgeber) muss gewählt werden.");
        }

        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.displayName()
        );

        if (request.becomeOwner()) {
            user.addRole(UserRole.OWNER);
            PetOwner owner = new PetOwner(request.displayName(), request.email(), null);
            user.setOwnerProfile(owner);
        }

        if (request.becomeHost()) {
            user.addRole(UserRole.HOST);
            // Host-Profil als Skeleton anlegen, Details füllt der User später im Profil
            Host host = new Host(
                request.displayName(),
                request.email(),
                null,
                new HashSet<>(),
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                BigDecimal.ZERO
            );
            user.setHostProfile(host);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * DTO für die Registrierung.
     */
    public record RegisterRequest(
        String username,
        String email,
        String password,
        String displayName,
        boolean becomeOwner,
        boolean becomeHost
    ) {}
}

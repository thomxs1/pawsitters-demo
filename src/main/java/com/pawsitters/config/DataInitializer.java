package com.pawsitters.config;

import com.pawsitters.model.*;
import com.pawsitters.repository.*;
import com.pawsitters.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Legt beim Start ein paar Demo-Daten an, damit die Anwendung sofort
 * vorzeigbar ist und die Live-Demo nicht von Null starten muss.
 *
 * Nicht aktiv im "test"-Profil, damit Tests nicht ueberraschend mit Daten starten.
 *
 * Passwoerter werden aus den Environment-Variablen DEMO_PASSWORD und
 * ADMIN_PASSWORD gelesen (siehe application.properties). In Produktion
 * BITTE starke Werte setzen!
 */
@Configuration
@Profile("!test")
public class DataInitializer {

    @Value("${pawsitters.demo.password}")
    private String demoPassword;

    @Value("${pawsitters.demo.admin-password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedDemoData(UserRepository userRepository,
                                          PetOwnerRepository ownerRepository,
                                          HostRepository hostRepository,
                                          PetRepository petRepository,
                                          UserService userService,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // schon Daten vorhanden
            }

            // === Demo-User 1: Tom (Tierhalter mit Hund) ===
            User tom = userService.register(new UserService.RegisterRequest(
                "tom",
                "tom@example.com",
                demoPassword,
                "Tom Müller",
                true,   // becomeOwner
                false   // becomeHost
            ));
            // Hund "Bello" hinzufügen
            PetOwner tomOwner = tom.getOwnerProfile();
            Pet bello = new Pet("Bello", AnimalType.DOG, 5,
                "Freundlicher Labrador, verträgt sich mit allen Hunden. "
                + "Braucht morgens und abends einen Spaziergang.", tomOwner);
            petRepository.save(bello);

            // === Demo-User 2: Anna (Gastgeberin für Hunde) ===
            User anna = userService.register(new UserService.RegisterRequest(
                "anna",
                "anna@example.com",
                demoPassword,
                "Anna Schmidt",
                false,  // becomeOwner
                true    // becomeHost
            ));
            Host annaHost = anna.getHostProfile();
            annaHost.setDescription("Tierfreundliche Gastgeberin mit eigenem Garten. "
                + "Erfahrung mit Hunden seit über 10 Jahren.");
            annaHost.setAcceptedAnimals(EnumSet.of(AnimalType.DOG));
            annaHost.setAvailableFrom(LocalDate.now().minusDays(7));
            annaHost.setAvailableUntil(LocalDate.now().plusMonths(2));
            annaHost.setPricePerWeek(new BigDecimal("100.00"));
            hostRepository.save(annaHost);

            // === Demo-User 3: Ben (Gastgeber für Hunde und Katzen) ===
            User ben = userService.register(new UserService.RegisterRequest(
                "ben",
                "ben@example.com",
                demoPassword,
                "Ben Weber",
                false,
                true
            ));
            Host benHost = ben.getHostProfile();
            benHost.setDescription("Ruhige Wohnung in der Innenstadt, perfekt für Katzen "
                + "und kleinere Hunde. Tierarzt im selben Haus.");
            benHost.setAcceptedAnimals(EnumSet.of(AnimalType.DOG, AnimalType.CAT));
            benHost.setAvailableFrom(LocalDate.now().minusDays(3));
            benHost.setAvailableUntil(LocalDate.now().plusMonths(3));
            benHost.setPricePerWeek(new BigDecimal("130.00"));
            hostRepository.save(benHost);

            // === Demo-User 4: Clara (sowohl Halter als auch Gastgeberin) ===
            User clara = userService.register(new UserService.RegisterRequest(
                "clara",
                "clara@example.com",
                demoPassword,
                "Clara Becker",
                true,
                true
            ));
            // Claras Katze
            Pet luna = new Pet("Luna", AnimalType.CAT, 3,
                "Sehr verschmuste Hauskatze, frisst nur Trockenfutter.",
                clara.getOwnerProfile());
            petRepository.save(luna);
            // Claras Host-Profil
            Host claraHost = clara.getHostProfile();
            claraHost.setDescription("Katzenliebhaberin, biete Pflege für ruhige Katzen.");
            claraHost.setAcceptedAnimals(EnumSet.of(AnimalType.CAT, AnimalType.RABBIT));
            claraHost.setAvailableFrom(LocalDate.now());
            claraHost.setAvailableUntil(LocalDate.now().plusMonths(6));
            claraHost.setPricePerWeek(new BigDecimal("85.00"));
            hostRepository.save(claraHost);

            // === Admin-User (für Plattform-Verwaltung) ===
            User admin = new User(
                "admin",
                "admin@pawsitters.example",
                passwordEncoder.encode(adminPassword),
                "Plattform Admin"
            );
            admin.addRole(UserRole.ADMIN);
            userRepository.save(admin);

            System.out.println("=== Pawsitters Demo-Daten geladen ===");
            System.out.println("  tom    (Tierhalter mit Hund Bello)");
            System.out.println("  anna   (Gastgeberin fuer Hunde)");
            System.out.println("  ben    (Gastgeber fuer Hunde + Katzen)");
            System.out.println("  clara  (Tierhalterin UND Gastgeberin)");
            System.out.println("  admin  (Plattform-Admin)");
            System.out.println("Passwoerter aus Env-Vars DEMO_PASSWORD / ADMIN_PASSWORD");
            System.out.println("(Default lokal: demo1234 / admin1234)");
            System.out.println("====================================");
        };
    }
}

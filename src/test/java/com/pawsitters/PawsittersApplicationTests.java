package com.pawsitters;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke-Test: Prüft, dass der Spring-Kontext fehlerfrei startet.
 * Im "test"-Profil wird DataInitializer nicht ausgeführt, so dass der
 * Test nicht von Demo-Daten abhängt.
 */
@SpringBootTest
@ActiveProfiles("test")
class PawsittersApplicationTests {

    @Test
    void contextLoads() {
        // Wenn der Spring-Kontext lädt, ist der Test erfolgreich.
    }
}

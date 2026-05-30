package com.pawsitters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Pawsitters - Pet Holiday Platform
 *
 * Einstiegspunkt der Spring-Boot-Anwendung. Startet den eingebetteten
 * Tomcat-Server, initialisiert die H2-Datenbank im Speicher und lädt
 * alle Komponenten (Controller, Services, Repositories).
 */
@SpringBootApplication
public class PawsittersApplication {

    public static void main(String[] args) {
        SpringApplication.run(PawsittersApplication.class, args);
    }
}

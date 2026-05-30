package com.pawsitters.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für Spring Security.
 *
 * Prüft:
 *  - Oeffentliche Routen sind ohne Login erreichbar
 *  - Geschützte Routen leiten auf Login um
 *  - Registrierung legt einen User an, der sich anschliessend einloggen kann
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void publicRoutes_areAccessibleWithoutLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void protectedRoutes_redirectToLoginWhenNotAuthenticated() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mvc.perform(get("/pets"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mvc.perform(get("/requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void registrationAndLogin_workEndToEnd() throws Exception {
        // 1. Registrierung
        mvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("password", "password123")
                        .param("displayName", "New User")
                        .param("becomeOwner", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // 2. Login mit den frischen Credentials
        mvc.perform(post("/login")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void registration_withDuplicateUsername_showsError() throws Exception {
        // Erste Registrierung
        mvc.perform(post("/register")
                        .param("username", "duplicate")
                        .param("email", "first@example.com")
                        .param("password", "password123")
                        .param("displayName", "First")
                        .param("becomeOwner", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Zweite Registrierung mit gleichem Username sollte fehlschlagen
        mvc.perform(post("/register")
                        .param("username", "duplicate")
                        .param("email", "second@example.com")
                        .param("password", "password123")
                        .param("displayName", "Second")
                        .param("becomeOwner", "true")
                        .with(csrf()))
                .andExpect(status().isOk()) // bleibt auf Register-Page
                .andExpect(view().name("auth/register"));
    }

    @Test
    void registration_withoutAnyRole_showsError() throws Exception {
        mvc.perform(post("/register")
                        .param("username", "noroleuser")
                        .param("email", "norole@example.com")
                        .param("password", "password123")
                        .param("displayName", "No Role")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }
}

package com.pawsitters.service;

import com.pawsitters.model.User;
import com.pawsitters.model.UserRole;
import com.pawsitters.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für UserService.
 * Verifiziert die Registrierungs-Logik einschliesslich Eindeutigkeit,
 * Rollen-Validierung und automatischer Profil-Anlage.
 */
class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void register_asOwnerOnly_createsOwnerProfile() {
        var request = new UserService.RegisterRequest(
                "tom", "tom@example.com", "secret123", "Tom Müller", true, false);

        User saved = service.register(request);

        assertEquals("tom", saved.getUsername());
        assertEquals("HASHED", saved.getPasswordHash());
        assertTrue(saved.hasRole(UserRole.OWNER));
        assertFalse(saved.hasRole(UserRole.HOST));
        assertNotNull(saved.getOwnerProfile());
        assertNull(saved.getHostProfile());
        assertEquals("Tom Müller", saved.getOwnerProfile().getName());
    }

    @Test
    void register_asHostOnly_createsHostProfile() {
        var request = new UserService.RegisterRequest(
                "anna", "anna@example.com", "secret123", "Anna Schmidt", false, true);

        User saved = service.register(request);

        assertTrue(saved.hasRole(UserRole.HOST));
        assertFalse(saved.hasRole(UserRole.OWNER));
        assertNotNull(saved.getHostProfile());
        assertNull(saved.getOwnerProfile());
        assertEquals("Anna Schmidt", saved.getHostProfile().getName());
    }

    @Test
    void register_asBoth_createsBothProfiles() {
        var request = new UserService.RegisterRequest(
                "clara", "clara@example.com", "secret123", "Clara Becker", true, true);

        User saved = service.register(request);

        assertTrue(saved.hasRole(UserRole.OWNER));
        assertTrue(saved.hasRole(UserRole.HOST));
        assertNotNull(saved.getOwnerProfile());
        assertNotNull(saved.getHostProfile());
    }

    @Test
    void register_withoutAnyRole_throwsException() {
        var request = new UserService.RegisterRequest(
                "x", "x@example.com", "secret123", "X", false, false);

        assertThrows(IllegalArgumentException.class, () -> service.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateUsername_throwsException() {
        when(userRepository.existsByUsername("tom")).thenReturn(true);
        var request = new UserService.RegisterRequest(
                "tom", "tom@example.com", "secret123", "Tom", true, false);

        assertThrows(IllegalArgumentException.class, () -> service.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsException() {
        when(userRepository.existsByEmail("tom@example.com")).thenReturn(true);
        var request = new UserService.RegisterRequest(
                "tom", "tom@example.com", "secret123", "Tom", true, false);

        assertThrows(IllegalArgumentException.class, () -> service.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_hashesPasswordWithEncoder() {
        var request = new UserService.RegisterRequest(
                "tom", "tom@example.com", "PlainPassword!", "Tom", true, false);

        User saved = service.register(request);

        verify(passwordEncoder).encode("PlainPassword!");
        assertEquals("HASHED", saved.getPasswordHash());
        assertNotEquals("PlainPassword!", saved.getPasswordHash());
    }
}

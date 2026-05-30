package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * Authentifizierungs-Entität.
 * Trennt bewusst die "Login-Daten" (Username, Passwort, Rollen) von den
 * fachlichen Profilen (PetOwner, Host). Ein User kann mit einem PetOwner-
 * und/oder einem Host-Profil verknüpft sein.
 *
 * Passwörter werden NUR als BCrypt-Hash gespeichert, nie als Klartext.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Benutzername darf nicht leer sein")
    @Size(min = 3, max = 30, message = "Benutzername muss 3-30 Zeichen lang sein")
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt-Hash des Passworts. NIE als Klartext speichern!
     */
    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false)
    private String displayName;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = EnumSet.noneOf(UserRole.class);

    /**
     * Verknüpfung zum PetOwner-Profil (falls Rolle OWNER vorhanden).
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_owner_id")
    private PetOwner ownerProfile;

    /**
     * Verknüpfung zum Host-Profil (falls Rolle HOST vorhanden).
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id")
    private Host hostProfile;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User() {}

    public User(String username, String email, String passwordHash, String displayName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }

    public void addRole(UserRole role) {
        if (roles == null) {
            roles = EnumSet.noneOf(UserRole.class);
        }
        roles.add(role);
    }

    // Getter und Setter

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }

    public PetOwner getOwnerProfile() { return ownerProfile; }
    public void setOwnerProfile(PetOwner ownerProfile) { this.ownerProfile = ownerProfile; }

    public Host getHostProfile() { return hostProfile; }
    public void setHostProfile(Host hostProfile) { this.hostProfile = hostProfile; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

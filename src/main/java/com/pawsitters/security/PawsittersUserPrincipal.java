package com.pawsitters.security;

import com.pawsitters.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom Principal, das die komplette User-Entity bei sich trägt.
 * So können Controller direkt @AuthenticationPrincipal PawsittersUserPrincipal nutzen
 * und haben Zugriff auf User-ID, Display Name, Profile etc.
 */
public class PawsittersUserPrincipal implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public PawsittersUserPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

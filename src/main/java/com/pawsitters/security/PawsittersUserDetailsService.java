package com.pawsitters.security;

import com.pawsitters.model.User;
import com.pawsitters.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Brücke zwischen unserer User-Entity und Spring Security.
 * Spring ruft loadUserByUsername() bei jedem Login auf.
 */
@Service
public class PawsittersUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PawsittersUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Unbekannter Benutzer: " + username));

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toList());

        return new PawsittersUserPrincipal(user, authorities);
    }
}

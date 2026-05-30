package com.pawsitters.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Zentrale Security-Konfiguration.
 *
 * - Oeffentliche Routen: /, /login, /register, statische Ressourcen
 * - Alles andere: nur für eingeloggte Nutzer
 * - Login-Form: /login (custom Page)
 * - Logout: /logout, Redirect auf /
 * - Passwörter: BCrypt
 *
 * CSRF ist aktiv (Spring Default), Thymeleaf-Forms binden den Token
 * automatisch über <form action="..." method="post">.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Oeffentliche Seiten
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/"),
                    AntPathRequestMatcher.antMatcher("/login"),
                    AntPathRequestMatcher.antMatcher("/register"),
                    AntPathRequestMatcher.antMatcher("/css/**"),
                    AntPathRequestMatcher.antMatcher("/js/**"),
                    AntPathRequestMatcher.antMatcher("/images/**"),
                    AntPathRequestMatcher.antMatcher("/favicon.ico")
                ).permitAll()
                // H2-Konsole nur für Admins (im Dev könnte man das auch öffnen)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).hasRole("ADMIN")
                // Alles andere braucht Login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // CSRF aktiv lassen, aber für H2-Konsole abschalten (sonst geht die nicht)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
            )
            // H2-Konsole braucht Frames - nur für dieselbe Origin erlaubt
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}

package com.example.onboarding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Modern lambda syntax to disable CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll() // Replaces antMatchers
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // Standard Basic Auth configuration
            
        // Keeps the H2 console working inside frames
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); 
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123") // Keeps your exercise-only plain-text password style
            .roles("ADMIN")
            .build();
            
        return new InMemoryUserDetailsManager(admin);
    }
}

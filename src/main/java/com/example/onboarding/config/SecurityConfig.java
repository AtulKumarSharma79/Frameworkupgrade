package com.example.onboarding.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic security for the API.
 * Exercise-only credentials: admin / admin123.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. Disable CSRF, as in the original application
        http.csrf(csrf -> csrf.disable());

        // 2. Configure authorization rules
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(PathRequest.toH2Console()).permitAll()
            .anyRequest().authenticated()
        );

        // 3. Enable HTTP Basic authentication
        http.httpBasic(Customizer.withDefaults());

        // 4. Allow H2 console pages to use same-origin frames
        http.headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin())
        );

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {

        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
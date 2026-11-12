package com.example.onboarding.config;

import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;

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
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers( "/api/**").permitAll()
                .anyRequest().authenticated()
            )

            .httpBasic(Customizer.withDefaults());
            //.headers(headers -> headers
                //.frameOptions(frameOptions -> frameOptions.sameOrigin())
            //);
        
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() throws Exception {
        UserDetails admin = User.withUsername("admin")
            .username("admin")
            .password("admin123")
            .roles("ADMIN") 
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}

package de.kekiiis.aufgabenmanagement.security;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

import de.kekiiis.aufgabenmanagement.view.LoginView;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true) // hiermit aktiviert Spring Security auch die Prüfung von @RolesAllowed auf Service-Methoden.
public class SecurityConfiguration {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http.with(
            VaadinSecurityConfigurer.vaadin(),
            configurer -> configurer.loginView(LoginView.class)
        ).build();
    }
}

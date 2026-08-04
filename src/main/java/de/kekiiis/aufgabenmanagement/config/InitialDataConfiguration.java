package de.kekiiis.aufgabenmanagement.config;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Role;
import de.kekiiis.aufgabenmanagement.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialDataConfiguration {
    
    @Bean
    CommandLineRunner createInitialAdministrator (
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (appUserRepository.existsByUsername("admin")) {
                return;
            }

            String initialPassword = System.getenv("INITIAL_ADMIN_PASSWORD");

            if (initialPassword == null || initialPassword.isBlank()) {
                throw new IllegalStateException(
                    "Die Umgebungsvariable INITIAL_ADMIN_PASSWORD fehlt."
                );
            }

            AppUser admin = new AppUser(
                "admin",
                "admin@example.de",
                passwordEncoder.encode(initialPassword),
                "System",
                "Administrator"
            );

            admin.addRole(Role.ADMIN);
            admin.setEnabled(true);

            appUserRepository.save(admin);
        };
    }
}

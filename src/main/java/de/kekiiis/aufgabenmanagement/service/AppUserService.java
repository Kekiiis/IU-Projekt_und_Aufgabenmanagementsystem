package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Role;
import de.kekiiis.aufgabenmanagement.repository.AppUserRepository;

import jakarta.annotation.security.RolesAllowed;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {
    
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @RolesAllowed("ADMIN")
    @Transactional  // sorgt dafür, dass die Datenbankoperation innerhalb der Transaktion ausgeführt wird. Sollte das Speichern fehlschlagen, wird die Transaktion zurückgerollt.
    public AppUser createUser(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        Role role
    ) {
        if (username == null || username.isBlank()
            || email == null || email.isBlank()
            || password == null || password.isBlank()
            || firstName == null || firstName.isBlank()
            || lastName == null || lastName.isBlank()
            || role == null) {
            
            throw new IllegalArgumentException(
                "Bitte fülle alle Pflichtfelder aus."
            );
        }

        username = username.trim();
        email = email.trim();
        firstName = firstName.trim();
        lastName = lastName.trim();

        // Feldlängen prüfen
        if (username.length() > 100 || firstName.length() > 100 || lastName.length() > 100) {
            throw new IllegalArgumentException(
                "Benutzername, Vorname, und Nachname dürfen höchstens 100 Zeichen lang sein."
            );
        }

        if (email.length() > 255) {
            throw new IllegalArgumentException(
                "Die E-Mail-Adresse darf höchstens 255 Zeichen lang sein."
            );
        }

        //E-Mail-Format überprüfen
        if(!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                "Bitte gib eine gültige E-Mail-Adresse ein."
            );
        }

        //Passwortlänge prüfen
        if(password.length() < 12) {
            throw new IllegalArgumentException(
                "Das Passwort muss mindestens 12 Zeichen lang sein."
            );
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                "Der Benutzername ist bereits vergeben."
            );
        }

        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Diese E-Mail-Adresse ist bereits vergeben."
            );
        }

        String passwordHash = passwordEncoder.encode(password);

        AppUser user = new AppUser(
            username, 
            email, 
            passwordHash, 
            firstName, 
            lastName);

        user.addRole(role);

        return appUserRepository.save(user);
    }
}

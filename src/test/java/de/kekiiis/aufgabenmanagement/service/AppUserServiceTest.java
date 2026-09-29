package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.AufgabenmanagementApplication;
import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Role;
import de.kekiiis.aufgabenmanagement.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AufgabenmanagementApplication.class)
@ActiveProfiles("test")
@Transactional
class AppUserServiceTest {
    
    @Autowired
    private AppUserService appUserService;

    @Autowired 
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminKannBenutzerAnlegen() {

        AppUser user = appUserService.createUser(
            "TestMitarbeiter01", 
            "testtitarbeiter01@example.de", 
            "SicheresPasswort01", 
            "Test", 
            "Mitarbeiter", 
            Role.EMPLOYEE);

        assertNotNull(user.getId());
        assertEquals("TestMitarbeiter01", user.getUsername());
        assertTrue(user.getRoles().contains(Role.EMPLOYEE));

        assertTrue(appUserRepository.existsByUsername("TestMitarbeiter01"));
    }

    @Test 
    @WithMockUser(roles = "PROJECT_MANAGER") 
    void projektleitungDarfKeineBenutzerAnlegen() {
        assertThrows(
            AccessDeniedException.class,
            () -> appUserService.createUser(
                "UnerlaubteProjektleitung", 
                "unerlaubteprojektleitung@example.de", 
                "SicheresPasswort02", 
                "Test", 
                "Projektleitung", 
                Role.PROJECT_MANAGER)
        );

        assertFalse(
          appUserRepository.existsByUsername("UnerlaubteProjektleitung")  
        );
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void mitarbeiterDarfKeineBenutzerAnlegen() {

        assertThrows(
            AccessDeniedException.class, 
            () -> appUserService.createUser(
                "UnerlaubterBenutzer", 
                "unerlaubt@example.de", 
                "SicheresPasswort03", 
                "Test", 
                "Erlaubt", 
                Role.EMPLOYEE)
        );

        assertFalse(appUserRepository.existsByUsername("UnerlaubterBenutzer"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void zuKurzesPasswortWirdAbgewiesen() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> appUserService.createUser(
                "KurzesPasswort", 
                "kurzespasswort@example.de", 
                "Kurz123", 
                "Test", 
                "Benutzer", 
                Role.EMPLOYEE)
        );

        assertEquals("Das Passwort muss mindestens 12 Zeichen lang sein.", exception.getMessage());

        assertFalse(appUserRepository.existsByUsername("KurzesPasswort"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void passwortWirdGehashedGespeichert() {

        String klartextPasswort = "SicheresPasswort04";

        AppUser user = appUserService.createUser(
            "HashTest", 
            "hashtest@example.de", 
            klartextPasswort, 
            "Hash", 
            "Test", 
            Role.EMPLOYEE);

        assertNotEquals(klartextPasswort, user.getPasswordHash());

        assertTrue(passwordEncoder.matches(klartextPasswort,user.getPasswordHash()));
    }
}   

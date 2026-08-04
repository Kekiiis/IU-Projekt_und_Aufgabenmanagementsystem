package de.kekiiis.aufgabenmanagement.security;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) // beim Login wird der Benutzer gesucht.
            throws UsernameNotFoundException {
        
        AppUser appUser = appUserRepository.findByUsername(username) 
                .orElseThrow(() -> new UsernameNotFoundException(   // wenn der Benutzer nicht in der Datenbank gefunden wird, wird der Fehler geworfen.
                    "Benutzer wurde nicht gefunden."
                ));
            
        String[] roles = appUser.getRoles()
                .stream()
                .map(Enum::name)
                .toArray(String[]::new);

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(roles)
                .disabled(!appUser.isEnabled())
                .build();
        }
}

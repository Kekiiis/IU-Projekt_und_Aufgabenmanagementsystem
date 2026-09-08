package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

import org.springframework.security.core.userdetails.UserDetails;

@PermitAll
public class MainLayout extends AppLayout {
    
    private final transient AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        createHeader();
    }

    private void createHeader() {
        H2 title = new H2("Projekt- und Aufgabenmanagementsystem");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);

        authenticationContext
            .getAuthenticatedUser(UserDetails.class)
            .ifPresent(user -> {
                Span username =
                    new Span("Angemeldet als: " + user.getUsername());

                Button logoutButton = new Button(
                    "Abmelden",
                    event -> authenticationContext.logout()
                );

                header.add(
                    title,
                    username,
                    logoutButton
                );

                header.expand(title);
            });

        addToNavbar(header);
    }
}

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
        H2 space = new H2(" ");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);

        authenticationContext
            .getAuthenticatedUser(UserDetails.class)
            .ifPresent(user -> {
                Span username =
                    new Span(user.getUsername());

                Button logoutButton = new Button(
                    "Abmelden",
                    event -> authenticationContext.logout()
                );

                Button homeButton = new Button(
                    "Homepage",
                    click -> getUI().ifPresent(ui -> ui.navigate(""))
                );

                header.add(
                    title,
                    homeButton,
                    space,
                    username,
                    logoutButton
                );

                header.expand(space);
            });

        addToNavbar(header);
    }
}

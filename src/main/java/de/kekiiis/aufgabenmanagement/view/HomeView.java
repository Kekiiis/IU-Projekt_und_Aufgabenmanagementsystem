package de.kekiiis.aufgabenmanagement.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Startseite")
@PermitAll // jeder angemeldete Benutzer darf die Startseite sehen.
public class HomeView extends VerticalLayout{
    
    public HomeView() {

        Button projects = new Button(
            "Projekte",
            click -> getUI().ifPresent(ui -> ui.navigate("projects"))
        );

        Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();

        boolean canManageUsers = authentication.getAuthorities().stream()
            .anyMatch(authority -> 
                authority.getAuthority().equals("ROLE_ADMIN"));

        Button userManagementButton = new Button(
            "Benutzerverwaltung",
            click -> getUI().ifPresent(ui -> ui.navigate("users"))
        );

        userManagementButton.setVisible(canManageUsers);

        add(
            new Paragraph("Du bist erfolgreich angemeldet."),
            projects,
            userManagementButton
        );
    }
}

package de.kekiiis.aufgabenmanagement.view;

import org.springframework.security.core.userdetails.User;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.service.AppUserService;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Benutzerverwaltung")
@RolesAllowed("ADMIN")
public class UserListView extends VerticalLayout {
    
    private final AppUserService appUserService;

    private final Grid<AppUser> userGrid = new Grid<>(AppUser.class, false);

    public UserListView(AppUserService appUserService) {
        this.appUserService = appUserService;

        configureGrid();

        Button newUserButton = new Button("Neuer Benutzer", event -> {
            UserForm userForm = new UserForm(appUserService, this::refreshGrid);
            userForm.open();
        });

        add(
          new H2("Benutzerverwaltung"),
          newUserButton,
          userGrid  
        );

        refreshGrid();
    }

    private void configureGrid() {

        userGrid.addColumn(AppUser::getUsername)
            .setHeader("Benutzername");

        userGrid.addColumn(AppUser::getFirstName)
            .setHeader("Vorname");

        userGrid.addColumn(AppUser::getLastName)
            .setHeader("Nachname");

        userGrid.addColumn(AppUser::getEmail)
            .setHeader("E-Mail-Adresse");

        userGrid.addColumn(user -> 
            user.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "))
        ).setHeader("Rollen");

        userGrid.addColumn(user ->
            user.isEnabled() ? "Aktiv" : "Deaktiviert"
        ).setHeader("Status");
    }

    private void refreshGrid() {
        userGrid.setItems(appUserService.findAll());
    }
}

package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
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
            click -> getUI().ifPresent(ui -> 
                ui.navigate("projects")
            )
        );
        add(
            new Paragraph("Du bist erfolgreich angemeldet."),
            projects
        );
    }
}

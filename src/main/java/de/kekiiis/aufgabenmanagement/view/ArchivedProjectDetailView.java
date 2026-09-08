package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.service.ProjectService;

import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

@Route("archived-projects")
@PageTitle("Archiviertes Projekt")
@PermitAll
public class ArchivedProjectDetailView extends VerticalLayout implements HasUrlParameter<Long> {
    
    private final ProjectService projectService;

    public ArchivedProjectDetailView(
        ProjectService projectService
    ) {
        this.projectService = projectService;
    }

    @Override
    public void setParameter(
        BeforeEvent event,
        Long projectId
    ) {

        removeAll();

        Project project = 
                projectService.findByIdWithMembers(projectId);

        String members = project.getMembers()
            .stream()
            .map(this::getUserDisplayName)
            .collect(Collectors.joining(", "));

        if(members.isBlank()) {
            members = "Keine Mitarbeitenden zugeordnet.";
        }

        Button backButton = new Button(
            "Zurück zu archivierten Projekten",
            click -> getUI().ifPresent(ui -> 
                ui.navigate("archived-projects")
            )
        );

        add(
            backButton,
            new H2(project.getName()),
            new Paragraph(
                "Beschreibung: "
                    + project.getDescription()        
            ),
            new Paragraph(
                "Projektleitung: "
                    + getUserDisplayName(
                        project.getProjectLeader()
                    )
            ),
            new Paragraph(
                "Mitarbeitende: " + members
            ),
            new Paragraph("Status: Archiviert")
        );
    }

    private String getUserDisplayName(AppUser user) {
        return user.getFirstName() 
            + " " 
            + user.getLastName() 
            + " (" + user.getUsername() + ")";
    }
}

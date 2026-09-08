package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.service.ProjectService;

import jakarta.annotation.security.PermitAll;

@Route("archived-projects")
@PageTitle("Archivierte Projekte")
@PermitAll
public class ArchivedProjectListView extends VerticalLayout {
    
    private final ProjectService projectService;

    private final Grid<Project> projectGrid =
        new Grid<>(Project.class, false);

    public ArchivedProjectListView(ProjectService projectService) {
        this.projectService = projectService;

        configureGrid();

        Button backButton = new Button(
            "Zurück zu Projekten",
            event -> getUI().ifPresent(ui ->
                ui.navigate("projects")
            )
        );

        add(
          backButton,
          projectGrid  
        );

        refreshGrid();
    }

    private void configureGrid() {

        projectGrid.addColumn(Project::getName)
            .setHeader("Name");

        projectGrid.addColumn(project -> 
                project.getProjectLeader().getUsername())
            .setHeader("Projektleitung");

        projectGrid.addColumn(Project::getDescription)
            .setHeader("Beschreibung");

        projectGrid.addComponentColumn(project -> {
            Button restoreButton = new Button("Wiederherstellen");

            restoreButton.addClickListener(event -> {
                projectService.restore(project);
                refreshGrid();
            });

            return restoreButton;
        }).setHeader("Aktionen");

        projectGrid.asSingleSelect().addValueChangeListener(event -> {
            Project selectedProject = event.getValue();

            if (selectedProject != null) {
                getUI().ifPresent(ui -> 
                    ui.navigate(
                        "archived-projects/" + selectedProject.getId()
                    )
                );
            }
        });
    }

    private void refreshGrid() {
        projectGrid.setItems(projectService.findArchivedProjects());
    }
}

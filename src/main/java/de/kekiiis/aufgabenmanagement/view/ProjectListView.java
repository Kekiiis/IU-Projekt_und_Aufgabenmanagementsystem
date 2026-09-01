package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.service.AppUserService;
import de.kekiiis.aufgabenmanagement.service.ProjectService;

import jakarta.annotation.security.PermitAll;

@Route("projects")
@PageTitle("Projekte")
@PermitAll
public class ProjectListView extends VerticalLayout {
    
    private final AppUserService appUserService;
    private final ProjectService projectService;
    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);

    public ProjectListView(ProjectService projectService, AppUserService appUserService) {
        this.projectService = projectService;
        this.appUserService = appUserService;

        configureGrid();

        ProjectForm projectForm = new ProjectForm(appUserService.findAll());

        projectForm.setVisible(false);

        projectForm.addSaveListener(event -> {
            projectService.save(event.getProject());
            projectForm.clearForm();
            refreshGrid();
        });

        projectForm.addCancelListener(event -> {
            projectForm.clearForm();
        });

        Button newProjectButton = new Button(
            "Neues Projekt",
            event -> {
                Project project = new Project(
                    "", 
                    "", 
                    null
                );

                projectForm.setProject(project);
                projectForm.setVisible(true);
            }
        );
        

        add(newProjectButton, projectForm, projectGrid);

        refreshGrid();
    }

    public void configureGrid() {
        projectGrid.addColumn(Project::getName)
            .setHeader("Name");

        projectGrid.addColumn(project -> 
                project.getProjectLeader().getUsername())
            .setHeader("Projektleitung");

        projectGrid.addColumn(project -> 
                project.isArchived() ? "Ja" : "Nein")
            .setHeader("Archiviert");
    }

    public void refreshGrid() {
        projectGrid.setItems(projectService.findAll());
    }
}

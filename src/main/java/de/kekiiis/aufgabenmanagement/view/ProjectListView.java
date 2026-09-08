package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
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
    private final ProjectForm projectForm;
    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);

    public ProjectListView(ProjectService projectService, AppUserService appUserService) {
        this.projectService = projectService;
        this.appUserService = appUserService;

        configureGrid();

        this.projectForm = new ProjectForm(appUserService.findAll());

        projectForm.setVisible(false);

        projectForm.addSaveListener(event -> {
            projectService.save(event.getProject());
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
            refreshGrid();
        });

        projectForm.addCancelListener(event -> {
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
        });

        projectGrid.asSingleSelect().addValueChangeListener(event -> {
            Project selectedProject = event.getValue();

            if (selectedProject != null) {
                projectForm.setProject(selectedProject);
                projectForm.setVisible(true);
            }
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

        Button archivedProjectsButton = new Button(
            "Archivierte Projekte",
            event -> getUI().ifPresent(ui -> 
                ui.navigate("archived-projects")
            )
        );
        

        add(
            newProjectButton, 
            archivedProjectsButton, 
            projectForm, 
            projectGrid
        );

        refreshGrid();
    }

    public void configureGrid() {
        projectGrid.addColumn(Project::getName)
            .setHeader("Name");

        projectGrid.addColumn(project -> 
                project.getProjectLeader().getUsername())
            .setHeader("Projektleitung");

        projectGrid.addComponentColumn(project -> {
            Button archiveButton = new Button("Archivieren");

            archiveButton.setEnabled(!project.isArchived());

            archiveButton.addClickListener(event -> {
                projectService.archive(project);

                projectGrid.asSingleSelect().clear();
                projectForm.clearForm();
                
                refreshGrid();

                Notification.show(
                    "Projekt \"" + project.getName() + "\" wurde archiviert."
                );
            });

            return archiveButton;
        }).setHeader("Aktionen");
    }

    private void refreshGrid() {
        projectGrid.setItems(projectService.findActiveProjects());
    }
}

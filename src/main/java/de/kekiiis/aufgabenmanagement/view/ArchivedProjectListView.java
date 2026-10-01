package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.service.AppUserService;
import de.kekiiis.aufgabenmanagement.service.ProjectService;

import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "archived-projects", layout = MainLayout.class)
@PageTitle("Archivierte Projekte")
@PermitAll
public class ArchivedProjectListView extends VerticalLayout {
    
    private final ProjectService projectService;
    private final AppUserService appUserService;

    private final Grid<Project> projectGrid =
        new Grid<>(Project.class, false);

    public ArchivedProjectListView(ProjectService projectService, AppUserService appUserService) {
        this.projectService = projectService;
        this.appUserService = appUserService;

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
                openProjectDetails(selectedProject);
            }
        });
    }

    private void refreshGrid() {
        projectGrid.setItems(projectService.findArchivedProjects());
    }

    private void openProjectDetails(Project selectedProject) {

        Project project = projectService.findByIdWithMembers(selectedProject.getId());

        List<AppUser> users = appUserService.findAll();

        ProjectForm form = new ProjectForm(users);

        form.setProject(project);

        // Archivierte Projekte nur anzeigen
        form.setEditMode(false);
        form.setEditingAllowed(false);

        Dialog dialog = new Dialog(form);
        dialog.setHeaderTitle("Projektdetails");
        dialog.setWidth("500px");

        form.addCancelListener(e -> {
            dialog.close();
            projectGrid.deselectAll();
        });

        dialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                projectGrid.deselectAll();
            }
        });

        dialog.open();
    }
}

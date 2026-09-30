package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.service.AppUserService;
import de.kekiiis.aufgabenmanagement.service.ProjectService;

import jakarta.annotation.security.PermitAll;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projekte")
@PermitAll
public class ProjectListView extends VerticalLayout {
    
    private final AppUserService appUserService;
    private final ProjectService projectService;
    private final ProjectForm projectForm;
    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);
    private final Dialog projectDialog = new Dialog();

    public ProjectListView(ProjectService projectService, AppUserService appUserService) {
        this.projectService = projectService;
        this.appUserService = appUserService;

        configureGrid();

        this.projectForm = new ProjectForm(appUserService.findAll());

        projectDialog.setWidth("500px");
        projectDialog.setMaxWidth("95vw");

        projectDialog.add(projectForm);

        projectForm.addSaveListener(event -> {
            projectService.save(event.getProject());
            projectDialog.close();
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
            refreshGrid();
        });

        projectForm.addCancelListener(event -> {
            projectDialog.close();
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
        });

        projectGrid.asSingleSelect().addValueChangeListener(event -> {
            Project selectedProject = event.getValue();

            if (selectedProject != null) {
                Project projectWithMembers = projectService.findByIdWithMembers(selectedProject.getId());

                projectForm.setProject(projectWithMembers);
                projectForm.setEditMode(false);
                projectDialog.setHeaderTitle("Projektdetails");
                projectDialog.open();
            }
        });

        Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();

        boolean canManageProjects = authentication.getAuthorities().stream()
            .anyMatch(authority -> 
                authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_PROJECT_MANAGER")
            );

        projectForm.setEditingAllowed(canManageProjects);

        Button newProjectButton = new Button(
            "Neues Projekt",
            event -> {
                Project project = new Project("", "", null);

                projectForm.setProject(project);
                projectForm.setEditMode(true);
                projectDialog.setHeaderTitle("Neues Projekt anlegen");
                projectDialog.open();
            }
        );

        newProjectButton.setVisible(canManageProjects);

        Button archivedProjectsButton = new Button(
            "Archivierte Projekte",
            event -> getUI().ifPresent(ui -> 
                ui.navigate("archived-projects")
            )
        );
        
        archivedProjectsButton.setVisible(canManageProjects);

        add(
            newProjectButton, 
            archivedProjectsButton, 
            projectGrid
        );

        refreshGrid();
    }

    public void configureGrid() {

        Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();

        boolean canManageProjects = authentication.getAuthorities().stream()
            .anyMatch(authority -> 
                authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_PROJECT_MANAGER")
            );

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
            archiveButton.setVisible(canManageProjects);
            return archiveButton;
            
        }).setHeader("Aktionen");

        
    }

    private void refreshGrid() {
        projectGrid.setItems(projectService.findActiveProjects());
    }
}

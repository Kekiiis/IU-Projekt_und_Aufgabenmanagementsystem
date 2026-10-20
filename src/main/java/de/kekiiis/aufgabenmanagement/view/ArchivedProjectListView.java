package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.entity.Task;
import de.kekiiis.aufgabenmanagement.entity.TaskStatus;
import de.kekiiis.aufgabenmanagement.service.AppUserService;
import de.kekiiis.aufgabenmanagement.service.ProjectService;
import de.kekiiis.aufgabenmanagement.service.TaskService;

import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "archived-projects", layout = MainLayout.class)
@PageTitle("Archivierte Projekte")
@PermitAll
public class ArchivedProjectListView extends VerticalLayout {
    
    private final ProjectService projectService;
    private final AppUserService appUserService;
    private final TaskService taskService;

    private final Grid<Project> projectGrid =
        new Grid<>(Project.class, false);

    public ArchivedProjectListView(
        ProjectService projectService, 
        AppUserService appUserService,
        TaskService taskService
    ) {
        this.projectService = projectService;
        this.appUserService = appUserService;
        this.taskService = taskService;

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

    // Setzt die Übersicht der archivierten Aufgaben zusammen.
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

    // Aktualisiert die archivierte Projektübersicht.
    private void refreshGrid() {
        projectGrid.setItems(projectService.findArchivedProjects());
    }

    // Öffnet die Projektdetails des archivierten Projektes.
    private void openProjectDetails(Project selectedProject) {

        Project project = projectService.findByIdWithMembers(selectedProject.getId());

        List<AppUser> users = appUserService.findAll();

        ProjectForm form = new ProjectForm(users);

        form.setProject(project);

        // Archivierte Projekte dürfen nicht bearbeitet werden.
        form.setEditMode(false);
        form.setEditingAllowed(false);

        // Aufgaben des archivierten Projektes laden.
        List<Task> tasks = taskService.findByProject(project);

        // Aufgabenübersicht ohne Bearbeitungsmöglichkeiten.
        Grid<Task> taskGrid = new Grid<>(Task.class, false);

        taskGrid.addColumn(Task::getTitle)
            .setHeader("Aufgabe");

        taskGrid.addColumn(task -> getTaskStatusLabel(task.getTaskStatus()))
            .setHeader("Status");

        taskGrid.setItems(tasks);
        taskGrid.setWidthFull();
        taskGrid.setHeight("250px");

        // Projektfortschritt berechnen.
        int totalTasks = tasks.size();

        long completedTasks = tasks.stream()
            .filter(task -> task.getTaskStatus() == TaskStatus.CLOSED)
            .count();
        
        int progressPercentage = totalTasks == 0
        ? 0
        : (int) Math.round(completedTasks * 100.0 / totalTasks);

        H3 progressHeading = new H3("Projektfortschritt");

        ProgressBar progressBar = new ProgressBar(0, 100, progressPercentage);
        progressBar.setWidthFull();

        Span progressText = new Span(
            completedTasks + " von " + totalTasks
            + " Aufgaben erledigt. (" + progressPercentage + "%)"
        );

        // Dialoginhalt zusammensetzen.
        VerticalLayout dialogContent = new VerticalLayout(
            form,
            progressHeading,
            progressBar,
            progressText,
            new H3("Aufgaben"),
            taskGrid
        );

        dialogContent.setPadding(false);
        dialogContent.setWidthFull();

        Dialog dialog = new Dialog(dialogContent);
        dialog.setHeaderTitle("Projektdetails");
        dialog.setWidth("700px");
        dialog.setMaxWidth("95vw");

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

    // Holt die Statuswerte der Aufgaben.
    private String getTaskStatusLabel (TaskStatus status) {
        return switch(status) {
            case OPEN -> "Offen";
            case IN_PROGRESS -> "In Bearbeitung";
            case CLOSED -> "Erledigt";
        };
    }
}

package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.entity.Task;
import de.kekiiis.aufgabenmanagement.entity.TaskStatus;
import de.kekiiis.aufgabenmanagement.service.AppUserService;
import de.kekiiis.aufgabenmanagement.service.ProjectService;
import de.kekiiis.aufgabenmanagement.service.ProjectPermissionService;
import de.kekiiis.aufgabenmanagement.service.TaskService;

import jakarta.annotation.security.PermitAll;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projekte")
@PermitAll
public class ProjectListView extends VerticalLayout {
    
    private final AppUserService appUserService;
    private final ProjectService projectService;
    private final ProjectPermissionService projectPermissionService;
    private final TaskService taskService;

    private final ProjectForm projectForm;
    private final TaskForm taskForm;

    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);
    private final Grid<Task> taskGrid = new Grid<>(Task.class, false);

    private final Dialog projectDialog = new Dialog();
    private final Dialog taskDialog = new Dialog();

    private Project currentProject;
    private static final String NEUE_AUFGABE_TEXT = "Neue Aufgabe";
    private final Button newTaskButton = new Button(NEUE_AUFGABE_TEXT);

    public ProjectListView(
        ProjectService projectService, 
        AppUserService appUserService, 
        TaskService taskService,
        ProjectPermissionService projectPermissionService) {

        this.projectService = projectService;
        this.appUserService = appUserService;
        this.taskService = taskService;
        this.projectPermissionService = projectPermissionService;

        configureGrid();
        configureTaskGrid();

        this.projectForm = new ProjectForm(appUserService.findAll());
        this.taskForm = new TaskForm();

        projectDialog.setWidth("700px");
        projectDialog.setMaxWidth("95vw");

        VerticalLayout dialogContent = new VerticalLayout(
            projectForm,
            new H3("Aufgaben"),
            newTaskButton,
            taskGrid
        );

        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);
        dialogContent.setWidthFull();

        taskDialog.setHeaderTitle(NEUE_AUFGABE_TEXT);
        taskDialog.add(taskForm);
        taskDialog.setWidth("500px");

        // Erstellt eine neue Aufgabe innerhalb eines Projektes.
        newTaskButton.addClickListener(event -> {
            if (currentProject == null) {
                return;
            }

            if (!projectPermissionService.canEditTasks(currentProject)) {
                Notification.show("Du darfst in diesem Projekt keine Aufgaben erstellen.");
                return;
            }

            taskForm.setTask(new Task("", "", currentProject));
            taskForm.setEditMode(true);

            taskDialog.setHeaderTitle(NEUE_AUFGABE_TEXT);
            taskDialog.open();
        });

        // Speichert eine Aufgabe bzw. speichert die Änderungen einer Aufgabe innerhalb eines Projektes.
        taskForm.addSaveListener(event -> {
            Task task = event.getTask();

            if (currentProject == null
                || !projectPermissionService.canEditTasks(currentProject)) {
                
                Notification.show("Du darfst in diesem Projekt keine Aufgaben bearbeiten.");
                return;
            }

            task.setProject(currentProject);

            if(task.getTaskStatus() == null) {
                task.setTaskStatus(TaskStatus.OPEN);
            }

            taskService.save(task);

            taskDialog.close();
            refreshTaskGrid();
        });

        // Bricht die Eingabe ab und verwirft die Änderungen an einer Aufgabe bzw. verwirft die Erstellung einer neuen Aufgabe.
        taskForm.addCancelListener(event -> {
            taskDialog.close();
            taskForm.clearForm();
            taskGrid.asSingleSelect().clear();
        });

        projectDialog.add(dialogContent);

        // Speichert ein Projekt bzw. speichert die Änderungen an einem Projekt.
        projectForm.addSaveListener(event -> {
            projectService.save(event.getProject());
            projectDialog.close();
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
            refreshGrid();
        });

        // Bricht die Eingabe ab und verwirft die Änderungen an einem Projekt bzw. verwirft die Erstellung eines neuen Projektes.
        projectForm.addCancelListener(event -> {
            projectDialog.close();
            projectForm.clearForm();
            projectGrid.asSingleSelect().clear();
        });

        // Zeigt die Details eines Projektes nach dem Anklicken.
        projectGrid.asSingleSelect().addValueChangeListener(event -> {  
            Project selectedProject = event.getValue();

            if (selectedProject != null) {
                Project projectWithMembers = projectService.findByIdWithMembers(selectedProject.getId());

                if (!projectPermissionService.canAccessProject(projectWithMembers)) {
                    projectGrid.asSingleSelect().clear();

                    Notification.show("Du hast keine Berechtigung um dieses Projekt zu öffnen.");
                    return;
                }

                currentProject = projectWithMembers;

                taskForm.setEditingAllowed(projectPermissionService.canEditTasks(currentProject));

                newTaskButton.setVisible(projectPermissionService.canEditTasks(currentProject));

                projectForm.setProject(projectWithMembers);
                projectForm.setEditMode(false);

                taskGrid.setItems(taskService.findByProject(projectWithMembers));

                projectDialog.setHeaderTitle("Projektdetails");

                projectDialog.open();
            }
        });

        Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();

        boolean canManageProjects = authentication.getAuthorities().stream()
            .anyMatch(authority -> 
                authority.getAuthority().equals("ROLE_ADMIN") 
                || authority.getAuthority().equals("ROLE_PROJECT_MANAGER")
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
                authority.getAuthority().equals("ROLE_ADMIN") 
                || authority.getAuthority().equals("ROLE_PROJECT_MANAGER")
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

    public void configureTaskGrid() {

        taskGrid.addColumn(Task::getTitle)
            .setHeader("Aufgabe");

        taskGrid.addComponentColumn(task -> {

            ComboBox<TaskStatus> statusBox = new ComboBox<>();

            statusBox.setItems(TaskStatus.values());                    // stellt die drei Statuswerte zur Auswahl.
            statusBox.setItemLabelGenerator(this::getTaskStatusLabel);  // zeigt die deutschen Bezeichnungen der drei Statuswerte an.
            statusBox.setValue(task.getTaskStatus());                   // wählt automatisch den aktuell gespeicherten Status.
            statusBox.setWidth("180px");

            statusBox.setReadOnly(
                currentProject == null 
                || !projectPermissionService.canEditTasks(currentProject));

            statusBox.addValueChangeListener(event -> {                 // reagiert darauf, wenn der Status geändert wird.
                
                if (!event.isFromClient()) {                            // event.isFromClient() verhindert, dass das anfängliche Setzen des Status bereits einen Speichervorgang auslöst.
                    return;
                }

                if (currentProject == null
                    || !projectPermissionService.canEditTasks(currentProject)) {

                    statusBox.setValue(task.getTaskStatus());

                    Notification.show("Du darfst den Aufgabenstatus nicht ändern.");
                    return;
                }

                TaskStatus newStatus = event.getValue();

                if (newStatus == null) {
                    return;
                }

                task.setTaskStatus(newStatus);
                taskService.save(task);                                 // speichert die Änderung dauerhaft in der Datenbank.

                Notification.show("Aufgabenstatus wurde aktualisiert.");
            });

            return statusBox;

        }).setHeader("Status");

        taskGrid.setWidthFull();

        taskGrid.asSingleSelect().addValueChangeListener(event -> {
            Task selectedTask = event.getValue();

            if (selectedTask != null) {
                taskForm.setTask(selectedTask);
                taskForm.setEditMode(false);

                taskDialog.setHeaderTitle("Aufgabendetails");
                taskDialog.open();
            }
        });
    }

    // übersetzt die Statuswerte auf Deutsch.
    private String getTaskStatusLabel(TaskStatus status) {
        return switch (status) {
            case OPEN -> "Offen";
            case IN_PROGRESS -> "in Bearbeitung";
            case CLOSED -> "Erledigt";
        };
    }

    private void refreshGrid() {
        projectGrid.setItems(projectService.findAccessibleActiveProjects());
    }

    private void refreshTaskGrid() {
        if (currentProject != null) {
            taskGrid.setItems(taskService.findByProject(currentProject));
        }
    }
}

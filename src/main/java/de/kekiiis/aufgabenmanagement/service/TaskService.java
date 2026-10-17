package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.entity.Task;
import de.kekiiis.aufgabenmanagement.repository.TaskRepository;
import de.kekiiis.aufgabenmanagement.repository.ProjectRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service 
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectPermissionService projectPermissionService;

    private static final String PROJEKTFEHLERMELDUNG = "Das Projekt wurde nicht gefunden.";
    private static final String AUFGABENFEHLERMELDUNG = "Die Aufgabe wurde nicht gefunden";

    public TaskService(
        TaskRepository taskRepository, 
        ProjectRepository projectRepository, 
        ProjectPermissionService projectPermissionService) {
            
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectPermissionService = projectPermissionService;
    }

    @Transactional
    public List<Task> findByProject(Project project) {
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException(PROJEKTFEHLERMELDUNG);
        }

        Project projectWithMembers = projectRepository
            .findWithMembersById(project.getId())
            .orElseThrow(() -> new IllegalArgumentException(PROJEKTFEHLERMELDUNG));

        if (!projectPermissionService.canAccessProject(projectWithMembers)) {
            throw new AccessDeniedException(
                "Keine Berechtigung zum Anzeigen der Aufgaben."
            );
        }

        return taskRepository.findByProject(projectWithMembers);
    }

    @Transactional
    public Task save(Task task) {
        if(task == null || task.getProject() == null 
                || task.getProject().getId() == null) {
            throw new IllegalArgumentException(
                "Die Aufgabe muss einem gültigen Projekt zugeordnet sein."
            );
        }

        Project project;
        
        if (task.getId() != null) {
            // Bestehende Aufgabe aus der Datenbank laden.
            Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new IllegalArgumentException(AUFGABENFEHLERMELDUNG));

            // Tatsächlich gespeichertes Projekt verwenden.
            project = projectRepository
                .findWithMembersById(existingTask.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException(PROJEKTFEHLERMELDUNG));
        

            projectPermissionService.requireTaskEditPermission(project);

            // Nur erlaubte Felder übernehmen.
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setTaskStatus(task.getTaskStatus());

            return taskRepository.save(existingTask);
        }

        // Neue Aufgabe
        project = projectRepository
            .findWithMembersById(task.getProject().getId())
            .orElseThrow(() -> new IllegalArgumentException(PROJEKTFEHLERMELDUNG));

        projectPermissionService.requireTaskEditPermission(project);

        task.setProject(project);

        return taskRepository.save(task);
    }

    @Transactional 
    public void delete(Task task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException(AUFGABENFEHLERMELDUNG);
        }

        Task existingTask = taskRepository.findById(task.getId())
            .orElseThrow(() -> new IllegalArgumentException(AUFGABENFEHLERMELDUNG));

        Project project = projectRepository
        .findWithMembersById(existingTask.getProject().getId())
        .orElseThrow(() -> new IllegalArgumentException(PROJEKTFEHLERMELDUNG));

        projectPermissionService.requireTaskEditPermission(project);

        taskRepository.delete(existingTask);
    }
}

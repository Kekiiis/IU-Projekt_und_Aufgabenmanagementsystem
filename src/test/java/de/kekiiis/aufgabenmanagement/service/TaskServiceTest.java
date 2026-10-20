package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.entity.Task;
import de.kekiiis.aufgabenmanagement.repository.ProjectRepository;
import de.kekiiis.aufgabenmanagement.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith (MockitoExtension.class)
class TaskServiceTest {
    
    @Mock 
    private TaskRepository taskRepository;

    @Mock 
    private ProjectRepository projectRepository;

    @Mock 
    private ProjectPermissionService projectPermissionService;

    @InjectMocks 
    private TaskService taskService;

    private String keineBerechtigungText = "Keine Berechtigung zur Bearbeitung der Aufgaben.";

    // Test: Prüft, dass mit Berechtigung die Aufgabe gelesen werden kann.
    @Test
    void authorizedUserCanReadTasks() {
        Project project = mock(Project.class);
        Task task = mock(Task.class);

        when(project.getId()).thenReturn(1L);
        when(projectRepository.findWithMembersById(1L))
            .thenReturn(Optional.of(project));
        when(projectPermissionService.canAccessProject(project))
            .thenReturn(true);
        when(taskRepository.findByProject(project))
            .thenReturn(List.of(task));

        List<Task> result = taskService.findByProject(project);

        assertEquals(1, result.size());
        assertSame(task, result.get(0));
    }

    // Test: Prüft, dass ohne Berechtigung die Aufgabe nicht gelesen werden kann.
    @Test 
    void unauthorizedUserCannotReadTasks() {
        Project project = mock(Project.class);

        when(project.getId()).thenReturn(1L);
        when(projectRepository.findWithMembersById(1L))
            .thenReturn(Optional.of(project));
        when(projectPermissionService.canAccessProject(project))
            .thenReturn(false);

        assertThrows(
            AccessDeniedException.class, 
            () -> taskService.findByProject(project)
        );

        verifyNoInteractions(taskRepository);
    }

    // Test: simuliert eine verweigerte Bearbeitungsberechtigung und prüft, dass keine Aufgabe gespeichert wird.
    @Test 
    void archivedProjectsCannotSaveNewTask() {
        Project project = mock(Project.class);
        
        when(project.getId()).thenReturn(1L);

        Task task = new Task(
            "Testaufgabe",
            "Beschreibung der Testaufgabe",
            project
        );

        when(projectRepository.findWithMembersById(1L))
            .thenReturn(Optional.of(project));

        doThrow(new AccessDeniedException(
            keineBerechtigungText
        )).when(projectPermissionService)
            .requireTaskEditPermission(project);

        assertThrows(
            AccessDeniedException.class, 
            () -> taskService.save(task)  
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    // Test: Prüft, dass ein Benutzer ohne Berechtigung eine bestehende Aufgabe nicht verändern kann.
    @Test
    void unauthorizedUserCannotEditExistingTask() {
        Project project = mock(Project.class);
        Task existingTask = mock(Task.class);
        Task changedTask = mock(Task.class);

        when(changedTask.getId()).thenReturn(1L);
        when(changedTask.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(2L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(existingTask));

        when(existingTask.getProject()).thenReturn(project);

        when(projectRepository.findWithMembersById(2L))
            .thenReturn(Optional.of(project));

        doThrow(new AccessDeniedException(
            keineBerechtigungText
        )).when(projectPermissionService)
            .requireTaskEditPermission(project);

        assertThrows(
            AccessDeniedException.class, 
            () -> taskService.save(changedTask)
        );

        verify(existingTask, never()).setTitle(any());
        verify(existingTask, never()).setDescription(any());
        verify(existingTask, never()).setTaskStatus(any());

        verify(taskRepository, never()).save(any(Task.class));
    }

    // Test: Prüft, dass TaskService eine verweigerte Berechtiung berücksichtigt und die Aufgabe nicht aus der Datenbank löscht.
    @Test 
    void unauthorizedUserCannotDeleteTask() {
        Project project = mock(Project.class);
        Task existingTask = mock(Task.class);
        Task taskToDelete = mock(Task.class);

        when(taskToDelete.getId()).thenReturn(1L);

        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(existingTask));

        when(existingTask.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(2L);

        when(projectRepository.findWithMembersById(2L))
            .thenReturn(Optional.of(project));
        
        doThrow(new AccessDeniedException(
            keineBerechtigungText
        )).when(projectPermissionService)
            .requireTaskEditPermission(project);

        assertThrows(
            AccessDeniedException.class, 
            () -> taskService.delete(taskToDelete)
        );

        verify(taskRepository, never()).delete(any(Task.class));
    }
}

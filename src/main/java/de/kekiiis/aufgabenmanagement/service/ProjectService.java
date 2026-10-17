package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.repository.ProjectRepository;

import org.springframework.stereotype.Service;

import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectPermissionService projectPermissionService;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectPermissionService projectPermissionService) {
            
        this.projectRepository = projectRepository;
        this.projectPermissionService = projectPermissionService;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
    public void archive(Project project) {
        project.setArchived(true);
        projectRepository.save(project);
    }

    @RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
    public void restore(Project project) {
        project.setArchived(false);
        projectRepository.save(project);
    }

    public List<Project> findActiveProjects() {
        return projectRepository.findByArchivedFalse();
    }

    // Prüft, ob die angemeldete Person Zugriff auf das Projekt hat und gibt nur erlaubte Projekte zurück.
    public List<Project> findAccessibleActiveProjects() {
        return projectRepository.findByArchivedFalse().stream()
            .map(project -> findByIdWithMembers(project.getId()))
            .filter(projectPermissionService::canAccessProject)
            .toList();
    }

    public List<Project> findArchivedProjects() {
        return projectRepository.findByArchivedTrue();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Projekt wurde nicht geladen."
                )
            );
    }

    public Project findByIdWithMembers(Long id) {
        return projectRepository.findWithMembersById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Projekt wurde nicht gefunden."
                )
            );
    }
}
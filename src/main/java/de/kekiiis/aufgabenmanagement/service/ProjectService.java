package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.Project;
import de.kekiiis.aufgabenmanagement.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void archive(Project project) {
        project.setArchived(true);
        projectRepository.save(project);
    }

    public void restore(Project project) {
        project.setArchived(false);
        projectRepository.save(project);
    }

    public List<Project> findActiveProjects() {
        return projectRepository.findByArchivedFalse();
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
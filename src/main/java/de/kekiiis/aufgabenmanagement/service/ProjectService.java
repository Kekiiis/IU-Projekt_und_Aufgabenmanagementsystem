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
}
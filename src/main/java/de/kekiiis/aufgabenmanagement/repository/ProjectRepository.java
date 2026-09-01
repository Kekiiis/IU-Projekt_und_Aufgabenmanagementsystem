package de.kekiiis.aufgabenmanagement.repository;

import de.kekiiis.aufgabenmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    
}

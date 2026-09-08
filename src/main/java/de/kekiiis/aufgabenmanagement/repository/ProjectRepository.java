package de.kekiiis.aufgabenmanagement.repository;

import de.kekiiis.aufgabenmanagement.entity.Project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Override
    @EntityGraph(attributePaths = {"members"})
    List<Project> findAll();

    @EntityGraph(attributePaths = {"members"})
    List<Project> findByArchivedFalse();

    @EntityGraph(attributePaths = {"members"})
    List<Project> findByArchivedTrue();

    @EntityGraph(attributePaths = {"members"})
    Optional<Project> findWithMembersById(Long id);
    
}

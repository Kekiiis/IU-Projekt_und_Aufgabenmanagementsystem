package de.kekiiis.aufgabenmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tasks") // Tabelle für die Aufgaben
public class Task {
    
    // Aufgaben ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aufgabentitel
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    // Aufgabenbeschreibung
    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    // Aufgabenstatus
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.OPEN;

    // Projekt, zu dem die Aufgabe gehört
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    protected Task() {
        // von JPA benötigt
    }

    public Task(String title, String description, Project project) {
        this.title = title;
        this.description = description;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return status;
    }

    public void setTaskStatus(TaskStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}

package de.kekiiis.aufgabenmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects") // Tabelle für die Projekte
public class Project {
    
    // Projekt ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Projekt Name
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    // Projektbeschreibung
    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    // Archiviertes Projekt
    @Column(nullable = false)
    private boolean archived = false;

    // Zugeteilte Benutzer ID als Projekt Manager 
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_leader_id", nullable = false)
    private AppUser projectLeader;

    // Zugeteilte Benutzer, die an dem Projekt arbeiten
    @ManyToMany
    @JoinTable (
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> members = new HashSet<>();

    protected Project() {
        // Von JPA benötigt.
    }

    public Project(String name, String description, AppUser projectLeader) {
        this.name = name;
        this.description = description;
        this.projectLeader = projectLeader;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public AppUser getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(AppUser projectLeader) {
        this.projectLeader = projectLeader;
    }

    public Set<AppUser> getMembers() {
        return members;
    }

    public void setMembers(Set<AppUser> members) {
        this.members = members;
    }

    public void addMember(AppUser user) {
        members.add(user);
    }

    public void removeMember(AppUser user) {
        members.remove(user);
    }
}
package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProjectPermissionService {
    
    private final AppUserService appUserService;

    public ProjectPermissionService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public boolean canAccessProject(Project project) {
        if (project == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        AppUser currentUser = appUserService.findByUsername(authentication.getName()).orElse(null);

        if (currentUser == null) {
            return false;
        }

        Long userId = currentUser.getId();

        boolean isProjectLeader = project.getProjectLeader() != null && userId.equals(project.getProjectLeader().getId());

        boolean isMember = project.getMembers().stream()
            .anyMatch(member -> userId.equals(member.getId()));

        return isProjectLeader || isMember;
    }

    public boolean canEditTasks(Project project) {
        return canAccessProject(project) && !project.isArchived();
    }

    public void requireTaskEditPermission(Project project) {
        if (!canEditTasks(project)) {
            throw new AccessDeniedException("Keine Berechtigung zur Bearbeitung der Aufgaben.");
        }
    }
}

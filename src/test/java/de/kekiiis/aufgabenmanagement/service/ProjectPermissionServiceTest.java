package de.kekiiis.aufgabenmanagement.service;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith (MockitoExtension.class)
class ProjectPermissionServiceTest {

    @Mock
    private AppUserService appUserService;

    @AfterEach 
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Test: Admin darf auf das Projekt zugreifen.
    @Test 
    void adminCanAccessProject() {
        Project project = mock(Project.class);

        authenticate("admin", "ROLE_ADMIN");

        ProjectPermissionService permissionService = 
            new ProjectPermissionService(appUserService);

        assertTrue(permissionService.canAccessProject(project));
    }

    // Test: Zugewiesener Mitarbeiter darf auf das Projekt zugreifen.
    @Test 
    void projectMemberCanAccessProject() {
        AppUser user = mock(AppUser.class);
        AppUser projectLeader = mock(AppUser.class);
        Project project = mock(Project.class);

        when(user.getId()).thenReturn(1L);
        when(projectLeader.getId()).thenReturn(2L);
        when(project.getProjectLeader()).thenReturn(projectLeader);
        when(project.getMembers()).thenReturn(Set.of(user));
        when(appUserService.findByUsername("Mitarbeiterin01"))
            .thenReturn(Optional.of(user));

        authenticate("Mitarbeiterin01", "ROLE_EMPLOYEE");

        ProjectPermissionService permissionService = 
            new ProjectPermissionService(appUserService);

        assertTrue(permissionService.canAccessProject(project));
    }

    // Test: Nicht zugewiesener Mitarbeiter darf nicht auf das Projekt zugreifen.
    @Test 
    void unrelatedUserCannotAccessProject() {
        AppUser user = mock(AppUser.class);
        AppUser projectLeader = mock(AppUser.class);
        Project project = mock(Project.class);

        when(user.getId()).thenReturn(1L);
        when(projectLeader.getId()).thenReturn(2L);
        when(project.getProjectLeader()).thenReturn(projectLeader);
        when(project.getMembers()).thenReturn(Set.of());
        when(appUserService.findByUsername("Mitarbeiterin01"))
            .thenReturn(Optional.of(user));

        authenticate("Mitarbeiterin01", "ROLE_EMPLOYEE");

        ProjectPermissionService permissionService =
            new ProjectPermissionService(appUserService);

        assertFalse(permissionService.canAccessProject(project));
    }

    // Test: Archivierte Aufgaben können nicht mehr bearbeitet werden. Auch nicht von Admins.
    @Test 
    void archivedProjectCannotBeEdited() {
        Project project = mock(Project.class);

        when(project.isArchived()).thenReturn(true);

        authenticate("admin", "ROLE_ADMIN");

        ProjectPermissionService permissionService =
            new ProjectPermissionService(appUserService);

        assertFalse(permissionService.canEditTasks(project));

        assertThrows(
            AccessDeniedException.class,
            () -> permissionService.requireTaskEditPermission(project)
        );
    }

    private void authenticate(String username, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
            username,
            "password",
            List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
    
}
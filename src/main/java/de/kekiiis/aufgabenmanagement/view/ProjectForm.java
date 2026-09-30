package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import de.kekiiis.aufgabenmanagement.entity.AppUser;
import de.kekiiis.aufgabenmanagement.entity.Project;

import java.util.List;

public class ProjectForm extends FormLayout {

    private boolean editMode = false;
    private boolean editingAllowed = true;
    
    private final TextField name = new TextField("Name");
    private final TextArea description = new TextArea("Beschreibung");

    private final ComboBox<AppUser> projectLeader = 
        new ComboBox<>("Projektleitung");

    private final MultiSelectComboBox<AppUser> members = 
        new MultiSelectComboBox<>("Mitarbeitende");

    private final Button editButton = new Button("Bearbeiten");
    private final Button saveButton = new Button("Speichern");
    private final Button cancelButton = new Button("Abbrechen");

    private final Binder<Project> binder = new Binder<>(Project.class);

    private Project project;

    public ProjectForm(List<AppUser> users) {

        configureUserFields(users);

        name.setRequiredIndicatorVisible(true);
        projectLeader.setRequiredIndicatorVisible(true);

        setResponsiveSteps(
            new ResponsiveStep("0", 1)
        );

        name.setWidthFull();
        description.setWidthFull();
        projectLeader.setWidthFull();
        members.setWidthFull();

        HorizontalLayout buttons = new HorizontalLayout(
            cancelButton,
            editButton,
            saveButton
        );

        buttons.setWidthFull();
        buttons.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        
        members.setWidthFull();

        add(
            name,
            description,
            projectLeader,
            members,
            buttons
        );

        binder.forField(name)
            .asRequired("Name darf nicht leer sein.")
            .bind(Project::getName, Project::setName);

        binder.forField(description)
            .bind(Project::getDescription, Project::setDescription);

        binder.forField(projectLeader)
            .asRequired("Bitte eine Projektleitung auswählen.")
            .bind(Project::getProjectLeader, Project::setProjectLeader);

        binder.forField(members)
            .bind(Project::getMembers, Project::setMembers);

        editButton.addClickListener(event -> setEditMode(true));

        saveButton.addClickListener(event -> validateAndSave());

        cancelButton.addClickListener(event -> 
            fireEvent(new CancelEvent(this))
        );
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;

        name.setReadOnly(!editMode);
        description.setReadOnly(!editMode);
        projectLeader.setReadOnly(!editMode);
        members.setReadOnly(!editMode);

        editButton.setVisible(editingAllowed && !editMode);
        saveButton.setVisible(editMode);
        cancelButton.setText(editMode ? "Abbrechen" : "Schließen");
    }

    public void setEditingAllowed(boolean editingAllowed) {
        this.editingAllowed = editingAllowed;
        editButton.setVisible(editingAllowed && !editMode);
    }

    private void configureUserFields(List<AppUser> users) {

        projectLeader.setItems(users);
        members.setItems(users);

        projectLeader.setItemLabelGenerator(this::getUserDisplayName);
        members.setItemLabelGenerator(this::getUserDisplayName);
    }

    private String getUserDisplayName(AppUser user) {
        return user.getFirstName()
            + " "
            + user.getLastName()
            + " (" + user.getUsername() + ")"; 
    }

    public void setProject(Project project) {
        this.project = project;
        binder.readBean(project);
    }

    public void clearForm() {
        project = null;
        binder.readBean(null);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(project);
            fireEvent(new SaveEvent(this, project));
        } catch (ValidationException e) {
            Notification.show("Bitte prüfe die Eingaben.");
        }
    }

    // Events des Formulars 

    public static abstract class ProjectFormEvent
            extends ComponentEvent<ProjectForm> {

        private final Project project;

        protected ProjectFormEvent(
            ProjectForm source,
            Project project
        ) {
            super(source, false);
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

    public static class SaveEvent extends ProjectFormEvent {

        public SaveEvent(
            ProjectForm source, Project project
        ) {
            super(source, project);
        }
    }

    public static class CancelEvent extends ProjectFormEvent {

        public CancelEvent(ProjectForm source) {
            super(source, null);
        }
    }

    // Listener 

    public Registration addSaveListener(
            ComponentEventListener<SaveEvent> listener
    ) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCancelListener(
        ComponentEventListener<CancelEvent> listener
    ) {
        return addListener(CancelEvent.class, listener);
    }
}
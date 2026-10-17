package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import de.kekiiis.aufgabenmanagement.entity.Task;

public class TaskForm extends FormLayout{

    private boolean editMode = false;
    private boolean editingAllowed = true;
    
    private final TextField title = new TextField("Titel");
    private final TextArea description = new TextArea("Beschreibung");

    private final Button editButton = new Button("Bearbeiten");
    private final Button saveButton = new Button("Speichern");
    private final Button cancelButton = new Button("Abbrechen");

    private final Binder<Task> binder = new Binder<>(Task.class);

    private Task task;

    public TaskForm() {

        title.setRequiredIndicatorVisible(true);
        title.setWidthFull();

        description.setWidthFull();

        setResponsiveSteps(new ResponsiveStep("0",1));

        HorizontalLayout buttons = new HorizontalLayout(
            cancelButton, 
            editButton,
            saveButton
        );

        buttons.setWidthFull();
        buttons.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        add(
            title,
            description,
            buttons
        );

        binder.forField(title)
            .asRequired("Der Titel darf nicht leer sein.")
            .bind(Task::getTitle, Task::setTitle);

        binder.forField(description)
            .bind(Task::getDescription, Task::setDescription);

        saveButton.addClickListener(event -> validateAndSave());

        editButton.addClickListener(event -> setEditMode(true));

        cancelButton.addClickListener(event -> 
            fireEvent(new CancelEvent(this))
        );

        setEditMode(false);
    }
    // Bearbeitungsmodus wird nur aktiviert, wenn editingAllowed auf true steht.
    public void setEditMode(boolean editMode) {
        this.editMode = editMode && editingAllowed;

        title.setReadOnly(!this.editMode);
        description.setReadOnly(!this.editMode);

        editButton.setVisible(editingAllowed && !this.editMode);
        saveButton.setVisible(editingAllowed && this.editMode);

        cancelButton.setText(this.editMode ? "Abbrechen" : "Schließen");
    }

    // Formular wird schreibgeschützt, wenn die Bearbeitungsberechtigung entzogen wird.
    public void setEditingAllowed(boolean editingAllowed) {
        this.editingAllowed = editingAllowed;
        setEditMode(this.editMode);
    }

    public void setTask(Task task) {
        this.task = task;
        binder.readBean(task);
    }

    public void clearForm() {
        task = null;
        binder.readBean(null);
    }

    public void validateAndSave() {
        if (!editingAllowed || !editMode || task == null) {
            Notification.show("Du darfst diese Aufgabe nicht bearbeiten.");
            return;
        }

        try {
            binder.writeBean(task);
            fireEvent(new SaveEvent(this, task));
        } catch (ValidationException e) {
            Notification.show("Bitte prüfe die Eingaben.");
        }
    }

    // Event des Formulars
    public static abstract class TaskFormEvent extends ComponentEvent<TaskForm> {
        
        private final Task task;

        protected TaskFormEvent(
            TaskForm source,
            Task task
        ) {
            super(source, false);
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class SaveEvent extends TaskFormEvent {

        public SaveEvent(TaskForm source, Task task) {
            super(source, task);
        }
    }

    public static class CancelEvent extends TaskFormEvent {

        public CancelEvent(TaskForm source) {
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

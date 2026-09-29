package de.kekiiis.aufgabenmanagement.view;

import org.aspectj.weaver.ast.Not;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import de.kekiiis.aufgabenmanagement.entity.Role;
import de.kekiiis.aufgabenmanagement.service.AppUserService;

public class UserForm extends Dialog {

    private final AppUserService appUserService;

    private final TextField username = new TextField("Benutzername");
    private final EmailField email = new EmailField("E-Mail-Adresse");
    private final TextField firstName = new TextField("Vorname");
    private final TextField lastName = new TextField("Nachname");
    private final PasswordField password = new PasswordField("Passwort");
    private final Select<Role> role = new Select<>();

    public UserForm(AppUserService appUserService, Runnable onUserCreated) {
        this.appUserService = appUserService;

        setHeaderTitle("Neuen Benutzer anlegen");
        setWidth("500px");

        configureFields();

        FormLayout formLayout = new FormLayout(
            username,
            email,
            firstName,
            lastName,
            password,
            role
        );

        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );

        formLayout.setColspan(email, 2);
        formLayout.setColspan(password, 2);
        formLayout.setColspan(role, 2);

        Button cancelButton = new Button("Abbrechen", event -> close());

        Button saveButton = new Button("Benutzer anlegen", event -> {
            if (saveUser()) {
                onUserCreated.run();
                close();
            }
        });

        add(formLayout);

        getFooter().add(cancelButton, saveButton);
    }

    private void configureFields() {
        username.setRequired(true);
        username.setMaxLength(100);

        email.setRequiredIndicatorVisible(true);
        email.setMaxLength(255);

        firstName.setRequired(true);
        firstName.setMaxLength(100);

        lastName.setRequired(true);
        lastName.setMaxLength(100);

        password.setRequired(true);
        password.setMinLength(12);
        password.setHelperText("Mindestens 12 Zeichen");

        role.setLabel("Rolle");
        role.setItems(Role.values());
        role.setItemLabelGenerator(this::getRoleLabel);
        role.setRequiredIndicatorVisible(true);
        role.setEmptySelectionAllowed(false);
    }

    private String getRoleLabel(Role role) {
        return switch (role) {
            case ADMIN -> "Administrator:in";
            case PROJECT_MANAGER -> "Projektleiter:in";
            case EMPLOYEE -> "Mitarbeiter:in";
        };
    }

    private boolean saveUser() {
        if (username.isEmpty()
            || email.isEmpty()
            || firstName.isEmpty()
            || lastName.isEmpty()
            || password.isEmpty()
            || role.isEmpty()) {

            showError("Bitte fülle alle Pflichtfelder aus.");
            return false;
        }

        if (email.isInvalid()) {
            showError("Bitte gib eine gültige E-Mail-Adresse ein.");
            return false;
        }

        if (password.getValue().length() < 12) {
            showError("Das Passwort muss mindestens 12 Zeichen lang sein.");
            return false;
        }

        try {
            appUserService.createUser(
                username.getValue(), 
                email.getValue(), 
                password.getValue(), 
                firstName.getValue(), 
                lastName.getValue(), 
                role.getValue()
            );

            Notification.show(
                "Benutzerkonto wurde angelegt.",
                3000,
                Notification.Position.MIDDLE
            );

            return true;
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(
            message,
            5000,
            Notification.Position.MIDDLE
        );

        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
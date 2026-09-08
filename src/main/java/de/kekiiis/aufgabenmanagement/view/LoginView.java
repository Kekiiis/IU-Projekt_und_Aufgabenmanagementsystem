package de.kekiiis.aufgabenmanagement.view;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Anmelden")
@AnonymousAllowed // wird benötigt, damit nicht eingeloggte Benutzer die Login Seite aufrufen können.
public class LoginView extends VerticalLayout
        implements BeforeEnterObserver {
    
    private final LoginForm loginForm = new LoginForm();

    private final Span capsLockWarning = new Span("⚠ Feststelltaste ist aktiviert!");

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Anmelden");
        form.setUsername("Benutzername");
        form.setPassword("Passwort");
        form.setSubmit("Anmelden");
        i18n.setForm(form);

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Benutzername oder Passwort falsch");
        errorMessage.setMessage("Bitte überprüfe deine Anmeldedaten und versuche es erneut.");
        i18n.setErrorMessage(errorMessage);

        loginForm.setI18n(i18n);

        capsLockWarning.getStyle()
            .set("color", "red")
            .set("font-weight", "bold");

        capsLockWarning.setVisible(false);

        getElement().executeJs("""
            const view = this;

            const updateCapsLockState = (event) => {
                if (typeof event.getModifierState === 'function') {
                    const capsLockOn = event.getModifierState('CapsLock');

                    view.$server.setCapsLockWarning(capsLockOn);
                }
            };

            window.addEventListener('keydown', updateCapsLockState);
            window.addEventListener('keyup', updateCapsLockState);
        """);

        add(
            new H1("Projekt- und Aufgabenmanagement"),
            loginForm,
            capsLockWarning
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {

            loginForm.setError(true);
        }
    }

    @ClientCallable
    public void setCapsLockWarning(boolean capsLockOn) {
        capsLockWarning.setVisible(capsLockOn);
    }
}

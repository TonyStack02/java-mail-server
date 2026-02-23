package client.controller;

import client.model.ClientModel;
import client.view.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 * Controller per la gestione della finestra di Login.
 * <p>
 * Gestisce l'autenticazione dell'utente, occupandosi di:
 * <ul>
 * <li>Validare sintatticamente l'indirizzo email (Regex).</li>
 * <li>Interrogare il Model per l'accesso al server.</li>
 * <li>Gestire gli errori di connessione o di utente non autorizzato.</li>
 * </ul>
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    private ClientModel model;
    private ViewFactory viewFactory;

    /**
     * Inizializza il controller e imposta i listener per l'interfaccia.
     * Include il login tramite tasto INVIO.
     */
    public void initModel(ClientModel model, ViewFactory viewFactory) {
        this.model = model;
        this.viewFactory = viewFactory;

        // Permette di premere INVIO invece di cliccare il bottone
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onLoginButton();
            }
        });
    }

    /**
     * Gestisce l'azione di Login.
     * Esegue una validazione rigorosa in due step:
     * 1. Locale: Controllo sintattico tramite Regex.
     * 2. Remota: Controllo esistenza utente tramite Server.
     */
    @FXML
    protected void onLoginButton() {
        // Leggiamo l'input
        String email = emailField.getText();

        // Controllo campo vuoto
        if (email.isEmpty()) {
            errorLabel.setText("Inserisci un'email!");
            return;
        }

        // --- VALIDAZIONE REGEX ---
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

        if (!email.matches(emailRegex)) {
            errorLabel.setText("Formato email non valido! (es: mario@test.it)");
            return;
        }

        try {
            // Proviamo a fare il Login
            // Se l'utente non è nella lista  del server, lancio un'eccezione
            model.login(email);

            // Se siamo qui utente accettato
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            viewFactory.closeStage(currentStage);
            viewFactory.showInboxWindow();

        } catch (Exception e) {
            //Server spento o utente non trovato
            errorLabel.setText(e.getMessage());
            System.out.println("Login fallito: " + e.getMessage());
        }
    }
}
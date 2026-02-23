package client.controller;

import client.model.ClientModel;
import client.view.ViewFactory;
import common.Email;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;



/**
 * Controller per la finestra di composizione, risposta e inoltro email.
 * <p>
 * Responsabilità principali:
 * <ul>
 * <li>Parsing e validazione di destinatari multipli (separati da virgola).</li>
 * <li>Creazione dell'oggetto Email e invio tramite Model.</li>
 * <li>Precompilazione campi in caso di Reply/Forward.</li>
 * </ul>
 */
public class WriteMailController {

    @FXML private TextField recipientField; // A:
    @FXML private TextField subjectField;   // Oggetto:
    @FXML private TextArea messageArea;     // Corpo del messaggio
    @FXML private Label errorLabel;         // Feedback errori

    private ClientModel model;
    private ViewFactory viewFactory;

    public void initModel(ClientModel model, ViewFactory viewFactory) {
        this.model = model;
        this.viewFactory = viewFactory;
    }

    /**
     * Gestisce l'invio del messaggio.
     * Effettua il parsing della stringa dei destinatari gestendo liste multiple
     * e validando ogni singolo indirizzo tramite Regex.
     */
    @FXML
    protected void onSendButton() {
        // Raccolgo i dati
        String sender = model.getCurrentUser();
        String recipientText = recipientField.getText();
        String subject = subjectField.getText();
        String text = messageArea.getText();

        // Controllo campo vuoto
        if (recipientText.isEmpty()) {
            errorLabel.setText("Inserisci almeno un destinatario!");
            return;
        }

        // GESTIONE DESTINATARI MULTIPLI
        // Spezziamo la stringa ogni volta che troviamo una virgola
        String[] recipientArray = recipientText.split(",");

        // Creiamo la lista finale pulita
        java.util.List<String> validRecipients = new java.util.ArrayList<>();

        // Regex per validazione email
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

        for (String mail : recipientArray) {
            String cleanMail = mail.trim(); // Rimuove spazi extra

            // Se anche solo una mail è sbagliata blocchiamo l'invio
            if (!cleanMail.matches(emailRegex)) {
                errorLabel.setText("Indirizzo non valido: " + cleanMail);
                return;
            }

            validRecipients.add(cleanMail);
        }

        // Creo l'oggetto Email
        Email email = new Email(sender, validRecipients, subject, text);

        try {
            // Invio tramite il Model
            model.sendEmail(email);

            // Chiudo la finestra su successo
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            stage.close();
            System.out.println("Email inviata correttamente!");

        } catch (java.io.IOException | ClassNotFoundException e) {
            errorLabel.setText("Errore invio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Metodo di utilità per pre-compilare i campi.
     * Usato da InboxController per le funzioni "Rispondi", "Rispondi a tutti" e "Inoltra".
     */
    public void fillFields(String to, String subject, String body) {
        recipientField.setText(to);
        subjectField.setText(subject);
        messageArea.setText(body);
    }
}
package client.controller;

import client.model.ClientModel;
import client.view.ViewFactory;
import common.Email;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/**
 * Controller principale per la schermata della posta in arrivo (Inbox).
 * <p>
 * Questa classe gestisce l'interazione tra l'utente e la lista delle email
 * Si occupa di:
 * <ul>
 * <li>Visualizzare la lista delle email in una TableView.</li>
 * <li>Mostrare i dettagli del messaggio selezionato.</li>
 * <li>Gestire le azioni di risposta, inoltro e cancellazione.</li>
 * </ul>
 */
public class InboxController {

    @FXML private Label userLabel;
    @FXML private TableView<Email> emailTable;
    @FXML private TableColumn<Email, String> senderCol;
    @FXML private TableColumn<Email, String> subjectCol;
    @FXML private TableColumn<Email, String> dateCol;

    // Dettaglio mail
    @FXML private Label selectedSubjectLabel;
    @FXML private Label selectedSenderLabel;
    @FXML private Label selectedDateLabel;
    @FXML private TextArea emailContentArea;

    @FXML private Button replyBtn;
    @FXML private Button replyAllBtn;
    @FXML private Button forwardBtn;

    // Stato connessione
    @FXML private Label statusLabel;

    private ClientModel model;
    private ViewFactory viewFactory;

    //Servizio di aggiornamento automatico
    private ScheduledService<List<Email>> emailUpdateService;

    /**
     * Inizializza il controller collegando il Model e configurando la TableView.
     * Avvia il servizio di aggiornamento automatico.
     */
    public void initModel(ClientModel model, ViewFactory viewFactory) {
        this.model = model;
        this.viewFactory = viewFactory;

        userLabel.setText("Account: " + model.getCurrentUser());

        // Binding delle colonne della tabella con le proprietà dell'oggetto Email
        senderCol.setCellValueFactory(new PropertyValueFactory<>("mittente"));
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("oggetto"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dataFormattata"));

        emailTable.setItems(model.getInbox());

        // Listener per gestire la selezione di una riga
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showEmailDetails(newSelection);
            } else {
                clearDetails();
            }
        });

        // AVVIO L'AGGIORNAMENTO AUTOMATICO
        startAutomaticUpdates();
    }

    /**
     * Avvia un servizio in background (in un Thread separato) che richiede periodicamente
     * al server le nuove email.
     * <p>
     * <b>Gestione della Concorrenza:</b> Il task viene eseguito fuori dal JavaFX Application Thread
     * per non bloccare l'interfaccia grafica.
     * <p>
     * <b>Scalabilità:</b>Non scarica l'intera inbox ad ogni ciclo,
     * ma richiede solo i messaggi arrivati dopo l'ultimo aggiornamento noto.
     */
    private void startAutomaticUpdates() {
        emailUpdateService = new ScheduledService<List<Email>>() {
            @Override
            protected Task<List<Email>> createTask() {
                return new Task<List<Email>>() {
                    @Override
                    protected List<Email> call() throws Exception {
                        // Eseguito in un thread background
                        return model.getNewEmailsFromServer();
                    }
                };
            }
        };

        // Polling ogni 5 secondi
        emailUpdateService.setPeriod(Duration.seconds(5));

        // Callback in caso di SUCCESSO (eseguita nel JavaFX Thread)
        emailUpdateService.setOnSucceeded(event -> {
            List<Email> nuoviMessaggi = emailUpdateService.getValue();

            // Feedback visuale connessione
            statusLabel.setText("Connesso - Aggiornato");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Aggiornamento Lista (Solo se ci sono novità)
            if (nuoviMessaggi != null && !nuoviMessaggi.isEmpty()) {

                // Se è il primo avvio (lista vuota), riempiamo tutto
                if (model.getInbox().isEmpty()) {
                    model.getInbox().setAll(nuoviMessaggi);
                } else {
                    // Altrimenti aggiungiamo in coda solo le nuove mail
                    model.getInbox().addAll(nuoviMessaggi);

                    // Ordinamento per data decrescente
                    model.getInbox().sort((e1, e2) -> e2.getDataSpedizione().compareTo(e1.getDataSpedizione()));

                    // Notifica utente
                    showNotification("Nuova mail ricevuta!", "Hai " + nuoviMessaggi.size() + " nuovi messaggi.");
                }
            }
        });

        // Callback in caso di fallimento (es. Server spento)
        emailUpdateService.setOnFailed(event -> {
            statusLabel.setText("Disconnesso / Errore Server");
            statusLabel.setStyle("-fx-text-fill: red;");
        });

        emailUpdateService.start();
    }

    /**
     * Mostra una notifica a schermo (Alert) in modo Thread-Safe.
     */
    private void showNotification(String titolo, String messaggio) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifica MailClient");
            alert.setHeaderText(titolo);
            alert.setContentText(messaggio);
            alert.show();
        });
    }

    private void showEmailDetails(Email email) {
        selectedSubjectLabel.setText(email.getOggetto());
        selectedSenderLabel.setText("Da: " + email.getMittente());
        selectedDateLabel.setText("Data: " + email.getDataFormattata());
        emailContentArea.setText(email.getTesto());

        replyBtn.setDisable(false);
        replyAllBtn.setDisable(false);
        forwardBtn.setDisable(false);
    }

    private void clearDetails() {
        selectedSubjectLabel.setText("Nessuna mail selezionata");
        selectedSenderLabel.setText("");
        selectedDateLabel.setText("");
        emailContentArea.setText("");
    }

    @FXML
    protected void onWriteAction() {
        viewFactory.showWriteMailWindow();
    }

    /**
     * Gestisce la cancellazione di una email.
     */
    @FXML
    protected void onDeleteAction() {
        Email selectedEmail = emailTable.getSelectionModel().getSelectedItem();
        if (selectedEmail != null) {
            try {
                model.deleteEmail(selectedEmail);
                clearDetails();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Errore cancellazione: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onRefreshAction() {
        if (emailUpdateService.isRunning()) {
            emailUpdateService.cancel();
        }
        emailUpdateService.restart();
    }

    /**
     * Prepara una risposta al solo mittente.
     */
    @FXML
    protected void onReplyAction() {
        Email selected = emailTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String oggetto = selected.getOggetto();
            String nuovoOggetto = oggetto.startsWith("Re:") ? oggetto : "Re: " + oggetto; //per non duplicare "Re:"

            viewFactory.showWriteMailWindow(selected.getMittente(), nuovoOggetto, "");
        }
    }

    /**
     * Risponde a tutti i destinatari (Reply-All).
     * Filtra automaticamente l'indirizzo dell'utente corrente per evitare l'autoinvio.
     */
    @FXML
    protected void onReplyAllAction() {
        Email selected = emailTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            String io = model.getCurrentUser();
            String mittenteOriginale = selected.getMittente();
            List<String> destinatariOriginali = selected.getDestinatari();

            java.util.List<String> listaFinale = new java.util.ArrayList<>();

            if (!mittenteOriginale.equals(io)) {
                listaFinale.add(mittenteOriginale);
            }

            for (String destinatario : destinatariOriginali) {
                if (!destinatario.equals(io)) {
                    listaFinale.add(destinatario);
                }
            }

            String tuttoIlMondo = String.join(", ", listaFinale);

            String oggetto = selected.getOggetto();
            String nuovoOggetto = oggetto.startsWith("Re:") ? oggetto : "Re: " + oggetto;

            viewFactory.showWriteMailWindow(tuttoIlMondo, nuovoOggetto, "");
        }
    }

    /**
     * Inoltra il messaggio a nuovi destinatari.
     * Include il testo originale nel corpo del messaggio.
     */
    @FXML
    protected void onForwardAction() {
        Email selected = emailTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String testoInoltrato = "\n\n--- Messaggio Inoltrato ---\n" + selected.getTesto();

            String oggetto = selected.getOggetto();
            String nuovoOggetto = oggetto.startsWith("Fwd:") ? oggetto : "Fwd: " + oggetto;

            viewFactory.showWriteMailWindow("", nuovoOggetto, testoInoltrato);
        }
    }
}
package client.model;

import client.connection.ServerConnection;
import common.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe Model del pattern MVC lato Client.
 * <p>
 * Mantiene lo stato (utente corrente, lista email) e gestisce la logica di business,
 * inclusa la sincronizzazione intelligente con il server.
 */
public class ClientModel {

    private String currentUser;

    // Qualsiasi modifica si riflette automaticamente sulla GUI
    private final ObservableList<Email> inbox;

    private final ServerConnection connection;

    // Mantiene traccia dell'ultimo messaggio ricevuto
    private LocalDateTime ultimoAggiornamento = null;

    public ClientModel() {
        this.inbox = FXCollections.observableArrayList();
        this.connection = new ServerConnection();
    }

    // --- AZIONI PRINCIPALI ---

    /**
     * Effettua il primo login scaricando l'intera casella di posta.
     * Inizializza il timestamp per i futuri aggiornamenti.
     */
    public void login(String email) throws IOException, ClassNotFoundException {
        List<Email> scaricate = connection.login(email);
        this.currentUser = email;
        this.inbox.setAll(scaricate);

        // Aggiorniamo il timestamp all'ultima mail ricevuta
        if (!scaricate.isEmpty()) {
            ultimoAggiornamento = scaricate.get(scaricate.size() - 1).getDataSpedizione();
        }
    }

    public void sendEmail(Email email) throws IOException, ClassNotFoundException {
        connection.sendEmail(email);
    }

    public void deleteEmail(Email email) throws IOException, ClassNotFoundException {
        connection.deleteEmail(currentUser, email);
        inbox.remove(email);
    }

    /**
     * Aggiornamento completo manuale
     */
    public void refresh() throws IOException, ClassNotFoundException {
        if (currentUser != null) {
            List<Email> scaricate = connection.login(currentUser);
            this.inbox.setAll(scaricate);
            if (!scaricate.isEmpty()) {
                ultimoAggiornamento = scaricate.get(scaricate.size() - 1).getDataSpedizione();
            }
        }
    }

    /**
     * Viene chiamato periodicamente dal Controller.
     * Invece di scaricare tutto, invia al server la data dell'ultimo aggiornamento
     * e riceve solo i messaggi nuovi.
     */
    public List<Email> getNewEmailsFromServer() throws IOException, ClassNotFoundException {
        // Se non abbiamo ancora fatto login o non abbiamo dati, facciamo un fetch completo
        if (ultimoAggiornamento == null) {
            List<Email> tutte = connection.login(currentUser);
            if (!tutte.isEmpty()) {
                ultimoAggiornamento = tutte.get(tutte.size() - 1).getDataSpedizione();
            }
            return tutte;
        }

        // Altrimenti chiediamo solo le novità
        List<Email> nuove = connection.getUpdates(currentUser, ultimoAggiornamento);

        if (!nuove.isEmpty()) {
            // Aggiorniamo il timestamp all'ultima delle nuove mail
            ultimoAggiornamento = nuove.get(nuove.size() - 1).getDataSpedizione();
        }

        return nuove;
    }

    // --- GETTERS ---

    public ObservableList<Email> getInbox() {
        return inbox;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public LocalDateTime getUltimoAggiornamento() {
        return ultimoAggiornamento;
    }
}
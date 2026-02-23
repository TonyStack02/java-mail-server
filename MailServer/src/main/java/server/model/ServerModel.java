package server.model;

import common.Email;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import server.network.ConnectionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Modello principale del Server.
 * <p>
 * Responsabilità:
 * <ul>
 * <li>Mantiene lo stato globale dell'applicazione server.</li>
 * <li>Coordina la logica di business (Login, Smistamento posta).</li>
 * <li>Gestisce la lista degli utenti autorizzati (White-list).</li>
 * <li>Interfaccia la rete (ConnectionHandler) con la persistenza (FileManager).</li>
 * </ul>
 */
public class ServerModel {

    // --- CONFIGURAZIONE UTENTI (White-list) ---
    // Definisce l'insieme fisso degli account supportati
    // L'accesso o l'invio verso indirizzi non presenti in questo Set verrà rifiutato.
    private static final Set<String> UTENTI_AMMESSI = Set.of(
            "mario@test.it",
            "luigi@test.it",
            "peach@test.it",
            "spike@test.it"
    );

    // Lista osservabile per i log
    private final ObservableList<String> logs;

    private final FileManager fileManager;
    private final ConnectionHandler connectionHandler;

    public ServerModel() {
        this.logs = FXCollections.observableArrayList();
        this.fileManager = new FileManager();

        // Avvio del servizio di rete sulla porta 8189
        this.connectionHandler = new ConnectionHandler(8189, this);
        this.connectionHandler.startServer();
    }

    public ObservableList<String> getLogs() { return logs; }

    /**
     * Aggiunge una voce al log di sistema.
     * Utilizza  Platform.runLater per garantire che l'aggiornamento della UI
     * avvenga nel Thread corretto di JavaFX.
     */
    public void addLog(String text) {
        Platform.runLater(() -> logs.add(text));
    }

    // --- LOGICA DI BUSINESS ---

    /**
     * Gestisce la ricezione e lo smistamento di una nuova email.
     * <p>
     * Esegue controlli di validità sui destinatari. Se anche uno solo non esiste,
     * l'intera operazione viene abortita lanciando un'eccezione.
     *
     * @param email L'email da smistare.
     * Exception Se uno dei destinatari non è nella white-list.
     */
    public void riceviEmail(Email email) throws Exception {
        // VALIDAZIONE: Controllo esistenza destinatari
        for (String destinatario : email.getDestinatari()) {
            if (!UTENTI_AMMESSI.contains(destinatario)) {
                throw new Exception("Indirizzo inesistente: " + destinatario);
            }
        }

        // Salvataggio nelle caselle dei destinatari
        for (String destinatario : email.getDestinatari()) {
            fileManager.salvaEmail(destinatario, email);
        }

        addLog("Ricevuta mail da " + email.getMittente() + " per " + email.getDestinatari().size() + " destinatari.");
    }

    /**
     * Recupera la posta per un utente autenticato.
     * @param utente L'utente che richiede accesso.
     * @return La lista completa dei messaggi.
     * Exception Se l'utente non è autorizzato.
     */
    public List<Email> getEmailUtente(String utente) throws Exception {
        // SICUREZZA: Controllo autenticazione
        if (!UTENTI_AMMESSI.contains(utente)) {
            addLog("Tentativo di accesso non autorizzato: " + utente);
            throw new Exception("Utente sconosciuto! Accesso negato.");
        }

        addLog("L'utente " + utente + " ha richiesto la posta (Login completo).");
        return fileManager.caricaEmail(utente);
    }

    /**
     * Recupera solo i messaggi successivi a una certa data.
     * @param utente L'utente richiedente.
     * @param lastUpdate Timestamp dell'ultimo aggiornamento noto al client.
     * @return Lista filtrata dei nuovi messaggi.
     */
    public List<Email> getEmailUtenteAfter(String utente, LocalDateTime lastUpdate) throws Exception {
        // Riutilizziamo la logica di autenticazione e caricamento base
        List<Email> tutte = getEmailUtente(utente);

        if (lastUpdate == null) return tutte;

        // FILTRO STREAM: Manteniamo solo i messaggi con data > lastUpdate
        return tutte.stream()
                .filter(e -> e.getDataSpedizione().isAfter(lastUpdate))
                .collect(Collectors.toList());
    }

    public void cancellaEmail(String utente, Email email) {
        fileManager.cancellaEmail(utente, email);
        addLog("L'utente " + utente + " ha cancellato una mail.");
    }

    public void stopServer() {
        connectionHandler.stop();
    }
}
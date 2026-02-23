package client.connection;

import common.Email;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestione della comunicazione di rete tra Client e Server.
 * <p>
 * Implementa un protocollo basato su Socket non persistenti:
 * per ogni operazione viene aperta una nuova connessione, eseguita la richiesta
 * e chiuso il socket.
 */
public class ServerConnection {

    // Indirizzo del Server
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8189;

    /**
     * Effettua il login al server e scarica l'intera casella di posta iniziale.
     *
     * @param emailUtente L'indirizzo email con cui autenticarsi (es. "mario@test.it").
     * @return Una lista contenente tutte le email presenti nella casella di posta dell'utente.
     * IOException Se la connessione fallisce o se il server restituisce un errore (es. utente non trovato).
     * ClassNotFoundException Se la deserializzazione dell'oggetto Email fallisce.
     */
    @SuppressWarnings("unchecked")
    public List<Email> login(String emailUtente) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LOGIN");       // Comando
            out.writeObject(emailUtente);   // Parametro

            String response = (String) in.readObject();

            if ("OK".equals(response)) {
                return (List<Email>) in.readObject();
            } else {
                // Se il server lancia errore, lo rilanciamo qui
                throw new IOException(response);
            }
        }
    }

    /**
     * Richiede al server un aggiornamento della casella di posta.
     * <p>
     * Inviando il timestamp dell'ultimo aggiornamento, il server restituisce solo i messaggi nuovi,
     * @param email L'indirizzo email dell'utente.
     * @param lastUpdate La data e ora dell'ultima email ricevuta (può essere null).
     * @return Una lista contenente solo i nuovi messaggi arrivati dopo {@code lastUpdate}.
     * IOException In caso di errori di rete o risposta negativa del server.
     * ClassNotFoundException In caso di errori di deserializzazione.
     */
    @SuppressWarnings("unchecked")
    public List<Email> getUpdates(String email, LocalDateTime lastUpdate) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("REFRESH");
            out.writeObject(email);
            out.writeObject(lastUpdate); // Data dell'ultimo aggiornamento

            String response = (String) in.readObject();

            if ("OK".equals(response)) {
                return (List<Email>) in.readObject();
            } else {
                throw new IOException(response);
            }
        }
    }

    /**
     * Invia una nuova email al server per lo smistamento.
     *
     * @param email L'oggetto Email
     * IOException Se il server rifiuta l'invio o per errori di rete.
     * ClassNotFoundException In caso di errori di protocollo.
     */
    public void sendEmail(Email email) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SEND");
            out.writeObject(email);

            String response = (String) in.readObject();
            if (!"OK".equals(response)) {
                throw new IOException(response);
            }
        }
    }

    /**
     * Richiede la cancellazione di una email dal server.
     *
     * @param emailUtente L'utente che richiede la cancellazione.
     * @param emailDaCancellare L'oggetto Email da rimuovere (identificato tramite ID).
     * IOException Se l'operazione fallisce lato server.
     * ClassNotFoundException In caso di errori di protocollo.
     */
    public void deleteEmail(String emailUtente, Email emailDaCancellare) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DELETE");
            out.writeObject(emailUtente);
            out.writeObject(emailDaCancellare);

            String response = (String) in.readObject();
            if (!"OK".equals(response)) {
                throw new IOException("Errore cancellazione: " + response);
            }
        }
    }
}
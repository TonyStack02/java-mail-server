package server.network;

import common.Email;
import server.model.ServerModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Worker Thread dedicato alla gestione di una singola richiesta Client.
 * <p>
 * Implementa l'interfaccia Runnable per essere eseguito in parallelo.
 * Gestisce il ciclo di vita di una connessione (apertura, comando, risposta, chiusura).
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerModel model;

    public ClientHandler(Socket socket, ServerModel model) {
        this.socket = socket;
        this.model = model;
    }

    @Override
    public void run() {
        // Try-with-resources assicura la chiusura degli stream
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            //Lettura del comando
            String command = (String) in.readObject();

            switch (command) {
                case "LOGIN":
                    try {
                        String userEmail = (String) in.readObject();

                        List<Email> inbox = model.getEmailUtente(userEmail);

                        // OK + Dati
                        out.writeObject("OK");
                        out.writeObject(inbox);
                    } catch (Exception e) {
                        // Messaggio di errore
                        out.writeObject(e.getMessage());
                    }
                    break;

                case "REFRESH":
                    try {
                        String userEmail = (String) in.readObject();
                        LocalDateTime lastUpdate = (LocalDateTime) in.readObject();

                        List<Email> inbox = model.getEmailUtenteAfter(userEmail, lastUpdate);

                        out.writeObject("OK");
                        out.writeObject(inbox);
                    } catch (Exception e) {
                        out.writeObject(e.getMessage());
                    }
                    break;

                case "SEND":
                    try {
                        Email email = (Email) in.readObject();
                        model.riceviEmail(email);
                        out.writeObject("OK");
                    } catch (Exception e) {
                        out.writeObject(e.getMessage());
                    }
                    break;

                case "DELETE":
                    String user = (String) in.readObject();
                    Email emailDaCancellare = (Email) in.readObject();
                    model.cancellaEmail(user, emailDaCancellare);
                    out.writeObject("OK");
                    break;

                default:
                    System.out.println("Comando sconosciuto ricevuto: " + command);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore comunicazione client: " + e.getMessage());
        } finally {
            // Chiusura sicura del socket
            try { socket.close(); } catch (IOException e) { }
        }
    }
}
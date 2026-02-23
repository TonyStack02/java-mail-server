package server.network;

import server.model.ServerModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Gestore delle connessioni in ingresso.
 * <p>
 * Questa classe gira su un Thread separato e rimane in ascolto sulla porta.
 * Quando un client si connette:
 * 1. Accetta la connessione (Socket).
 * 2. Delega la gestione della richiesta a un nuovo thread (ClientHandler).
 * 3. Torna in ascolto.
 */
public class ConnectionHandler {

    private final int port;
    private final ServerModel model;
    private boolean running = true;

    public ConnectionHandler(int port, ServerModel model) {
        this.port = port;
        this.model = model;
    }

    /**
     * Avvia il ciclo di ascolto del server in un Thread separato
     * per non bloccare l'interfaccia grafica del server.
     */
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                model.addLog("Server avviato e in ascolto sulla porta " + port);

                while (running) {
                    // Il programma si ferma finché non arriva un Client
                    Socket socket = serverSocket.accept();

                    // Appena arriva, creiamo l'operaio (ClientHandler) e lo facciamo partire
                    // Ogni client ha il suo thread
                    ClientHandler handler = new ClientHandler(socket, model);
                    new Thread(handler).start();
                }

            } catch (IOException e) {
                if (running) {
                    model.addLog("Errore ServerSocket: " + e.getMessage());
                } else {
                    System.out.println("Server spento correttamente.");
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        // Il ServerSocket bloccato su accept() lancerà un'eccezione alla chiusura
    }
}
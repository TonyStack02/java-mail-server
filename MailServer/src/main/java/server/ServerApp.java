package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.controller.ServerController;
import server.model.ServerModel;

import java.io.IOException;

/**
 * Classe di avvio del Mail Server.
 * <p>
 * Configura l'architettura MVC lato server:
 * <ol>
 * <li>Inizializza la GUI (JavaFX).</li>
 * <li>Istanzia il Model (gestione dati e persistenza).</li>
 * <li>Collega il Model al Controller.</li>
 * <li>Gestisce la chiusura dei thread e dei socket allo spegnimento.</li>
 * </ol>
 */
public class ServerApp extends Application {

    private ServerModel model;

    @Override
    public void start(Stage stage) throws IOException {
        // Carica la vista (il file FXML)
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApp.class.getResource("/server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 450); // Dimensioni leggermente aumentate

        // --- CARICAMENTO CSS ---
        if (getClass().getResource("/server.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/server.css").toExternalForm());
        }

        //Recupera il Controller creato da JavaFX
        ServerController controller = fxmlLoader.getController();

        //Crea il Model
        this.model = new ServerModel();

        // Passa il Model al Controller
        controller.setModel(model);

        // Impostazioni della finestra
        stage.setTitle("Mail Server - Admin Panel");
        stage.setScene(scene);

        // Gestione chiusura
        stage.setOnCloseRequest(event -> {
            // Spegniamo il server (chiude socket e salva file se necessario)
            model.stopServer();
            // Forza la chiusura di tutti i thread
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
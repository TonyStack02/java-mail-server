package client;

import client.model.ClientModel;
import client.view.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto di ingresso dell'applicazione Client.
 * <p>
 * Estende Application di JavaFX e si occupa del bootstrap dell'architettura:
 * <ol>
 * <li>Istanzia il Model (stato dell'app).</li>
 * <li>Istanzia la ViewFactory (gestione finestre).</li>
 * <li>Lancia la prima schermata (Login).</li>
 * </ol>
 */
public class ClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Creiamo il Model
        ClientModel model = new ClientModel();

        // Creiamo il regista
        ViewFactory viewFactory = new ViewFactory(model);

        // Mostra la schermata di Login
        viewFactory.showLoginWindow();

    }

    public static void main(String[] args) {
        launch();
    }
}
package client.view;

import client.controller.InboxController;
import client.controller.LoginController;
import client.controller.WriteMailController;
import client.model.ClientModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gestore della navigazione dell'interfaccia grafica.
 * <p>
 * Questa classe ha la responsabilità di:
 * <ul>
 * <li>Caricare i file FXML.</li>
 * <li>Creare le nuove finestre (Stage).</li>
 * <li>Inizializzare i Controller iniettando le dipendenze necessarie (Model e ViewFactory).</li>
 * </ul>
 */
public class ViewFactory {

    private final ClientModel model;

    public ViewFactory(ClientModel model) {
        this.model = model;
    }

    /**
     * Carica e mostra la finestra di Login.
     */
    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
        createStage(loader);

        // Dependency Injection: Passiamo il "Cervello" (Model) e il "Regista" (ViewFactory) al Controller
        LoginController controller = loader.getController();
        controller.initModel(model, this);
    }

    /**
     * Carica e mostra la finestra principale (Inbox).
     * Viene chiamata dopo il login.
     */
    public void showInboxWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inbox-view.fxml"));
        createStage(loader);

        InboxController controller = loader.getController();
        controller.initModel(model, this);
    }

    /**
     * Apre la finestra di composizione email pre-compilando i campi.
     * Utilizzato per le funzioni "Rispondi" e "Inoltra".
     *
     * @param destinatario Il destinatario preimpostato (o stringa vuota).
     * @param oggetto L'oggetto preimpostato (es. con "Re:" o "Fwd:").
     * @param testo Il corpo del messaggio (il testo citato).
     */
    public void showWriteMailWindow(String destinatario, String oggetto, String testo) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/write-view.fxml"));
        createStage(loader);

        WriteMailController controller = loader.getController();
        controller.initModel(model, this);

        // Popoliamo i campi della GUI
        controller.fillFields(destinatario, oggetto, testo);
    }

    /**
     * Overload per aprire la finestra di composizione vuota (nuova Mail).
     */
    public void showWriteMailWindow() {
        showWriteMailWindow("", "", "");
    }

    /**
     * Metodo ausiliario per la creazione dello Stage
     * (caricamento FXML e setup della Scene.)
     */
    private void createStage(FXMLLoader loader) {
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // --- CARICAMENTO CSS  ---
            if (getClass().getResource("/client.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());
            }
            // -----------------------------

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chiude una specifica finestra.
     * Per chiudere il Login dopo l'accesso o la finestra di scrittura dopo l'invio.
     *
     */
    public void closeStage(Stage stage) {
        stage.close();
    }
}
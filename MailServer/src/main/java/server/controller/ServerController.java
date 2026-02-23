package server.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.ListChangeListener;
import server.model.ServerModel;

/**
 * Controller per l'interfaccia grafica del Server (Admin Panel).
 * <p>
 * Gestisce la visualizzazione in tempo reale degli eventi di sistema.
 */
public class ServerController {

    @FXML
    private ListView<String> logList;

    @FXML
    private Label lblCount;

    private ServerModel model;

    /**
     * Inizializza il collegamento tra View e Model.
     * <p>
     * Oltre a collegare la lista:
     * 1. Aggiornamento automatico del contatore eventi.
     * 2. Auto-scroll.
     *
     * @param model Il modello del server contenente la logica e i dati.
     */
    public void setModel(ServerModel model) {
        this.model = model;

        // Data Binding: collego la lista grafica direttamente alla ObservableList del model
        logList.setItems(model.getLogs());

        // Listener per Contatore e Auto-scroll
        model.getLogs().addListener((ListChangeListener<String>) change -> {
            Platform.runLater(() -> {
                lblCount.setText("Eventi totali: " + model.getLogs().size());

                // Auto-scroll all'ultimo elemento
                if (!model.getLogs().isEmpty()) {
                    logList.scrollTo(model.getLogs().size() - 1);
                }
            });
        });
    }

    /**
     * Gestisce la pulizia dei log dalla GUI e dalla memoria.
     */
    @FXML
    protected void onClearLogClick() {
        if (model != null) {
            // Cancelliamo i log dalla memoria
            model.getLogs().clear();
        }
    }
}
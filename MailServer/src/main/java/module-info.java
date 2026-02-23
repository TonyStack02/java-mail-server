module mail.server {
    requires javafx.controls;
    requires javafx.fxml;

    // Apre il pacchetto controller a JavaFX (altrimenti non può collegare i bottoni!)
    opens server.controller to javafx.fxml;

    // Apre il pacchetto base (dove c'è ServerApp)
    opens server to javafx.fxml;

    // Esportiamo il pacchetto principale e common
    exports server;
    exports common;
}
module mail.client {
    requires javafx.controls;
    requires javafx.fxml;

    exports client;

    // --- QUESTA È LA RIGA CHE TI MANCA ---
    // Dà il permesso alla TableView di leggere dentro la classe Email
    opens common to javafx.base;
    // -------------------------------------

    opens client.view to javafx.fxml;
    opens client.controller to javafx.fxml;
}
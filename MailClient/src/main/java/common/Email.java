package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Classe Data Transfer Object che rappresenta un messaggio di posta elettronica.
 * <p>
 * Tutti i campi sono final per thread-safety.
 */
public class Email implements Serializable {

    // Identificativo di versione per la serializzazione.
    private static final long serialVersionUID = 1L;

    // ID univoco per identificare la mail
    private final UUID id;

    private final String mittente;
    private final List<String> destinatari;
    private final String oggetto;
    private final String testo;
    private final LocalDateTime dataSpedizione;

    /**
     * Costruisce una nuova Email.
     * Assegna automaticamente un ID univoco e il timestamp di creazione.
     */
    public Email(String mittente, List<String> destinatari, String oggetto, String testo) {
        this.id = UUID.randomUUID();
        this.mittente = mittente;
        this.destinatari = destinatari;
        this.oggetto = oggetto;
        this.testo = testo;
        this.dataSpedizione = LocalDateTime.now();
    }

    // --- GETTERS ---

    public UUID getId() { return id; }
    public String getMittente() { return mittente; }
    public List<String> getDestinatari() { return destinatari; }
    public String getOggetto() { return oggetto; }
    public String getTesto() { return testo; }
    public LocalDateTime getDataSpedizione() { return dataSpedizione; }

    // --- Metodi di Utilità per la UI ---

    /**
     * Restituisce la data di spedizione formattata come stringa leggibile.
     */
    public String getDataFormattata() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dataSpedizione.format(formatter);
    }

    /**
     * Verifica l'uguaglianza tra due email basandosi esclusivamente sull'ID univoco.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return id.equals(email.id);
    }

    @Override
    public String toString() {
        return mittente + ": " + oggetto;
    }
}
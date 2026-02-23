package server.model;

import common.Email;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Gestore della Persistenza su file system locale.
 * <p>
 * Utilizza ReentrantReadWriteLock per permettere letture parallele (scalabilità)
 * mantenendo la sicurezza esclusiva per le scritture.
 */
public class FileManager {

    private static final String DIR_PATH = "mail-data/";

    // Creiamo il gestore dei Lock
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rLock = rwl.readLock();
    private final Lock wLock = rwl.writeLock();

    public FileManager() {
        File directory = new File(DIR_PATH);
        if (!directory.exists()) {
            boolean creata = directory.mkdir();
            if (creata) System.out.println("Cartella 'mail-data' creata correttamente.");
        }
    }

    /**
     * OPERAZIONE DI SCRITTURA (WriteLock)
     * Deve essere esclusiva: nessun altro può leggere o scrivere mentre salvo.
     */
    public void salvaEmail(String utente, Email email) {
        wLock.lock(); // CHIUDO IL LUCCHETTO
        try {
            List<Email> inbox = caricaEmail(utente);

            inbox.add(email);

            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(DIR_PATH + utente + ".dat"))) {
                out.writeObject(inbox);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Errore critico salvataggio mail per: " + utente);
            }
        } finally {
            wLock.unlock(); // APRO IL LUCCHETTO
        }
    }

    /**
     * OPERAZIONE DI LETTURA (ReadLock)
     * Più thread possono entrare qui contemporaneamente.
     */
    @SuppressWarnings("unchecked")
    public List<Email> caricaEmail(String utente) {
        rLock.lock(); // CHIUDO IL LUCCHETTO
        try {
            File file = new File(DIR_PATH + utente + ".dat");

            if (!file.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(file))) {
                return (List<Email>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        } finally {
            rLock.unlock(); // APRO IL LUCCHETTO
        }
    }

    /**
     * OPERAZIONE DI SCRITTURA (WriteLock)
     */
    public void cancellaEmail(String utente, Email emailDaRimuovere) {
        wLock.lock(); // CHIUDO IL LUCCHETTO
        try {
            List<Email> inbox = caricaEmail(utente);

            boolean removed = inbox.remove(emailDaRimuovere);

            if (removed) {
                try (ObjectOutputStream out = new ObjectOutputStream(
                        new FileOutputStream(DIR_PATH + utente + ".dat"))) {
                    out.writeObject(inbox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            wLock.unlock(); // APRO IL LUCCHETTO
        }
    }
}
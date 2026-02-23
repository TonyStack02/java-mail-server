Simulazione Server di Posta Elettronica
Progetto accademico realizzato per l'esame di Programmazione III presso l'Università degli Studi di Torino. Il sistema implementa un'architettura client-server per la gestione di un servizio di messaggistica elettronica.

Struttura del Progetto
Il repository è organizzato in due moduli principali che comunicano tramite rete:

/Server: Gestisce l'instradamento dei messaggi, la persistenza dei dati e la sincronizzazione dei log.

/Client: Applicazione desktop che permette agli utenti di inviare, ricevere e visualizzare le email in tempo reale.

Caratteristiche Tecniche
Networking: Comunicazione client-server basata su Java Sockets.
Concorrenza: Gestione di connessioni multiple simultanee tramite l'utilizzo di Multithreading.
Thread Safety: Implementazione di blocchi Synchronized per garantire l'integrità dei dati durante la persistenza dei log su file, evitando race condition tra i vari thread dei client.
Interfaccia Grafica: GUI desktop intuitiva realizzata con JavaFX.

Tech Stack
Linguaggio: Java.

Librerie GUI: JavaFX.


package it.gestionale.prenotazioni.application;

import it.gestionale.prenotazioni.domain.Cliente;
import it.gestionale.prenotazioni.domain.Prenotazione;
import it.gestionale.prenotazioni.domain.Risorsa;
import it.gestionale.prenotazioni.enums.StatoPrenotazione;
import it.gestionale.prenotazioni.enums.TipoRisorsa;
import it.gestionale.prenotazioni.exceptions.PrenotazioneException;
import it.gestionale.prenotazioni.repository.ClienteRepository;
import it.gestionale.prenotazioni.repository.PersistentClienteRepository;
import it.gestionale.prenotazioni.repository.PersistentPrenotazioneRepository;
import it.gestionale.prenotazioni.repository.PersistentRisorsaRepository;
import it.gestionale.prenotazioni.repository.RisorsaRepository;
import it.gestionale.prenotazioni.service.PrenotazioneService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Application {
    private PrenotazioneService service;
    private ClienteRepository clienteRepo;
    private RisorsaRepository risorsaRepo;
    private PersistentPrenotazioneRepository prenotazioneRepo;
    private Scanner scanner;

    private static final String CLIENTI_FILE = "clienti.txt";
    private static final String RISORSE_FILE = "risorse.txt";
    private static final String PRENOTAZIONI_FILE = "prenotazioni.txt";

    public Application() {
        this.clienteRepo = new PersistentClienteRepository();
        this.risorsaRepo = new PersistentRisorsaRepository();
        this.prenotazioneRepo = new PersistentPrenotazioneRepository();
        this.service = new PrenotazioneService(prenotazioneRepo, clienteRepo, risorsaRepo);
        this.scanner = new Scanner(System.in);

        // Carica i dati da file
        loadData();

        // Se non ci sono dati, crea dati di esempio
        if (clienteRepo.findAll().isEmpty()) {
            initDatiEsempio();
        }
    }

    private void loadData() {
        try {
            clienteRepo.loadFromFile(CLIENTI_FILE);
            risorsaRepo.loadFromFile(RISORSE_FILE);
            prenotazioneRepo.loadFromFile(PRENOTAZIONI_FILE, clienteRepo, risorsaRepo);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei dati: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            clienteRepo.saveToFile(CLIENTI_FILE);
            risorsaRepo.saveToFile(RISORSE_FILE);
            prenotazioneRepo.saveToFile(PRENOTAZIONI_FILE, clienteRepo, risorsaRepo);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio dei dati: " + e.getMessage());
        }
    }

    private void initDatiEsempio() {
        // Creazione di alcuni dati di esempio
        clienteRepo.save(new Cliente("Mario", "Rossi", "mario.rossi@email.it", "333-1234567"));
        clienteRepo.save(new Cliente("Laura", "Bianchi", "laura.bianchi@email.it", "333-7654321"));
        clienteRepo.save(new Cliente("Giuseppe", "Verdi", "g.verdi@email.it", "333-1122334"));

        risorsaRepo.save(new Risorsa("Sala Conferenze A", "Sala grande con proiettore", TipoRisorsa.SALA_CONFERENZE, 20));
        risorsaRepo.save(new Risorsa("Sala Conferenze B", "Sala piccola per riunioni", TipoRisorsa.SALA_CONFERENZE, 8));
        risorsaRepo.save(new Risorsa("Tavolo Vista", "Tavolo con vista panoramica", TipoRisorsa.TAVOLO_RISTORANTE, 4));
        risorsaRepo.save(new Risorsa("Campo Tennis 1", "Campo da tennis coperto", TipoRisorsa.CAMPO_SPORTIVO, 4));
        risorsaRepo.save(new Risorsa("Postazione Lavoro 1", "Postazione con PC e monitor", TipoRisorsa.POSTAZIONE_LAVORO, 1));

        // Creazione di alcune prenotazioni di esempio
        try {
            LocalDateTime domani10 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime dopodomani15 = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0);

            service.creaPrenotazione(1L, 1L, domani10, domani10.plusHours(2), "Riunione aziendale");
            service.creaPrenotazione(2L, 2L, dopodomani15, dopodomani15.plusHours(1), "Colloquio di lavoro");

            // Conferma una prenotazione
            service.confermaPrenotazione(1L);
        } catch (Exception e) {
            // Ignora errori in fase di inizializzazione
        }
    }

    public void start() {
        boolean running = true;

        while (running) {
            clearScreen();
            mostraIntestazione();

            try {
                int scelta = leggiIntero("\nScegli un'opzione: ", 0, 20);

                switch (scelta) {
                    case 1 -> creaCliente();
                    case 2 -> visualizzaClienti();
                    case 3 -> eliminaCliente();
                    case 4 -> creaRisorsa();
                    case 5 -> visualizzaRisorse();
                    case 6 -> eliminaRisorsa();
                    case 7 -> creaPrenotazione();
                    case 8 -> visualizzaTuttePrenotazioni();
                    case 9 -> visualizzaPrenotazioniCliente();
                    case 10 -> visualizzaPrenotazioniRisorsa();
                    case 11 -> visualizzaPrenotazioniStato();
                    case 12 -> confermaPrenotazione();
                    case 13 -> completaPrenotazione();
                    case 14 -> cancellaPrenotazione();
                    case 15 -> modificaPrenotazione();
                    case 16 -> verificaDisponibilita();
                    case 17 -> ricercaPrenotazioniPeriodo();
                    case 18 -> visualizzaDettaglioPrenotazione();
                    case 19 -> mostraStatistiche();
                    case 20 -> salvaDati();
                    case 0 -> {
                        saveData();
                        running = false;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("\n⚠ Inserisci un numero valido!");
                attesa();
            } catch (Exception e) {
                System.out.println("\n❌ Errore: " + e.getMessage());
                attesa();
            }
        }

        System.out.println("\nArrivederci! Dati salvati su file.");
        scanner.close();
    }

    private void mostraIntestazione() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              GESTIONALE PRENOTAZIONI - MENU PRINCIPALE         ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║ GESTIONE CLIENTI:                                              ║");
        System.out.println("║   1. Crea nuovo cliente                                        ║");
        System.out.println("║   2. Visualizza tutti i clienti                                ║");
        System.out.println("║   3. Elimina cliente                                           ║");
        System.out.println("║                                                                ║");
        System.out.println("║ GESTIONE RISORSE:                                              ║");
        System.out.println("║   4. Crea nuova risorsa                                        ║");
        System.out.println("║   5. Visualizza tutte le risorse                               ║");
        System.out.println("║   6. Elimina risorsa                                           ║");
        System.out.println("║                                                                ║");
        System.out.println("║ GESTIONE PRENOTAZIONI:                                         ║");
        System.out.println("║   7. Crea nuova prenotazione                                   ║");
        System.out.println("║   8. Visualizza tutte le prenotazioni                          ║");
        System.out.println("║   9. Visualizza prenotazioni cliente                           ║");
        System.out.println("║   10. Visualizza prenotazioni risorsa                          ║");
        System.out.println("║   11. Visualizza prenotazioni per stato                        ║");
        System.out.println("║   12. Conferma prenotazione                                    ║");
        System.out.println("║   13. Completa prenotazione                                    ║");
        System.out.println("║   14. Cancella prenotazione                                    ║");
        System.out.println("║   15. Modifica prenotazione                                    ║");
        System.out.println("║   16. Verifica disponibilità risorsa                           ║");
        System.out.println("║   17. Ricerca prenotazioni per periodo                         ║");
        System.out.println("║   18. Visualizza dettaglio prenotazione                        ║");
        System.out.println("║                                                                ║");
        System.out.println("║ REPORT E STATISTICHE:                                          ║");
        System.out.println("║   19. Statistiche sistema                                      ║");
        System.out.println("║                                                                ║");
        System.out.println("║ PERSISTENZA DATI:                                              ║");
        System.out.println("║   20. Salva dati manualmente                                   ║");
        System.out.println("║                                                                ║");
        System.out.println("║   0. Esci (salva automaticamente)                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void attesa() {
        System.out.print("\nPremi INVIO per continuare...");
        scanner.nextLine();
    }

    private int leggiIntero(String messaggio, int min, int max) {
        while (true) {
            System.out.print(messaggio);
            try {
                int valore = Integer.parseInt(scanner.nextLine());
                if (valore >= min && valore <= max) {
                    return valore;
                }
                System.out.println("⚠ Inserisci un numero tra " + min + " e " + max);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Inserisci un numero valido!");
            }
        }
    }

    private long leggiLong(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            try {
                String input = scanner.nextLine();
                if (input.isEmpty()) {
                    return -1;
                }
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Inserisci un numero valido!");
            }
        }
    }

    private void creaCliente() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          CREAZIONE NUOVO CLIENTE         ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Telefono: ");
        String telefono = scanner.nextLine();

        try {
            Cliente cliente = new Cliente(nome, cognome, email, telefono);
            clienteRepo.save(cliente);
            System.out.println("\n✅ Cliente creato con successo! ID: " + cliente.getId());
        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void creaRisorsa() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          CREAZIONE NUOVA RISORSA         ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Descrizione: ");
        String descrizione = scanner.nextLine();

        System.out.println("\nTipi disponibili:");
        for (int i = 0; i < TipoRisorsa.values().length; i++) {
            System.out.printf("%d. %s%n", i + 1, TipoRisorsa.values()[i].getDescrizione());
        }

        int sceltaTipo = leggiIntero("\nScelta tipo (1-" + TipoRisorsa.values().length + "): ", 1, TipoRisorsa.values().length);
        TipoRisorsa tipo = TipoRisorsa.values()[sceltaTipo - 1];

        int capacita = leggiIntero("Capacità: ", 1, 1000);

        try {
            Risorsa risorsa = new Risorsa(nome, descrizione, tipo, capacita);
            risorsaRepo.save(risorsa);
            System.out.println("\n✅ Risorsa creata con successo! ID: " + risorsa.getId());
        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void creaPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        CREAZIONE NUOVA PRENOTAZIONE      ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        // Mostra clienti disponibili
        List<Cliente> clienti = clienteRepo.findAll();
        if (clienti.isEmpty()) {
            System.out.println("❌ Nessun cliente disponibile. Crea prima un cliente.");
            attesa();
            return;
        }

        System.out.println("Clienti disponibili:");
        for (Cliente c : clienti) {
            System.out.println(c);
        }

        Long clienteId = leggiLong("\nID Cliente: ");
        if (clienteId == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        // Mostra risorse disponibili
        List<Risorsa> risorse = risorsaRepo.findAll();
        if (risorse.isEmpty()) {
            System.out.println("❌ Nessuna risorsa disponibile. Crea prima una risorsa.");
            attesa();
            return;
        }

        System.out.println("\nRisorse disponibili:");
        for (Risorsa r : risorse) {
            System.out.println(r);
        }

        Long risorsaId = leggiLong("\nID Risorsa: ");
        if (risorsaId == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            System.out.print("\nData e ora inizio (dd/MM/yyyy HH:mm): ");
            String inizioStr = scanner.nextLine();
            LocalDateTime inizio = LocalDateTime.parse(inizioStr, formatter);

            System.out.print("Data e ora fine (dd/MM/yyyy HH:mm): ");
            String fineStr = scanner.nextLine();
            LocalDateTime fine = LocalDateTime.parse(fineStr, formatter);

            System.out.print("Note (opzionale): ");
            String note = scanner.nextLine();

            Prenotazione prenotazione = service.creaPrenotazione(clienteId, risorsaId, inizio, fine, note);
            System.out.println("\n✅ Prenotazione creata con successo! ID: " + prenotazione.getId());
            System.out.println("\nDettagli prenotazione:");
            System.out.println(prenotazione.toStringDettagliato());

        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Formato data non valido! Usa dd/MM/yyyy HH:mm");
        } catch (PrenotazioneException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Errore generico: " + e.getMessage());
            e.printStackTrace();
        }

        attesa();
    }

    private void visualizzaClienti() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║           ELENCO DEI CLIENTI             ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        List<Cliente> clienti = clienteRepo.findAll();

        if (clienti.isEmpty()) {
            System.out.println("Nessun cliente presente nel sistema.");
        } else {
            System.out.println("Totale clienti: " + clienti.size() + "\n");
            System.out.println("=".repeat(100));
            for (Cliente c : clienti) {
                System.out.println(c);
            }
            System.out.println("=".repeat(100));
        }

        attesa();
    }

    private void visualizzaRisorse() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║           ELENCO DELLE RISORSE           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        List<Risorsa> risorse = risorsaRepo.findAll();

        if (risorse.isEmpty()) {
            System.out.println("Nessuna risorsa presente nel sistema.");
        } else {
            System.out.println("Totale risorse: " + risorse.size() + "\n");
            System.out.println("=".repeat(120));
            for (Risorsa r : risorse) {
                System.out.println(r);
            }
            System.out.println("=".repeat(120));
        }

        attesa();
    }

    private void visualizzaTuttePrenotazioni() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         TUTTE LE PRENOTAZIONI            ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        List<Prenotazione> prenotazioni = service.getTuttePrenotazioni();

        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione presente nel sistema.");
        } else {
            System.out.println("Totale prenotazioni: " + prenotazioni.size() + "\n");
            System.out.println("=".repeat(130));
            for (Prenotazione p : prenotazioni) {
                System.out.println(p);
            }
            System.out.println("=".repeat(130));
        }

        attesa();
    }

    private void visualizzaPrenotazioniCliente() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     PRENOTAZIONI PER CLIENTE             ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long clienteId = leggiLong("ID Cliente (lascia vuoto per annullare): ");
        if (clienteId == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            List<Prenotazione> prenotazioni = service.getPrenotazioniCliente(clienteId);

            if (prenotazioni.isEmpty()) {
                System.out.println("\nNessuna prenotazione per questo cliente.");
            } else {
                System.out.println("\nTotale prenotazioni: " + prenotazioni.size() + "\n");
                System.out.println("=".repeat(130));
                for (Prenotazione p : prenotazioni) {
                    System.out.println(p);
                }
                System.out.println("=".repeat(130));
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void visualizzaPrenotazioniRisorsa() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     PRENOTAZIONI PER RISORSA             ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long risorsaId = leggiLong("ID Risorsa (lascia vuoto per annullare): ");
        if (risorsaId == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            List<Prenotazione> prenotazioni = service.getPrenotazioniRisorsa(risorsaId);

            if (prenotazioni.isEmpty()) {
                System.out.println("\nNessuna prenotazione per questa risorsa.");
            } else {
                System.out.println("\nTotale prenotazioni: " + prenotazioni.size() + "\n");
                System.out.println("=".repeat(130));
                for (Prenotazione p : prenotazioni) {
                    System.out.println(p);
                }
                System.out.println("=".repeat(130));
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void visualizzaPrenotazioniStato() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     PRENOTAZIONI PER STATO               ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        System.out.println("Stati disponibili:");
        for (int i = 0; i < StatoPrenotazione.values().length; i++) {
            System.out.printf("%d. %s%n", i + 1, StatoPrenotazione.values()[i].getDescrizione());
        }

        int scelta = leggiIntero("\nScelta stato (1-" + StatoPrenotazione.values().length + "): ", 1, StatoPrenotazione.values().length);
        StatoPrenotazione stato = StatoPrenotazione.values()[scelta - 1];

        try {
            List<Prenotazione> prenotazioni = service.getPrenotazioniStato(stato);

            if (prenotazioni.isEmpty()) {
                System.out.println("\nNessuna prenotazione con stato: " + stato.getDescrizione());
            } else {
                System.out.println("\nPrenotazioni con stato " + stato.getDescrizione() + ": " + prenotazioni.size() + "\n");
                System.out.println("=".repeat(130));
                for (Prenotazione p : prenotazioni) {
                    System.out.println(p);
                }
                System.out.println("=".repeat(130));
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void confermaPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       CONFERMA PRENOTAZIONE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Prenotazione da confermare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            service.confermaPrenotazione(id);
            System.out.println("\n✅ Prenotazione confermata con successo!");
        } catch (PrenotazioneException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void completaPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       COMPLETA PRENOTAZIONE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Prenotazione da completare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            service.completaPrenotazione(id);
            System.out.println("\n✅ Prenotazione completata con successo!");
        } catch (PrenotazioneException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void cancellaPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       CANCELLA PRENOTAZIONE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Prenotazione da cancellare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            service.cancellaPrenotazione(id);
            System.out.println("\n✅ Prenotazione cancellata con successo!");
        } catch (PrenotazioneException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void modificaPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       MODIFICA PRENOTAZIONE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Prenotazione da modificare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            System.out.print("\nNuova data e ora inizio (dd/MM/yyyy HH:mm): ");
            String inizioStr = scanner.nextLine();
            LocalDateTime nuovoInizio = LocalDateTime.parse(inizioStr, formatter);

            System.out.print("Nuova data e ora fine (dd/MM/yyyy HH:mm): ");
            String fineStr = scanner.nextLine();
            LocalDateTime nuovaFine = LocalDateTime.parse(fineStr, formatter);

            System.out.print("Nuove note (opzionale, lascia vuoto per non modificare): ");
            String note = scanner.nextLine();
            if (note.isEmpty()) {
                note = null;
            }

            Prenotazione p = service.modificaPrenotazione(id, nuovoInizio, nuovaFine, note);
            System.out.println("\n✅ Prenotazione modificata con successo!");
            System.out.println("\nNuovi dettagli prenotazione:");
            System.out.println(p.toStringDettagliato());

        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Formato data non valido! Usa dd/MM/yyyy HH:mm");
        } catch (PrenotazioneException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Errore generico: " + e.getMessage());
        }

        attesa();
    }

    private void verificaDisponibilita() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      VERIFICA DISPONIBILITÀ              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long risorsaId = leggiLong("ID Risorsa: ");
        if (risorsaId == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            System.out.print("\nData e ora inizio (dd/MM/yyyy HH:mm): ");
            String inizioStr = scanner.nextLine();
            LocalDateTime inizio = LocalDateTime.parse(inizioStr, formatter);

            System.out.print("Data e ora fine (dd/MM/yyyy HH:mm): ");
            String fineStr = scanner.nextLine();
            LocalDateTime fine = LocalDateTime.parse(fineStr, formatter);

            boolean disponibile = service.isRisorsaDisponibile(risorsaId, inizio, fine);

            if (disponibile) {
                System.out.println("\n✅ La risorsa è DISPONIBILE nel periodo specificato!");
            } else {
                System.out.println("\n❌ La risorsa NON è disponibile nel periodo specificato!");
                System.out.println("\nPrenotazioni esistenti nel periodo:");
                List<Prenotazione> conflitti = service.getPrenotazioniRisorsa(risorsaId).stream()
                        .filter(p -> p.getStato() != StatoPrenotazione.CANCELLATA)
                        .filter(p -> p.getDataOraInizio().isBefore(fine) && p.getDataOraFine().isAfter(inizio))
                        .collect(Collectors.toList());

                if (!conflitti.isEmpty()) {
                    for (Prenotazione p : conflitti) {
                        System.out.println("  - " + p);
                    }
                }
            }

        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Formato data non valido! Usa dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void ricercaPrenotazioniPeriodo() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    RICERCA PRENOTAZIONI PER PERIODO      ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            System.out.print("Data inizio (dd/MM/yyyy HH:mm): ");
            String inizioStr = scanner.nextLine();
            LocalDateTime inizio = LocalDateTime.parse(inizioStr, formatter);

            System.out.print("Data fine (dd/MM/yyyy HH:mm): ");
            String fineStr = scanner.nextLine();
            LocalDateTime fine = LocalDateTime.parse(fineStr, formatter);

            List<Prenotazione> prenotazioni = service.getPrenotazioniPeriodo(inizio, fine);

            if (prenotazioni.isEmpty()) {
                System.out.println("\nNessuna prenotazione nel periodo specificato.");
            } else {
                System.out.println("\nPrenotazioni trovate: " + prenotazioni.size() + "\n");
                System.out.println("=".repeat(130));
                for (Prenotazione p : prenotazioni) {
                    System.out.println(p);
                }
                System.out.println("=".repeat(130));
            }

        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Formato data non valido! Usa dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void visualizzaDettaglioPrenotazione() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      DETTAGLIO PRENOTAZIONE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Prenotazione: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            List<Prenotazione> tutte = service.getTuttePrenotazioni();
            Optional<Prenotazione> prenotazioneOpt = tutte.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst();

            if (prenotazioneOpt.isPresent()) {
                Prenotazione p = prenotazioneOpt.get();
                System.out.println("\n" + p.toStringDettagliato());
            } else {
                System.out.println("\n❌ Prenotazione non trovata!");
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void eliminaCliente() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          ELIMINA CLIENTE                 ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Cliente da eliminare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            // Verifica se il cliente ha prenotazioni attive
            List<Prenotazione> prenotazioniCliente = service.getPrenotazioniCliente(id);
            boolean haPrenotazioniAttive = prenotazioniCliente.stream()
                    .anyMatch(p -> p.getStato() != StatoPrenotazione.CANCELLATA &&
                            p.getStato() != StatoPrenotazione.COMPLETATA);

            if (haPrenotazioniAttive) {
                System.out.println("\n⚠ ATTENZIONE: Il cliente ha prenotazioni attive!");
                System.out.print("Confermi l'eliminazione? (s/n): ");
                String conferma = scanner.nextLine();

                if (!conferma.equalsIgnoreCase("s")) {
                    System.out.println("Eliminazione annullata.");
                    attesa();
                    return;
                }
            }

            boolean eliminato = clienteRepo.delete(id);

            if (eliminato) {
                System.out.println("\n✅ Cliente eliminato con successo!");
            } else {
                System.out.println("\n❌ Cliente non trovato!");
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void eliminaRisorsa() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          ELIMINA RISORSA                 ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Long id = leggiLong("ID Risorsa da eliminare: ");
        if (id == -1) {
            System.out.println("Operazione annullata.");
            attesa();
            return;
        }

        try {
            // Verifica se la risorsa ha prenotazioni future
            List<Prenotazione> prenotazioniRisorsa = service.getPrenotazioniRisorsa(id);
            boolean haPrenotazioniFuture = prenotazioniRisorsa.stream()
                    .anyMatch(p -> p.getStato() != StatoPrenotazione.CANCELLATA &&
                            p.getDataOraInizio().isAfter(LocalDateTime.now()));

            if (haPrenotazioniFuture) {
                System.out.println("\n⚠ ATTENZIONE: La risorsa ha prenotazioni future!");
                System.out.print("Confermi l'eliminazione? (s/n): ");
                String conferma = scanner.nextLine();

                if (!conferma.equalsIgnoreCase("s")) {
                    System.out.println("Eliminazione annullata.");
                    attesa();
                    return;
                }
            }

            boolean eliminata = risorsaRepo.delete(id);

            if (eliminata) {
                System.out.println("\n✅ Risorsa eliminata con successo!");
            } else {
                System.out.println("\n❌ Risorsa non trovata!");
            }

        } catch (Exception e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
        }

        attesa();
    }

    private void salvaDati() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          SALVATAGGIO DATI                ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        saveData();
        System.out.println("\n✅ Dati salvati con successo!");
        attesa();
    }

    private void mostraStatistiche() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          STATISTICHE SISTEMA             ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        List<Cliente> clienti = clienteRepo.findAll();
        List<Risorsa> risorse = risorsaRepo.findAll();
        List<Prenotazione> prenotazioni = service.getTuttePrenotazioni();

        System.out.println("📊 STATISTICHE GENERALI:");
        System.out.println("├─ Clienti registrati: " + clienti.size());
        System.out.println("├─ Risorse disponibili: " + risorse.size());
        System.out.println("└─ Prenotazioni totali: " + prenotazioni.size());

        System.out.println("\n📈 DISTRIBUZIONE PRENOTAZIONI PER STATO:");
        Map<StatoPrenotazione, Long> prenotazioniPerStato = prenotazioni.stream()
                .collect(Collectors.groupingBy(Prenotazione::getStato, Collectors.counting()));

        prenotazioniPerStato.forEach((stato, count) ->
                System.out.printf("├─ %-12s: %-3d (%.1f%%)\n",
                        stato.getDescrizione(), count,
                        prenotazioni.isEmpty() ? 0 : (count * 100.0 / prenotazioni.size())));

        System.out.println("\n🏢 RISORSE PER TIPO:");
        Map<TipoRisorsa, Long> risorsePerTipo = risorse.stream()
                .collect(Collectors.groupingBy(Risorsa::getTipo, Collectors.counting()));

        risorsePerTipo.forEach((tipo, count) ->
                System.out.println("├─ " + tipo.getDescrizione() + ": " + count));

        System.out.println("\n📅 PRENOTAZIONI PROSSIME (prossimi 7 giorni):");
        LocalDateTime ora = LocalDateTime.now();
        LocalDateTime tra7Giorni = ora.plusDays(7);

        long prenotazioniFuture = prenotazioni.stream()
                .filter(p -> p.getStato() != StatoPrenotazione.CANCELLATA)
                .filter(p -> p.getDataOraInizio().isAfter(ora) && p.getDataOraInizio().isBefore(tra7Giorni))
                .count();

        System.out.println("└─ " + prenotazioniFuture + " prenotazioni nei prossimi 7 giorni");

        System.out.println("\n💾 FILE DI DATI:");
        System.out.println("├─ " + CLIENTI_FILE + " (clienti)");
        System.out.println("├─ " + RISORSE_FILE + " (risorse)");
        System.out.println("└─ " + PRENOTAZIONI_FILE + " (prenotazioni)");

        attesa();
    }
}

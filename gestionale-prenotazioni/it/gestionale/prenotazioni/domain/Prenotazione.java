package it.gestionale.prenotazioni.domain;

import it.gestionale.prenotazioni.enums.StatoPrenotazione;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Prenotazione {
    private Long id;
    private Cliente cliente;
    private Risorsa risorsa;
    private LocalDateTime dataOraInizio;
    private LocalDateTime dataOraFine;
    private StatoPrenotazione stato;
    private String note;

    public Prenotazione() {}

    public Prenotazione(Long id, Cliente cliente, Risorsa risorsa, LocalDateTime dataOraInizio,
                        LocalDateTime dataOraFine, StatoPrenotazione stato, String note) {
        this.id = id;
        this.cliente = cliente;
        this.risorsa = risorsa;
        this.dataOraInizio = dataOraInizio;
        this.dataOraFine = dataOraFine;
        this.stato = stato;
        this.note = note;
    }

    public Prenotazione(Cliente cliente, Risorsa risorsa, LocalDateTime dataOraInizio, LocalDateTime dataOraFine) {
        this(null, cliente, risorsa, dataOraInizio, dataOraFine, StatoPrenotazione.PROVVISORIA, null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Risorsa getRisorsa() { return risorsa; }
    public void setRisorsa(Risorsa risorsa) { this.risorsa = risorsa; }
    public LocalDateTime getDataOraInizio() { return dataOraInizio; }
    public void setDataOraInizio(LocalDateTime dataOraInizio) { this.dataOraInizio = dataOraInizio; }
    public LocalDateTime getDataOraFine() { return dataOraFine; }
    public void setDataOraFine(LocalDateTime dataOraFine) { this.dataOraFine = dataOraFine; }
    public StatoPrenotazione getStato() { return stato; }
    public void setStato(StatoPrenotazione stato) { this.stato = stato; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("ID: %-3d | Cliente: %-15s %-15s | Risorsa: %-20s | Inizio: %-16s | Fine: %-16s | Stato: %-12s",
                id,
                cliente != null ? cliente.getNome() : "null",
                cliente != null ? cliente.getCognome() : "null",
                risorsa != null ? risorsa.getNome() : "null",
                dataOraInizio.format(formatter),
                dataOraFine.format(formatter),
                stato.getDescrizione());
    }

    public String toStringDettagliato() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "PRENOTAZIONE ID: " + id + "\n" +
                "=======================\n" +
                "Cliente: " + (cliente != null ? cliente.toStringBreve() : "null") + "\n" +
                "Risorsa: " + (risorsa != null ? risorsa.toStringBreve() : "null") + "\n" +
                "Periodo: " + dataOraInizio.format(formatter) + " - " + dataOraFine.format(formatter) + "\n" +
                "Stato: " + stato.getDescrizione() + "\n" +
                "Note: " + (note != null ? note : "Nessuna") + "\n";
    }
}

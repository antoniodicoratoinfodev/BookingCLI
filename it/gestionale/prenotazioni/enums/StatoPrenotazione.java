package it.gestionale.prenotazioni.enums;

public enum StatoPrenotazione {
    PROVVISORIA("Provvisoria"),
    CONFERMATA("Confermata"),
    COMPLETATA("Completata"),
    CANCELLATA("Cancellata");

    private final String descrizione;

    StatoPrenotazione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}

package it.gestionale.prenotazioni.enums;

public enum TipoRisorsa {
    SALA_CONFERENZE("Sala Conferenze"),
    TAVOLO_RISTORANTE("Tavolo Ristorante"),
    POSTAZIONE_LAVORO("Postazione Lavoro"),
    CAMPO_SPORTIVO("Campo Sportivo"),
    SERVIZIO("Servizio");

    private final String descrizione;

    TipoRisorsa(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}

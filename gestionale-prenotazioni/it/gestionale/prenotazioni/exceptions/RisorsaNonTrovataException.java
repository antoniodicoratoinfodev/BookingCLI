package it.gestionale.prenotazioni.exceptions;

public class RisorsaNonTrovataException extends PrenotazioneException {
    public RisorsaNonTrovataException(Long id) {
        super("Risorsa con ID " + id + " non trovata");
    }
}

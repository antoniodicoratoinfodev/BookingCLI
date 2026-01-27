package it.gestionale.prenotazioni.exceptions;

public class PrenotazioneNonTrovataException extends PrenotazioneException {
    public PrenotazioneNonTrovataException(Long id) {
        super("Prenotazione con ID " + id + " non trovata");
    }
}

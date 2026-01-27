package it.gestionale.prenotazioni.exceptions;

public class PrenotazioneException extends RuntimeException {
    public PrenotazioneException(String messaggio) {
        super(messaggio);
    }
}

package it.gestionale.prenotazioni.exceptions;

public class ClienteNonTrovatoException extends PrenotazioneException {
    public ClienteNonTrovatoException(Long id) {
        super("Cliente con ID " + id + " non trovato");
    }
}

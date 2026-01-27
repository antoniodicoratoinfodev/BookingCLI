package it.gestionale.prenotazioni.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RisorsaNonDisponibileException extends PrenotazioneException {
    public RisorsaNonDisponibileException(Long risorsaId, LocalDateTime inizio, LocalDateTime fine) {
        super(String.format("Risorsa ID %d non disponibile tra %s e %s", risorsaId,
                inizio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                fine.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
    }
}

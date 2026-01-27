package it.gestionale.prenotazioni.service;

import it.gestionale.prenotazioni.domain.Cliente;
import it.gestionale.prenotazioni.domain.Prenotazione;
import it.gestionale.prenotazioni.domain.Risorsa;
import it.gestionale.prenotazioni.enums.StatoPrenotazione;
import it.gestionale.prenotazioni.exceptions.*;
import it.gestionale.prenotazioni.repository.ClienteRepository;
import it.gestionale.prenotazioni.repository.PrenotazioneRepository;
import it.gestionale.prenotazioni.repository.RisorsaRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PrenotazioneService {
    private final PrenotazioneRepository prenotazioneRepository;
    private final ClienteRepository clienteRepository;
    private final RisorsaRepository risorsaRepository;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                               ClienteRepository clienteRepository,
                               RisorsaRepository risorsaRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.clienteRepository = clienteRepository;
        this.risorsaRepository = risorsaRepository;
    }

    public Prenotazione creaPrenotazione(Long clienteId, Long risorsaId,
                                         LocalDateTime inizio, LocalDateTime fine,
                                         String note) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNonTrovatoException(clienteId));

        Risorsa risorsa = risorsaRepository.findById(risorsaId)
                .orElseThrow(() -> new RisorsaNonTrovataException(risorsaId));

        if (!fine.isAfter(inizio)) {
            throw new PrenotazioneNonValidaException("La data/ora di fine deve essere successiva a quella di inizio");
        }

        if (inizio.isBefore(LocalDateTime.now())) {
            throw new PrenotazioneNonValidaException("Non è possibile creare prenotazioni nel passato");
        }

        if (Duration.between(inizio, fine).toHours() > 24) {
            throw new PrenotazioneNonValidaException("La prenotazione non può superare le 24 ore");
        }

        if (!isRisorsaDisponibile(risorsaId, inizio, fine)) {
            throw new RisorsaNonDisponibileException(risorsaId, inizio, fine);
        }

        Prenotazione prenotazione = new Prenotazione(cliente, risorsa, inizio, fine);
        prenotazione.setNote(note);
        prenotazione.setStato(StatoPrenotazione.PROVVISORIA);

        return prenotazioneRepository.save(prenotazione);
    }

    public boolean isRisorsaDisponibile(Long risorsaId, LocalDateTime inizio, LocalDateTime fine) {
        List<Prenotazione> prenotazioniEsistenti = prenotazioneRepository.findByRisorsa(risorsaId);

        return prenotazioniEsistenti.stream()
                .filter(p -> p.getStato() != StatoPrenotazione.CANCELLATA)
                .noneMatch(p -> intervalliSovrapposti(p.getDataOraInizio(), p.getDataOraFine(), inizio, fine));
    }

    private boolean intervalliSovrapposti(LocalDateTime inizio1, LocalDateTime fine1,
                                          LocalDateTime inizio2, LocalDateTime fine2) {
        return inizio1.isBefore(fine2) && fine1.isAfter(inizio2);
    }

    public List<Prenotazione> getPrenotazioniAttivePerRisorsa(Long risorsaId, LocalDateTime inizio, LocalDateTime fine) {
        return prenotazioneRepository.findByRisorsa(risorsaId).stream()
                .filter(p -> p.getStato() != StatoPrenotazione.CANCELLATA)
                .filter(p -> p.getDataOraInizio().isBefore(fine) && p.getDataOraFine().isAfter(inizio))
                .collect(Collectors.toList());
    }

    public void confermaPrenotazione(Long id) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new PrenotazioneNonTrovataException(id));

        if (prenotazione.getStato() != StatoPrenotazione.PROVVISORIA) {
            throw new OperazioneNonPermessaException("Solo le prenotazioni provvisorie possono essere confermate");
        }

        prenotazione.setStato(StatoPrenotazione.CONFERMATA);
        prenotazioneRepository.save(prenotazione);
    }

    public void completaPrenotazione(Long id) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new PrenotazioneNonTrovataException(id));

        if (prenotazione.getStato() == StatoPrenotazione.CANCELLATA) {
            throw new OperazioneNonPermessaException("Non è possibile completare una prenotazione cancellata");
        }

        if (prenotazione.getStato() == StatoPrenotazione.COMPLETATA) {
            throw new OperazioneNonPermessaException("La prenotazione è già stata completata");
        }

        prenotazione.setStato(StatoPrenotazione.COMPLETATA);
        prenotazioneRepository.save(prenotazione);
    }

    public void cancellaPrenotazione(Long id) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new PrenotazioneNonTrovataException(id));

        if (prenotazione.getStato() == StatoPrenotazione.COMPLETATA) {
            throw new OperazioneNonPermessaException("Non è possibile cancellare una prenotazione già completata");
        }

        prenotazione.setStato(StatoPrenotazione.CANCELLATA);
        prenotazioneRepository.save(prenotazione);
    }

    public Prenotazione modificaPrenotazione(Long id, LocalDateTime nuovoInizio,
                                             LocalDateTime nuovaFine, String note) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new PrenotazioneNonTrovataException(id));

        if (prenotazione.getStato() == StatoPrenotazione.COMPLETATA ||
                prenotazione.getStato() == StatoPrenotazione.CANCELLATA) {
            throw new OperazioneNonPermessaException("Non è possibile modificare una prenotazione " + prenotazione.getStato().getDescrizione());
        }

        if (!prenotazione.getDataOraInizio().equals(nuovoInizio) ||
                !prenotazione.getDataOraFine().equals(nuovaFine)) {

            if (!nuovaFine.isAfter(nuovoInizio)) {
                throw new PrenotazioneNonValidaException("La data/ora di fine deve essere successiva a quella di inizio");
            }

            boolean disponibile = prenotazioneRepository.findByRisorsa(prenotazione.getRisorsa().getId())
                    .stream()
                    .filter(p -> !p.getId().equals(id))
                    .filter(p -> p.getStato() != StatoPrenotazione.CANCELLATA)
                    .noneMatch(p -> intervalliSovrapposti(p.getDataOraInizio(), p.getDataOraFine(), nuovoInizio, nuovaFine));

            if (!disponibile) {
                throw new RisorsaNonDisponibileException(prenotazione.getRisorsa().getId(), nuovoInizio, nuovaFine);
            }

            prenotazione.setDataOraInizio(nuovoInizio);
            prenotazione.setDataOraFine(nuovaFine);
        }

        if (note != null) {
            prenotazione.setNote(note);
        }

        return prenotazioneRepository.save(prenotazione);
    }

    public List<Prenotazione> getPrenotazioniCliente(Long clienteId) {
        return prenotazioneRepository.findByCliente(clienteId);
    }

    public List<Prenotazione> getPrenotazioniRisorsa(Long risorsaId) {
        return prenotazioneRepository.findByRisorsa(risorsaId);
    }

    public List<Prenotazione> getPrenotazioniPeriodo(LocalDateTime inizio, LocalDateTime fine) {
        return prenotazioneRepository.findByPeriodo(inizio, fine);
    }

    public List<Prenotazione> getPrenotazioniStato(StatoPrenotazione stato) {
        return prenotazioneRepository.findByStato(stato);
    }

    public List<Prenotazione> getTuttePrenotazioni() {
        return prenotazioneRepository.findAll();
    }
}

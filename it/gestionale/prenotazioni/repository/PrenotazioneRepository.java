package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Prenotazione;
import it.gestionale.prenotazioni.enums.StatoPrenotazione;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrenotazioneRepository {
    Prenotazione save(Prenotazione prenotazione);
    Optional<Prenotazione> findById(Long id);
    List<Prenotazione> findAll();
    List<Prenotazione> findByCliente(Long clienteId);
    List<Prenotazione> findByRisorsa(Long risorsaId);
    List<Prenotazione> findByPeriodo(LocalDateTime inizio, LocalDateTime fine);
    List<Prenotazione> findByStato(StatoPrenotazione stato);
    boolean delete(Long id);
    void saveToFile(String filename, ClienteRepository clienteRepo, RisorsaRepository risorsaRepo);
    void loadFromFile(String filename, ClienteRepository clienteRepo, RisorsaRepository risorsaRepo);
}

package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Risorsa;
import it.gestionale.prenotazioni.enums.TipoRisorsa;
import java.util.List;
import java.util.Optional;

public interface RisorsaRepository {
    Risorsa save(Risorsa risorsa);
    Optional<Risorsa> findById(Long id);
    List<Risorsa> findAll();
    List<Risorsa> findByTipo(TipoRisorsa tipo);
    boolean delete(Long id);
    void saveToFile(String filename);
    void loadFromFile(String filename);
}

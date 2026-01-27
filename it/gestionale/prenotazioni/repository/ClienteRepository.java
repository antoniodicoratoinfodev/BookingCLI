package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(Long id);
    List<Cliente> findAll();
    List<Cliente> findByEmail(String email);
    boolean delete(Long id);
    void saveToFile(String filename);
    void loadFromFile(String filename);
}

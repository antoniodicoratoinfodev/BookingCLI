package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Cliente;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PersistentClienteRepository implements ClienteRepository {
    private final Map<Long, Cliente> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(idGenerator.getAndIncrement());
        } else {
            if (cliente.getId() >= idGenerator.get()) {
                idGenerator.set(cliente.getId() + 1);
            }
        }
        storage.put(cliente.getId(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Cliente> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Cliente> findByEmail(String email) {
        return storage.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("id,nome,cognome,email,telefono\n");
            for (Cliente cliente : storage.values()) {
                writer.write(String.format("%d,%s,%s,%s,%s\n",
                        cliente.getId(),
                        escapeCsv(cliente.getNome()),
                        escapeCsv(cliente.getCognome()),
                        escapeCsv(cliente.getEmail()),
                        escapeCsv(cliente.getTelefono())));
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei clienti: " + e.getMessage());
        }
    }

    @Override
    public void loadFromFile(String filename) {
        try {
            if (!Files.exists(Paths.get(filename))) {
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(filename));
            if (lines.isEmpty() || lines.size() == 1) return;

            storage.clear();
            long maxId = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    try {
                        Long id = Long.parseLong(parts[0]);
                        String nome = unescapeCsv(parts[1]);
                        String cognome = unescapeCsv(parts[2]);
                        String email = unescapeCsv(parts[3]);
                        String telefono = unescapeCsv(parts[4]);

                        Cliente cliente = new Cliente(id, nome, cognome, email, telefono);
                        storage.put(id, cliente);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Errore nel formato ID alla riga " + (i+1) + ": " + line);
                    }
                }
            }

            idGenerator.set(maxId + 1);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei clienti: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }
}

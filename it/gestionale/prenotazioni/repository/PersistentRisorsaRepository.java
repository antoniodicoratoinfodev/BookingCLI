package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Risorsa;
import it.gestionale.prenotazioni.enums.TipoRisorsa;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PersistentRisorsaRepository implements RisorsaRepository {
    private final Map<Long, Risorsa> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Risorsa save(Risorsa risorsa) {
        if (risorsa.getId() == null) {
            risorsa.setId(idGenerator.getAndIncrement());
        } else {
            if (risorsa.getId() >= idGenerator.get()) {
                idGenerator.set(risorsa.getId() + 1);
            }
        }
        storage.put(risorsa.getId(), risorsa);
        return risorsa;
    }

    @Override
    public Optional<Risorsa> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Risorsa> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Risorsa> findByTipo(TipoRisorsa tipo) {
        return storage.values().stream()
                .filter(r -> r.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("id,nome,descrizione,tipo,capacita\n");
            for (Risorsa risorsa : storage.values()) {
                writer.write(String.format("%d,%s,%s,%s,%d\n",
                        risorsa.getId(),
                        escapeCsv(risorsa.getNome()),
                        escapeCsv(risorsa.getDescrizione()),
                        escapeCsv(risorsa.getTipo().name()),
                        risorsa.getCapacita()));
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle risorse: " + e.getMessage());
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
                        String descrizione = unescapeCsv(parts[2]);
                        TipoRisorsa tipo = TipoRisorsa.valueOf(unescapeCsv(parts[3]));
                        int capacita = Integer.parseInt(parts[4]);

                        Risorsa risorsa = new Risorsa(id, nome, descrizione, tipo, capacita);
                        storage.put(id, risorsa);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Errore nel formato numerico alla riga " + (i+1) + ": " + line);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Errore nel formato del tipo risorsa alla riga " + (i+1) + ": " + line);
                    }
                }
            }

            idGenerator.set(maxId + 1);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle risorse: " + e.getMessage());
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

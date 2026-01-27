package it.gestionale.prenotazioni.repository;

import it.gestionale.prenotazioni.domain.Cliente;
import it.gestionale.prenotazioni.domain.Prenotazione;
import it.gestionale.prenotazioni.domain.Risorsa;
import it.gestionale.prenotazioni.enums.StatoPrenotazione;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PersistentPrenotazioneRepository implements PrenotazioneRepository {
    private final Map<Long, Prenotazione> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Prenotazione save(Prenotazione prenotazione) {
        if (prenotazione.getId() == null) {
            prenotazione.setId(idGenerator.getAndIncrement());
        } else {
            if (prenotazione.getId() >= idGenerator.get()) {
                idGenerator.set(prenotazione.getId() + 1);
            }
        }
        storage.put(prenotazione.getId(), prenotazione);
        return prenotazione;
    }

    @Override
    public Optional<Prenotazione> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Prenotazione> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Prenotazione> findByCliente(Long clienteId) {
        return storage.values().stream()
                .filter(p -> p.getCliente() != null && p.getCliente().getId().equals(clienteId))
                .sorted(Comparator.comparing(Prenotazione::getDataOraInizio))
                .collect(Collectors.toList());
    }

    @Override
    public List<Prenotazione> findByRisorsa(Long risorsaId) {
        return storage.values().stream()
                .filter(p -> p.getRisorsa() != null && p.getRisorsa().getId().equals(risorsaId))
                .sorted(Comparator.comparing(Prenotazione::getDataOraInizio))
                .collect(Collectors.toList());
    }

    @Override
    public List<Prenotazione> findByPeriodo(LocalDateTime inizio, LocalDateTime fine) {
        return storage.values().stream()
                .filter(p -> p.getDataOraInizio().isBefore(fine) && p.getDataOraFine().isAfter(inizio))
                .sorted(Comparator.comparing(Prenotazione::getDataOraInizio))
                .collect(Collectors.toList());
    }

    @Override
    public List<Prenotazione> findByStato(StatoPrenotazione stato) {
        return storage.values().stream()
                .filter(p -> p.getStato() == stato)
                .sorted(Comparator.comparing(Prenotazione::getDataOraInizio))
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public void saveToFile(String filename, ClienteRepository clienteRepo, RisorsaRepository risorsaRepo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("id,clienteId,risorsaId,dataOraInizio,dataOraFine,stato,note\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Prenotazione prenotazione : storage.values()) {
                String clienteId = prenotazione.getCliente() != null ? prenotazione.getCliente().getId().toString() : "";
                String risorsaId = prenotazione.getRisorsa() != null ? prenotazione.getRisorsa().getId().toString() : "";

                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                        prenotazione.getId(),
                        clienteId,
                        risorsaId,
                        escapeCsv(prenotazione.getDataOraInizio().format(formatter)),
                        escapeCsv(prenotazione.getDataOraFine().format(formatter)),
                        escapeCsv(prenotazione.getStato().name()),
                        escapeCsv(prenotazione.getNote())));
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle prenotazioni: " + e.getMessage());
        }
    }

    @Override
    public void loadFromFile(String filename, ClienteRepository clienteRepo, RisorsaRepository risorsaRepo) {
        try {
            if (!Files.exists(Paths.get(filename))) {
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(filename));
            if (lines.isEmpty() || lines.size() == 1) return;

            storage.clear();
            long maxId = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    try {
                        Long id = Long.parseLong(parts[0]);
                        String clienteIdStr = unescapeCsv(parts[1]);
                        String risorsaIdStr = unescapeCsv(parts[2]);
                        LocalDateTime dataOraInizio = LocalDateTime.parse(unescapeCsv(parts[3]), formatter);
                        LocalDateTime dataOraFine = LocalDateTime.parse(unescapeCsv(parts[4]), formatter);
                        StatoPrenotazione stato = StatoPrenotazione.valueOf(unescapeCsv(parts[5]));
                        String note = unescapeCsv(parts[6]);

                        Cliente cliente = null;
                        Risorsa risorsa = null;

                        if (!clienteIdStr.isEmpty()) {
                            Long clienteId = Long.parseLong(clienteIdStr);
                            cliente = clienteRepo.findById(clienteId).orElse(null);
                        }

                        if (!risorsaIdStr.isEmpty()) {
                            Long risorsaId = Long.parseLong(risorsaIdStr);
                            risorsa = risorsaRepo.findById(risorsaId).orElse(null);
                        }

                        Prenotazione prenotazione = new Prenotazione(id, cliente, risorsa, dataOraInizio, dataOraFine, stato, note);
                        storage.put(id, prenotazione);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Errore nel parsing della prenotazione alla riga " + (i+1) + ": " + line + " - " + e.getMessage());
                    }
                }
            }

            idGenerator.set(maxId + 1);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle prenotazioni: " + e.getMessage());
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

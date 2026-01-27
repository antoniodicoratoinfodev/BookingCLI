package it.gestionale.prenotazioni.domain;

import it.gestionale.prenotazioni.enums.TipoRisorsa;

public class Risorsa {
    private Long id;
    private String nome;
    private String descrizione;
    private TipoRisorsa tipo;
    private int capacita;

    public Risorsa() {}

    public Risorsa(Long id, String nome, String descrizione, TipoRisorsa tipo, int capacita) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.tipo = tipo;
        this.capacita = capacita;
    }

    public Risorsa(String nome, String descrizione, TipoRisorsa tipo, int capacita) {
        this(null, nome, descrizione, tipo, capacita);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public TipoRisorsa getTipo() { return tipo; }
    public void setTipo(TipoRisorsa tipo) { this.tipo = tipo; }
    public int getCapacita() { return capacita; }
    public void setCapacita(int capacita) { this.capacita = capacita; }

    @Override
    public String toString() {
        return String.format("ID: %-3d | Nome: %-20s | Tipo: %-20s | Cap: %-3d | Desc: %-30s",
                id, nome, tipo.getDescrizione(), capacita, descrizione);
    }

    public String toStringBreve() {
        return nome + " (" + tipo.getDescrizione() + ", cap: " + capacita + ")";
    }
}

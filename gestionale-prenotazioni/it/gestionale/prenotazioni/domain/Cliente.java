package it.gestionale.prenotazioni.domain;

public class Cliente {
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;

    public Cliente() {}

    public Cliente(Long id, String nome, String cognome, String email, String telefono) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
    }

    public Cliente(String nome, String cognome, String email, String telefono) {
        this(null, nome, cognome, email, telefono);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return String.format("ID: %-3d | Nome: %-15s | Cognome: %-15s | Email: %-20s | Tel: %-12s",
                id, nome, cognome, email, telefono);
    }

    public String toStringBreve() {
        return nome + " " + cognome + " (" + email + ")";
    }
}

package it.gestionale.prenotazioni.application;

public class GestionalePrenotazioni {
    public static void main(String[] args) {
        try {
            Application app = new Application();
            app.start();
        } catch (Exception e) {
            System.err.println("Errore critico nell'applicazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

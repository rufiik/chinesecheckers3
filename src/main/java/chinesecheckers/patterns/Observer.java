package chinesecheckers.patterns;
/**
 * Observer interface
 */
public interface Observer {
    /**
     * Metoda update aktualizuje obiekt.
     * @param message Wiadomość do aktualizacji.
     */
    void update(String message);
}

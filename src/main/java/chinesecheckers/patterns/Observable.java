package chinesecheckers.patterns;
/**
 * Observable interface
 */
public interface Observable {
    /**
     * Metoda addObserver dodaje obserwatora.
     * @param observer Obserwator.
     */
    void addObserver(Observer observer);
    /**
     * Metoda removeObserver usuwa obserwatora.
     * @param observer Obserwator.
     */
    void removeObserver(Observer observer);
    /**
     * Metoda notifyObservers powiadamia obserwatorów.
     * @param message Wiadomość do powiadomienia.
     */
    void notifyObservers(String message);
}

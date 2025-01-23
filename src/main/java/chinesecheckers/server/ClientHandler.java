package chinesecheckers.server;
import java.io.*;
import java.net.*;

import chinesecheckers.patterns.Observer;
/**
 * Klasa ClientHandler reprezentuje obsługę klienta serwera.
 */
public class ClientHandler implements Observer {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final int playerId;
    private final int maxPlayers;
/**
 * Konstruktor klasy ClientHandler.
 * @param socket Socket klienta.
 * @param playerId Identyfikator gracza.
 * @param maxPlayers Maksymalna liczba graczy.
 * @param variant Wariant gry.
 * @throws IOException Wyjątek wejścia/wyjścia.
 */
    public ClientHandler(Socket socket, int playerId, int maxPlayers,String variant) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.playerId = playerId;
        this.maxPlayers = maxPlayers;
        sendMessage("Witaj, Graczu " + playerId + "!");
        sendMessage("PLAYER_ID:" + playerId);
        sendMessage("Liczba graczy:" + maxPlayers);
        sendMessage("Wariant gry:"+ variant);
    }
/**
 * Metoda update obsługuje komunikację z klientem.
 */
    @Override
    public void update(String message) {
        sendMessage(message);
    }
    /**
     * Metoda sendMessage wysyła wiadomość do klienta.
     * @param message Wiadomość do wysłania.
     */
    public void sendMessage(String message) {
            out.println(message);
    }
    /**
     * Metoda sendGameState wysyła stan planszy do klienta.
     * @param board Plansza do gry.
     */
    public void sendGameState(Board board) {
        out.println("Stan planszy:" + board.toString());
    }
/**
 * Metoda receiveMessage odbiera wiadomość od klienta.
 * @return message - Wiadomość od klienta.
 */
    public String receiveMessage() {
        try {
            String message = in.readLine();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        } catch (IOException e) {
            System.out.println("Błąd podczas odbierania wiadomości: " + e.getMessage());
        }
        return null;
    }
/**
 * Metoda isConnected sprawdza, czy klient jest połączony.
 * @return true, jeśli klient jest połączony, w przeciwnym razie false.
 */
    public boolean isConnected() {
        try {
            socket.sendUrgentData(0xFF);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
/**
 * Metoda close zamyka połączenie z klientem.
 */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/**
 * Metoda getPlayerId zwraca identyfikator gracza.
 * @return playerId - Identyfikator gracza.
*/
    public int getPlayerId() {
        return playerId;
    }
/**
 * Metoda getMaxPlayers zwraca maksymalną liczbę graczy.
 * @return maxPlayers - Maksymalna liczba graczy.
 */
    public int getMaxPlayers() {
        return maxPlayers;
    }
/**
 * Metoda getOut zwraca obiekt PrintWriter.
 * @return out - Obiekt PrintWriter.
 */
    public PrintWriter getOut() {
        return out;
    }
/**
 * Metoda setOut ustawia obiekt PrintWriter.
 * @param out - Obiekt PrintWriter.
 */
    public void setOut(PrintWriter out) {
        this.out = out;
    }
/**
 * Metoda getIn zwraca obiekt BufferedReader.
 * @return in - Obiekt BufferedReader.
 */
    public BufferedReader getIn() {
        return in;
    }
/**
 * Metoda setIn ustawia obiekt BufferedReader.
 * @param in - Obiekt BufferedReader.
 */
    public void setIn(BufferedReader in) {
        this.in = in;
    }
}
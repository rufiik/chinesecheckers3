package chinesecheckers.client;
import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

import chinesecheckers.patterns.GameFacade;
import chinesecheckers.server.Board;
/**
 * Klasa GameClient reprezentuje klienta gry w chińskie warcaby.
 * Obsługuje komunikację z serwerem oraz interfejs graficzny.
 * Klasa ta jest odpowiedzialna za uruchomienie klienta gry.
 */
public class GameClient {
    private final String host;
    private final int port;
    private boolean isPlayerTurn = false;
    private boolean isConnected = true;
    private GameFacade gameFacade;
    private Board board;
    private int playerColor;
    private PrintWriter out;
    private int maxPlayers;
    private String variant;
/**
 * Konstruktor klasy GameClient.
 * @param host Adres IP serwera.
 * @param port Numer portu serwera.
 */
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
/**
 * Metoda start rozpoczyna połączenie z serwerem.
 */
    public void start() {
        try (Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Połączono z serwerem!");
            board = new Board();
           
            String idMessage;
            while ((idMessage = in.readLine()) != null) {
                if (idMessage.startsWith("PLAYER_ID:")) {
                    int playerId = Integer.parseInt(idMessage.split(":")[1]);
                    playerColor = playerId;
                } else if (idMessage.startsWith("Liczba graczy:")) {
                    maxPlayers = Integer.parseInt(idMessage.split(":")[1]);
                    
                }else if(idMessage.startsWith("Wariant gry:")){
                    setVariant(idMessage.split(":")[1]);
                    break;
                }
            }
            board.setVariant(variant);
            ClientGUI clientGUI = new ClientGUI(board, playerColor, this);
            gameFacade = new GameFacade(board, clientGUI, this);
            gameFacade.initializeGame(maxPlayers);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        synchronized (System.out) {
                            if (serverMessage.equals("Serwer został zamknięty.")) {
                                System.out.println(serverMessage);
                                stopConnection();
                                break;
                            } else if (serverMessage.startsWith("Twoja tura!")) {
                                isPlayerTurn = true;
                                SwingUtilities.invokeLater(() -> {
                                    gameFacade.showPlayerTurnMessage();
                                });
                            } else if (serverMessage.equals("Gra już się rozpoczęła.")) {
                                closeConnection();
                                break;
                            } else if (serverMessage.startsWith("Stan planszy:")) {
                                String gameState = serverMessage.substring("Stan planszy:".length()).trim();
                                SwingUtilities.invokeLater(() -> {
                                    gameFacade.updateGameState(gameState);
                                });
                            } else if (serverMessage.startsWith("Gracz ") && serverMessage.contains(" zajął miejsce ")) {
                                final String rankMessage = serverMessage;
                                SwingUtilities.invokeLater(() -> {
                                    gameFacade.updateStandings(rankMessage);
                                });
                            } else if (serverMessage.startsWith("Gra zakończona!")) {
                                clientGUI.endGame();
                            } else {
                                System.out.println(serverMessage);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Połączenie z serwerem zostało przerwane: " + e.getMessage());
                } finally {
                    isConnected = false;
                    stopConnection();
                }
            }).start();

            while (isConnected) {
                if (isPlayerTurn) {
                    isPlayerTurn = false;
                }
            }
        } catch (IOException e) {
            System.out.println("Nie można połączyć się z serwerem! Sprawdź, czy serwer jest uruchomiony i spróbuj ponownie.");
            System.exit(1);
        }
    }
/**
 * Metoda stopConnection zatrzymuje połączenie z serwerem.
 */
    public void stopConnection() {
        if (isConnected) {
            isConnected = false;
            System.out.println("Rozłączono z serwerem.");
        }
    }
/**
 * Metoda closeConnection zamyka połączenie z serwerem.
 */
    public void closeConnection() {
        if (isConnected) {
            isConnected = false;
            System.out.println("Gra już się rozpoczęła.");
        }
    }
/**
 * Metoda sendMove wysyła ruch gracza do serwera.
 * @param startX - współrzędna x początku ruchu  
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 */
    public void sendMove(int startX, int startY, int endX, int endY) {
        out.println("Ruch-" + startX + "," + startY + ":" + endX + "," + endY);
        SwingUtilities.invokeLater(() -> {
            gameFacade.endPlayerTurn();
        });
    }
/**
 * Metoda skipTurn pomija turę gracza.
 */
    public void skipTurn() {
        out.println("SKIP TURN");
        SwingUtilities.invokeLater(() -> {
            gameFacade.endPlayerTurn();
        });
    }
/**
 * Metoda getPlayerTurn zwraca informację o turze gracza.
 * @return true, jeśli jest tura gracza, w przeciwnym razie false.
 */
    public boolean getPlayerTurn(){
        return isPlayerTurn;
    }
/**
 * Metoda setFacade ustawia fasadę gry.
 * @return fasada gry
 */
    public GameFacade setFacade(){
       this.gameFacade = new GameFacade(board, new ClientGUI(board, playerColor, this), this);
       return this.gameFacade;
    }
    /**
     * Metoda getVariant zwraca wariant gry.
     * @return wariant gry
     */
    public String getVariant() {
        return variant;
    }
    /**
     * Metoda setVariant ustawia wariant gry.
     * @param variant wariant gry
     */
    public void setVariant(String variant) {
        this.variant = variant;
    }
    /**
     * Metoda getMaxPlayers zwraca maksymalną liczbę graczy.
     * @return maksymalna liczba graczy
     */
    public int getMaxPlayers(){
        return maxPlayers;
    }   
/**
 * Metoda main uruchamia klienta gry.
 * @param args Argumenty wywołania programu.
 */
    public static void main(String[] args) {
        GameClient client = new GameClient("localhost", 12345);
        client.start();
    }
}
package chinesecheckers.patterns;

import chinesecheckers.client.ClientGUI;
import chinesecheckers.client.GameClient;
import chinesecheckers.server.Board;
/**
 * Klasa GameFacade reprezentuje fasadę gry w chińskie warcaby.
 */
public class GameFacade {
    private Board board;
    private ClientGUI clientGUI;
    private GameClient gameClient;
/**
 * Konstruktor klasy GameFacade.
 * @param board Instancja klasy Board reprezentująca planszę do gry.
 * @param clientGUI Instancja klasy ClientGUI reprezentująca interfejs graficzny klienta gry.
 * @param gameClient Instancja klasy GameClient reprezentująca klienta gry.
 */
    public GameFacade(Board board, ClientGUI clientGUI, GameClient gameClient) {
        this.board = board;
        this.clientGUI = clientGUI;
        this.gameClient = gameClient;
    }
/**
 * Metoda initializeGame inicjalizuje grę.
 * @param maxPlayers Maksymalna liczba graczy.
 */
    public void initializeGame(int maxPlayers) {
        board.initializeOpponentBaseMapping(maxPlayers);
        clientGUI.initialize();
    }
/**
 * Metoda updateGameState aktualizuje stan gry.
 * @param gameState stan gry/planszy
 */
    public void updateGameState(String gameState) {
        board.update(gameState);
        clientGUI.repaint();
    }
/**
 * Metoda sendMove wysyła ruch do serwera.
 * @param startX - współrzędna x początku ruchu
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 */
    public void sendMove(int startX, int startY, int endX, int endY) {
        gameClient.sendMove(startX, startY, endX, endY);
    }
/**
 *   Metoda skipTurn pomija turę gracza.
 */
    public void skipTurn() {
        gameClient.skipTurn();
    }
/**
 * Metoda getPlayerTurn zwraca informację o turze gracza.
 */
    public void showPlayerTurnMessage() {
        clientGUI.showPlayerTurnMessage();
    }
/**
 * Metoda updateStandings aktualizuje ranking graczy.
 * @param rankMessage ranking graczy
 */
    public void updateStandings(String rankMessage) {
        clientGUI.updateStandings(rankMessage);
    }
/**
 * Metoda endPlayerTurn kończy turę gracza.
 */
    public void endPlayerTurn() {
        clientGUI.endPlayerTurn();
    }
/**
 * Metoda endGame kończy grę.
 */
    public void endGame() {
        clientGUI.endGame();
    }
}

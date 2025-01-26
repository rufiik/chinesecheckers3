package chinesecheckers.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BotPlayer extends ClientHandler {
    private Board board;
    private int playerId;
    private int maxPlayers;
    private String variant;
/**
 * Konstruktor klasy BotPlayer.
 * @param socket Socket klienta.
 * @param playerId Identyfikator gracza.
 * @param maxPlayers Maksymalna liczba graczy.
 * @param variant Wariant gry.
 * @throws IOException Wyjątek wejścia/wyjścia.
 */
    public BotPlayer(Socket socket, int playerId, int maxPlayers, String variant) throws IOException {
        super(socket, playerId, maxPlayers, variant, true);
        this.board = new Board();
        this.playerId = playerId;
        this.maxPlayers = maxPlayers;
        this.variant = variant;
        initializeEnemyBase(maxPlayers);
    }
    /**
     * Metoda initializeEnemyBase inicjalizuje bazę przeciwnika.
     * @param maxPlayers Maksymalna liczba graczy.
     */
    public void initializeEnemyBase(int maxPlayers) {
        board.initializeOpponentBaseMapping(maxPlayers);
    }
    /**
     * Metoda makeMove wykonuje ruch bota.
     * @return bestMove - Najlepszy ruch bota.
     */
    private int[] makeMove() {
        List<int[]> pieces = board.getPlayerPieces(playerId);
        int[] bestMove = null;
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;
    
        for (int[] piece : pieces) {
            List<int[]> destinations = getAllPossibleMoves(piece[0], piece[1]);
            for (int[] destination : destinations) {
                int dStart = calculateDistance(piece);
                int dDest = calculateDistance(destination);
                int destLength = dDest - dStart;

                if (destLength < minDistance) {
                    minDistance = destLength;
                    bestMove = new int[]{piece[0], piece[1], destination[0], destination[1]};
                    maxDistance = dStart;
                } else if (destLength == minDistance && dStart > maxDistance) {
                    bestMove = new int[]{piece[0], piece[1], destination[0], destination[1]};
                    maxDistance = dStart;
                }
            }
        }
        return bestMove;
    }
    /**
     * Metoda getAllPossibleMoves zwraca wszystkie możliwe ruchy.
     * @param x Współrzędna x.
     * @param y Współrzędna y.
     * @return allMoves - Lista wszystkich możliwych ruchów.
     */
    private List<int[]> getAllPossibleMoves(int x, int y) {
        List<int[]> allMoves = new ArrayList<>();
        List<int[]> directMoves = board.getPossibleMoves(x, y);
        for (int[] move : directMoves) {
            if (board.isValidMove(x, y, move[0], move[1], playerId)) {
                allMoves.add(move);
            }
        }

        for (int[] move : directMoves) {
            List<int[]> jumpMoves = getJumpMovesRecursive(move, new ArrayList<>());
            for (int[] jumpMove : jumpMoves) {
                if (board.isValidMove(x, y, jumpMove[0], jumpMove[1], playerId)) {
                    allMoves.add(jumpMove);
                }
            }
        }
        return allMoves;
    }
    /**
     * Metoda getJumpMovesRecursive zwraca możliwe ruchy skoku.
     * @param position Pozycja.
     * @param visited Lista odwiedzonych.
     * @return jumpMoves - Lista możliwych ruchów skoku.
     */
    private List<int[]> getJumpMovesRecursive(int[] position, List<int[]> visited) {
        List<int[]> jumpMoves = new ArrayList<>();
        List<int[]> possibleJumps = board.getPossibleJumps(position[0], position[1], playerId);
    
        for (int[] jump : possibleJumps) {
            boolean alreadyVisited = false;
            for (int[] visitedPos : visited) {
                if (visitedPos[0] == jump[0] && visitedPos[1] == jump[1]) {
                    alreadyVisited = true;
                    break;
                }
            }
            if (!alreadyVisited) {
                jumpMoves.add(jump);
                visited.add(new int[]{jump[0], jump[1]});
                jumpMoves.addAll(getJumpMovesRecursive(jump, visited));
            }
        }
        return jumpMoves;
    }
    /**
     * Metoda calculateDistance oblicza odległość.
     * @param position Pozycja.
     * @return Odległość.
     */
    private int calculateDistance(int[] position) {
        int targetX = board.getOpponentBasePositions(playerId).iterator().next()[0];
        int targetY = board.getOpponentBasePositions(playerId).iterator().next()[1];
        int dx = Math.abs(position[0] - targetX);
        int dy = Math.abs(position[1] - targetY);
        int dz = Math.abs(position[0] + position[1] - targetX - targetY);
        return dx + dy + dz;
    }
    /**
     * Metoda isConnected zwraca informację o połączeniu bot zawsze true.
     */
    public boolean isConnected() {
        return true;
    }
    /**
     * Metoda getMove zwraca ruch bota opozniony o 500 milisekund.
     * @return Ruch bota.
     */
    public String getMove() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] bestMove = makeMove();
        if (bestMove != null) {
            return "Ruch-" + bestMove[0] + "," + bestMove[1] + ":" + bestMove[2] + "," + bestMove[3];
        }
        return null;
    }
    /**
     * Metoda updateBoard aktualizuje planszę.
     * @param gameState Stan gry.
     */
    public void updateBoard(String gameState) {
        board.update(gameState);
    }
    /**
     * Metoda sendMessage wysyła wiadomość.
     */
    @Override
    public void sendMessage(String message) {
        if (message.startsWith("Stan planszy:")) {
            String gameState = message.substring("Stan planszy:".length()).trim();
            updateBoard(gameState);
        }

}
}
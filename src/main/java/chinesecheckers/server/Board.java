package chinesecheckers.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
/**
 * Klasa Board reprezentuje planszę do gry w chińskie warcaby.
 */
public class Board {
    private int[][] board;
    private static final int ROWS = 17;
    private static final int COLUMNS = 25;
    private List<Set<int[]>> playerBases;
    private int[] opponentBaseMapping;
    private int maxPlayers;
    private String variant;
/**
 *  Konstruktor klasy Board.  
 *  Inicjalizuje planszę do gry oraz bazy graczy.
 */
    public Board() {
        board = new int[ROWS][COLUMNS];
        initializeBoard();
        initializePlayerBases();
    }
/**
 *  Metoda initializeBoard inicjalizuje planszę do gry.
 */
    private void initializeBoard() {
        board = new int[][] {
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0},
            {7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7},
            {7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7},
            {7, 7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7, 7},
            {7, 7, 7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7, 7, 7},
            {7, 7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7, 7},
            {7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7},
            {7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7},
            {0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7}
        };
    }
/**
 * Metoda initializePlayerBases inicjalizuje bazy graczy.
 */
    private void initializePlayerBases() {
        playerBases = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            playerBases.add(new HashSet<>());
        }

        addPlayerBase(0, new int[][]{{0, 12}, {1, 11}, {1, 13}, {2, 10}, {2, 12}, {2, 14}, {3, 9}, {3, 11}, {3, 13}, {3, 15}});
        addPlayerBase(1, new int[][]{{16, 12}, {15, 11}, {15, 13}, {14, 10}, {14, 12}, {14, 14}, {13, 9}, {13, 11}, {13, 13}, {13, 15}});
        addPlayerBase(2, new int[][]{{12, 0}, {11, 1}, {12, 2}, {10, 2}, {11, 3}, {12, 4}, {9, 3}, {10, 4}, {11, 5}, {12, 6}});
        addPlayerBase(3, new int[][]{{12, 24}, {11, 23}, {12, 22}, {10, 22}, {9, 21}, {11, 21}, {10, 20}, {12, 20}, {11, 19}, {12, 18}});
        addPlayerBase(4, new int[][]{{4, 0}, {5, 1}, {4, 2}, {6, 2}, {5, 3}, {4, 4}, {5, 5}, {6, 4}, {7, 3}, {4, 6}});
        addPlayerBase(5, new int[][]{{4, 24}, {5, 23}, {4, 22}, {6, 22}, {5, 21}, {7, 21}, {4, 20}, {6, 20}, {4, 18}, {5, 19}});
    }
/**
 * Metoda initializeOpponentBaseMapping inicjalizuje mapowanie bazy przeciwnika.
 * @param numberOfPlayers Liczba graczy.
 */
    public void initializeOpponentBaseMapping(int numberOfPlayers) {
        switch(numberOfPlayers) {
            case 2:
                opponentBaseMapping = new int[]{1, 0};
                break;
            case 3:
                opponentBaseMapping = new int[]{1, 4, 5};
                break;
            case 4:
                opponentBaseMapping = new int[]{3, 2, 5, 4};
                break;
            case 6:
                opponentBaseMapping = new int[]{1, 0, 5, 4, 3, 2};
                break;
            default:
                throw new IllegalArgumentException("Nieprawidłowa ilość graczy: " + numberOfPlayers);
        }
    }
/**
 * Metoda addPlayerBase dodaje bazę gracza.
 * @param playerIndex Indeks gracza.
 * @param positions Pozycje gracza.
 */
    private void addPlayerBase(int playerIndex, int[][] positions) {
        for (int[] pos : positions) {
            playerBases.get(playerIndex).add(pos);
        }
    }
/**
 * Metoda initializeBoardForPlayers inicjalizuje planszę dla określonej liczby graczy.
 * @param numberOfPlayers Liczba graczy.
 * @throws IllegalArgumentException Jeśli liczba graczy jest nieprawidłowa.
 */
    public void initializeBoardForPlayers(int numberOfPlayers) {
        switch (numberOfPlayers) {
            case 2:
                setPlayerPieces(1, new int[][]{{0, 12}, {1, 11}, {1, 13}, {2, 10}, {2, 12}, {2, 14}, {3, 9}, {3, 11}, {3, 13}, {3, 15}});
                setPlayerPieces(2, new int[][]{{16, 12}, {15, 11}, {15, 13}, {14, 10}, {14, 12}, {14, 14}, {13, 9}, {13, 11}, {13, 13}, {13, 15}});
                break;
    
            case 3:
                setPlayerPieces(1, new int[][]{{0, 12}, {1, 11}, {1, 13}, {2, 10}, {2, 12}, {2, 14}, {3, 9}, {3, 11}, {3, 13}, {3, 15}});
                setPlayerPieces(2, new int[][]{{12, 24}, {11, 23}, {12, 22}, {10, 22}, {9, 21}, {11, 21}, {10, 20}, {12, 20}, {11, 19}, {12, 18}});
                setPlayerPieces(3, new int[][]{{12, 0}, {11, 1}, {12, 2}, {10, 2}, {11, 3}, {12, 4}, {9, 3}, {10, 4}, {11, 5}, {12, 6}});
                break;
    
            case 4:
                setPlayerPieces(1, new int[][]{{4, 0}, {5, 1}, {4, 2}, {6, 2}, {5, 3}, {4, 4}, {5, 5}, {6, 4}, {7, 3}, {4, 6}});
                setPlayerPieces(2, new int[][]{{4, 24}, {5, 23}, {4, 22}, {6, 22}, {5, 21}, {7, 21}, {4, 20}, {6, 20}, {4, 18}, {5, 19}});
                setPlayerPieces(3, new int[][]{{12, 0}, {11, 1}, {12, 2}, {10, 2}, {11, 3}, {12, 4}, {9, 3}, {10, 4}, {11, 5}, {12, 6}});
                setPlayerPieces(4, new int[][]{{12, 24}, {11, 23}, {12, 22}, {10, 22}, {9, 21}, {11, 21}, {10, 20}, {12, 20}, {11, 19}, {12, 18}});
                break;
    
            case 6:
                setPlayerPieces(1, new int[][]{{0, 12}, {1, 11}, {1, 13}, {2, 10}, {2, 12}, {2, 14}, {3, 9}, {3, 11}, {3, 13}, {3, 15}});
                setPlayerPieces(2, new int[][]{{16, 12}, {15, 11}, {15, 13}, {14, 10}, {14, 12}, {14, 14}, {13, 9}, {13, 11}, {13, 13}, {13, 15}});
                setPlayerPieces(3, new int[][]{{12, 0}, {11, 1}, {12, 2}, {10, 2}, {11, 3}, {12, 4}, {9, 3}, {10, 4}, {11, 5}, {12, 6}});
                setPlayerPieces(4, new int[][]{{12, 24}, {11, 23}, {12, 22}, {10, 22}, {9, 21}, {11, 21}, {10, 20},{12, 20}, {11, 19}, {12, 18}});
                setPlayerPieces(5, new int[][]{{4, 0}, {5, 1}, {4, 2}, {6, 2}, {5, 3}, {4, 4}, {5, 5}, {6, 4}, {7, 3}, {4, 6}});
                setPlayerPieces(6, new int[][]{{4, 24}, {5, 23}, {4, 22}, {6, 22}, {5, 21}, {7, 21}, {4, 20}, {6, 20}, {4, 18}, {5, 19}});
                break;
    
            default:
                throw new IllegalArgumentException("Nieprawidłowa ilość graczy: " + numberOfPlayers);
        }
    }
    /**
     * Metoda initializeBoardForChaos inicjalizuje planszę dla gry w Order Out Of Chaos.
     * @param numberOfPlayers Liczba graczy.
     */
    public void initializeBoardForChaos(int numberOfPlayers) {
        Random random = new Random();
        int piecesPerPlayer = 10;
        List<int[]> validPositions = new ArrayList<>();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (board[i][j] == 0 && !isInAnyBase(i, j)) {
                    validPositions.add(new int[]{i, j});
                }
            }
        }

        for (int i = validPositions.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = validPositions.get(i);
            validPositions.set(i, validPositions.get(j));
            validPositions.set(j, temp);
        }

        int index = 0;
        for (int player = 1; player <= numberOfPlayers; player++) {
            for (int p = 0; p < piecesPerPlayer; p++) {
                int[] pos = validPositions.get(index++);
                board[pos[0]][pos[1]] = player;
            }
        }
    }
/**
 * Metoda setVariant ustawia wariant gry.
 * @param player - numer gracza
 * @param positions - pozycje gracza
 */
    private void setPlayerPieces(int player, int[][] positions) {
        for (int[] pos : positions) {
            board[pos[0]][pos[1]] = player;
        }
    }
/**
 * Metoda isInAnyBase sprawdza, czy dana pozycja jest w bazie któregokolwiek gracza.
 * @param x - współrzędna x
 * @param y - współrzędna y
 * @return true, jeśli pozycja jest w bazie któregokolwiek gracza, w przeciwnym razie false.
 */
    private boolean isInAnyBase(int x, int y) {
        for (Set<int[]> base : playerBases) {
            for (int[] pos : base) {
                if (pos[0] == x && pos[1] == y) {
                    return true;
                }
            }
        }
        return false;
    }
/**
 * Metoda isInOpponentBase sprawdza, czy dana pozycja jest w bazie przeciwnika.
 * @param x - współrzędna x
 * @param y - współrzędna y
 * @param playerId - numer gracza
 * @return true, jeśli pozycja jest w bazie przeciwnika, w przeciwnym razie false.
 */
    public boolean isInOpponentBase(int x, int y, int playerId) {
        int opponentBaseIndex = opponentBaseMapping[playerId - 1];
        for (int[] base : playerBases.get(opponentBaseIndex)) {
            if (base[0] == x && base[1] == y) {
                return true;
            }
        }
        return false;
    }
/**
 * Metoda isInHomeBase sprawdza, czy dana pozycja jest w bazie domowej gracza.
 * @param x - współrzędna x
 * @param y - współrzędna y
 * @param playerId - numer gracza
 * @return true, jeśli pozycja jest w bazie domowej gracza, w przeciwnym razie false.
 */
    public boolean isInHomeBase(int x, int y, int playerId) {
        for (int[] base : playerBases.get(playerId - 1)) {
            if (base[0] == x && base[1] == y) {
                return true;
            }
        }
        return false;
    }
/**
 * Metoda allPiecesInHomeBase sprawdza, czy wszystkie pionki gracza są w bazie domowej.
 * @param playerId - numer gracza
 * @return true, jeśli wszystkie pionki gracza są w bazie domowej, w przeciwnym razie false.
 */
    public boolean allPiecesInHomeBase(int playerId) {
        Set<int[]> homeBase = playerBases.get(playerId - 1);
        for (int[] pos : homeBase) {
            if (board[pos[0]][pos[1]] != playerId) {
                return false;
            }
        }
        return true;
    }
/**
 * getHomeBasePositions zwraca pozycje bazy domowej gracza.
 * @param playerId - numer gracza
 * @return pozycje bazy pionków gracza.
 */
    public Set<int[]> getHomeBasePositions(int playerId) {
        return playerBases.get(playerId - 1);
    }
/**
 * getOpponentBasePositions zwraca pozycje bazy przeciwnika.
 * @param playerId - numer gracza
 * @return pozycje bazy pionków przeciwnika.
 */
    public Set<int[]> getOpponentBasePositions(int playerId) {
        int opponentBaseIndex = opponentBaseMapping[playerId - 1];
        return playerBases.get(opponentBaseIndex);
    }
/**
 * Metoda movePiece wykonuje ruch pionka.
 * @param startX - współrzędna x początku ruchu
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 * @param playerId - numer gracza
 * @return informacja o wykonanym ruchu
 */
    public synchronized String movePiece(int startX, int startY, int endX, int endY, int playerId) {
        if (isValidMove(startX, startY, endX, endY, playerId)) {
            board[endX][endY] = board[startX][startY];
            board[startX][startY] = 0;
            return "Ruch wykonany z (" + startX + "," + startY + ") na (" + endX + "," + endY + ").";
        } else {
            return "Nieprawidłowy ruch z (" + startX + "," + startY + ") na (" + endX + "," + endY + ").";
        }
    }
/**
 * Metoda hasPiece sprawdza, czy na danej pozycji znajduje się pionek.
 * @param x - współrzędna x początku skoku
 * @param y - współrzędna y początku skoku
 * @return  true, jeśli na danej pozycji znajduje się pionek, w przeciwnym razie false.
 */
    public boolean hasPiece(int x, int y) {
        return board[x][y] != 0 && board[x][y] != 7;
    }
/**
 * Metoda isEmpty sprawdza, czy dana pozycja jest pusta.
 * @param x - współrzędna x
 * @param y - współrzędna y
 * @return true, jeśli pozycja jest pusta, w przeciwnym razie false.
 */
    public boolean isEmpty(int x, int y) {
        return board[x][y] == 0;
    }
/**
 * Metoda getPossibleJumps zwraca możliwe skoki.
 * @param startX - współrzędna x początku skoku
 * @param startY - współrzędna y początku skoku
 * @param playerId - numer gracza
 * @return możliwe skoki
 */
    public List<int[]> getPossibleJumps(int startX, int startY, int playerId) {
        List<int[]> jumps = new ArrayList<>();
        int[][] directions = {
            {-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-2, -2}, {2, 2}, {-2, 2}, {2, -2}
        };

        for (int[] dir : directions) {
            int midX = startX + dir[0] / 2;
            int midY = startY + dir[1] / 2;
            int endX = startX + dir[0];
            int endY = startY + dir[1];
            if (isWithinBoard(endX, endY) && hasPiece(midX, midY) && isEmpty(endX, endY)) {
                jumps.add(new int[]{endX, endY});
            }
        }

        return jumps;
    }
/**
 * Metoda isValidMultiJump sprawdza, czy możliwe jest wielokrotne skakanie.
 * @param startX - współrzędna x początku skoku
 * @param startY - współrzędna y początku skoku
 * @param endX - współrzędna x końca skoku
 * @param endY - współrzędna y końca skoku
 * @param playerId - numer gracza
 * @return true, jeśli możliwe jest wielokrotne skakanie, w przeciwnym razie false.
 */
    public boolean isValidMultiJump(int startX, int startY, int endX, int endY, int playerId) {
        Set<String> visited = new HashSet<>();
        boolean result = canJump(startX, startY, endX, endY, playerId, visited);
        return result;
    }
/**
 * Metoda canJump sprawdza, czy możliwe jest skakanie.
 * @param startX - współrzędna x początku skoku
 * @param startY - współrzędna y początku skoku
 * @param endX - współrzędna x końca skoku
 * @param endY - współrzędna y końca skoku
 * @param playerId - numer gracza
 * @param visited - odwiedzone pozycje
 * @return true, jeśli możliwe jest skakanie, w przeciwnym razie false.
 */
    private boolean canJump(int startX, int startY, int endX, int endY, int playerId, Set<String> visited) {
        if (startX == endX && startY == endY) {
            return true;
        }

        visited.add(startX + "," + startY);

        for (int[] jump : getPossibleJumps(startX, startY, playerId)) {
            int nextX = jump[0];
            int nextY = jump[1];
            if (!visited.contains(nextX + "," + nextY) && canJump(nextX, nextY, endX, endY, playerId, visited)) {
                return true;
            }
        }

        visited.remove(startX + "," + startY);
        return false;
    }
/**
 * Metoda isValidMove sprawdza, czy ruch jest prawidłowy.
 * @param startX - współrzędna x początku ruchu
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 * @param playerId - numer gracza
 * @return true, jeśli ruch jest prawidłowy, w przeciwnym razie false.
 */
    public boolean isValidMove(int startX, int startY, int endX, int endY, int playerId) {
        if (!isWithinBoard(startX, startY) || !isWithinBoard(endX, endY)) {
            return false;
        }

        if (!hasPiece(startX, startY)) {
            return false;
        }

        if (!isEmpty(endX, endY)) {
            return false;
        }
        if ("Order Out Of Chaos".equals(variant)) {
            if (isInHomeBase(startX, startY, playerId) && !isInHomeBase(endX, endY, playerId)) {
                return false;
            }
        } else if ("Rozgrywka klasyczna".equals(variant)) {
            if (isInOpponentBase(startX, startY, playerId) && !isInOpponentBase(endX, endY, playerId)) {
                return false;
            }
        }

        if (isAdjacentMove(startX, startY, endX, endY) || isJumpMove(startX, startY, endX, endY) || isValidMultiJump(startX, startY, endX, endY, playerId)) {
            return true;
        }
        return false;
    }
/**
 * Metoda isWithinBoard sprawdza, czy dana pozycja jest na planszy.
 * @param x - współrzędna x
 * @param y - współrzędna y
 * @return true, jeśli pozycja jest na planszy, w przeciwnym razie false.
 */
    private boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < ROWS && y >= 0 && y < COLUMNS && board[x][y] != 7;
    }
/**
 * Metoda isAdjacentMove sprawdza, czy ruch jest sąsiedni.
 * @param startX - współrzędna x początku ruchu
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 * @return true, jeśli ruch jest sąsiedni, w przeciwnym razie false.
 */
    private boolean isAdjacentMove(int startX, int startY, int endX, int endY) {
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1) || (dx == 1 && dy == 1);
    }
/**
 * Metoda isJumpMove sprawdza, czy ruch jest skokiem.
 * @param startX - współrzędna x początku ruchu
 * @param startY - współrzędna y początku ruchu
 * @param endX - współrzędna x końca ruchu
 * @param endY - współrzędna y końca ruchu
 * @return true, jeśli ruch jest skokiem, w przeciwnym razie false.
 */
    private boolean isJumpMove(int startX, int startY, int endX, int endY) {
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        return hasPiece(midX, midY) && isAdjacentMove(startX, startY, midX, midY) && isAdjacentMove(midX, midY, endX, endY);
    }
/**
 * Metoda update aktualizuje planszę.
 * @param gameState - stan gry/planszy
 */
    public void update(String gameState) {
        String[] rows = gameState.split(";");
        for (int i = 0; i < rows.length; i++) {
            String[] cells = rows[i].split(",");
            for (int j = 0; j < cells.length; j++) {
                board[i][j] = Integer.parseInt(cells[j]);
            }
        }
    }
/**
 * Metoda isPlayerInOpponentBase sprawdza, czy gracz jest w bazie przeciwnika.
 * @param playerId - numer gracza
 * @return true, jeśli gracz jest w bazie przeciwnika, w przeciwnym razie false.
 */
    public boolean isPlayerInOpponentBase(int playerId) {
        int opponentBaseIndex = opponentBaseMapping[playerId - 1];
        Set<int[]> opponentBase = playerBases.get(opponentBaseIndex);

        for (int[] pos : opponentBase) {
            if (board[pos[0]][pos[1]] != playerId) {
                return false;
            }
        }
        return true;
    }
/**
 * toString zwraca stan planszy w postaci tekstowej.
 */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell).append(",");
            }
            sb.deleteCharAt(sb.length() - 1); 
            sb.append(";");
        }
        sb.deleteCharAt(sb.length() - 1); 
        return sb.toString();
    }
/**
 * Metoda getBoard zwraca planszę.
 * @return board - plansza
 */
    public int[][] getBoard() {
        return board;
    }
/**
 * Metoda getPlayerBases zwraca bazy graczy.
 * @return oponentBaseMapping - mapowanie baz przeciwników 
 */
    public int[] getOpponentBaseMapping() {
        return opponentBaseMapping;
    }
/**
 * Metoda setMaxPlayers ustawia maksymalną liczbę graczy.
 * @param maxPlayers - maksymalna liczba graczy
 */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
/**
 * Metoda setVariant ustawia wariant gry.
 * @param variant - wariant gry
 */
    public void setVariant(String variant) {
        this.variant = variant;
    }
}
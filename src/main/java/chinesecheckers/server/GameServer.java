package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

import chinesecheckers.patterns.Observable;
import chinesecheckers.patterns.Observer;
/**
 * Klasa GameServer reprezentuje serwer gry.
 */
public class GameServer implements Observable{
    private static GameServer instance;    
    private final int port;
    private final List<ClientHandler> players = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private final Set<Integer> disconnectedPlayers = new HashSet<>();
    private final List<Integer> standings = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int maxPlayers;
    private int nextPlayerId = 1;
    private final Board board;
    private boolean running;
    private boolean gameStarted = false;
    private final ServerGUI gui;
    private String variant;
    /**
     * Konstruktor klasy GameServer.
     * @param port Numer portu serwera.
     * @param gui Instancja klasy ServerGUI reprezentująca interfejs graficzny serwera.
     */
    private GameServer(int port, ServerGUI gui) {
        this.port = port;
        this.gui = gui;
        this.board = new Board();
    }
/**
 * Metoda getInstance zwraca instancję serwera gry.
 * @param port Numer portu serwera.
 * @param gui Instancja klasy ServerGUI reprezentująca interfejs graficzny serwera.
 * @return instance - instancja serwera gry.
 */
    public static synchronized GameServer getInstance(int port, ServerGUI gui) {
        if (instance == null) {
            instance = new GameServer(port, gui);
        }
        return instance;
    }
    /**
     * Metoda start uruchamia serwer gry.
     */
    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(port)){ 
            running=true;
            System.out.println("Serwer uruchomiony na porcie: " + port);
            initializeGame(serverSocket);
            startGame(serverSocket);
        } catch (BindException e) {
            System.out.println("Serwer już działa na porcie: " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Metoda startWithMockSocket uruchamia serwer gry z podanym gniazdem serwera/mockiem.
     * @param mockServerSocket Gniazdo serwera.
     */
    public void startWithMockSocket(ServerSocket mockServerSocket) {
        try {
            mockServerSocket.bind(null);
            this.running = true;
        } catch (BindException e) {
            this.running = false;
        } catch (IOException e) {
            this.running = false;
        }
    }
/**
 * Metoda initializeGame inicjalizuje grę.
 */
    private void initializeGame(ServerSocket serverSocket) throws IOException {
        variant = gui.getSelectedVariant();
        maxPlayers = gui.getSelectedPlayers();
        System.out.println("Wybrano liczbę graczy: " + maxPlayers);

        board.setMaxPlayers(maxPlayers);
        board.setVariant(variant);

        if ("Order Out Of Chaos".equals(variant)) {
            board.initializeBoardForChaos(maxPlayers);
        } else {
            board.initializeBoardForPlayers(maxPlayers);
        }

        System.out.println("Oczekiwanie na graczy..."); 
        new Thread(() -> handleNewConnections(serverSocket)).start();
        synchronized (players) {
            while (players.size() < maxPlayers) {
                try {
                    players.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Oczekiwanie na graczy przerwane.");
                }
            }
        }
    
        for (ClientHandler player : players) {
            playerOrder.add(player.getPlayerId());
        }
        
        System.out.println("Wszyscy gracze dołączyli. Losowanie kolejności...");
        Collections.shuffle(playerOrder);
        board.initializeOpponentBaseMapping(maxPlayers);
        for (ClientHandler player : players) {
            player.sendMessage("Kolejność gry: " + playerOrder.toString());
        }
        broadcastGameState();
        gameStarted = true;
    }
/**
 * Metoda removeDisconnectedPlayersBeforeStart usuwa rozłączonych graczy przed rozpoczęciem gry.
 */
    private void removeDisconnectedPlayersBeforeStart() {
        Iterator<ClientHandler> iterator = players.iterator();
        while (iterator.hasNext()) {
            ClientHandler player = iterator.next();
            if (!player.isConnected()) {
                System.out.println("Gracz " + player.getPlayerId() + " rozłączył się.");
                iterator.remove();
            }
        }
    }
/**
 * Metoda startGame rozpoczyna grę.
 * @param serverSocket Gniazdo serwera.
 * @throws IOException Wyjątek wejścia/wyjścia.
 */
    private void startGame(ServerSocket serverSocket) throws IOException {
        while ((standings.size() + disconnectedPlayers.size()) < maxPlayers) {
            processTurn();
        }
        System.out.println("Gra zakończona!");
        running=false;
        cleanupDisconnectedPlayers();
        displayStandings();
        gui.waitForWindowClose();
        System.exit(0);
    }
/**
 * Metoda processTurn przetwarza turę gracza.
 */
    private synchronized void processTurn() {
        int playerId = playerOrder.get(currentPlayerIndex);
        ClientHandler currentPlayer = null;

        for (ClientHandler player : players) {
            if (player.getPlayerId() == playerId) {
                currentPlayer = player;
                break;
            }
        }

        if (standings.contains(playerId) || disconnectedPlayers.contains(playerId)) {
            currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
            return;
        }

        if (currentPlayer == null || !currentPlayer.isConnected()) {
            System.out.println("Gracz " + playerId + " rozłączył się.");
            broadcastMessage("Gracz " + playerId + " rozłączył się.");
            disconnectedPlayers.add(playerId);
            currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
            return;
        }
        
        currentPlayer.sendMessage("Twoja tura!");
        broadcastMessage("Gracz " + playerId + " wykonuje ruch.", playerId);

        String move = currentPlayer.receiveMessage();
        if (move == null) {
            System.out.println("Gracz " + playerId + " rozłączył się w trakcie swojej tury.");
            broadcastMessage("Gracz " + playerId + " rozłączył się w trakcie swojej tury!");
            disconnectedPlayers.add(playerId);
        } else if (move.equalsIgnoreCase("WYGRANA")) {
            standings.add(playerId);
            broadcastMessage("Gracz " + playerId + " zajął miejsce " + standings.size() + "!");
        } else if (move != null && move.startsWith("Ruch-")) {
            String[] parts = move.substring(5).split(":");
            if (parts.length == 2) {
                String[] startCoords = parts[0].split(",");
                String[] endCoords = parts[1].split(",");
                if (startCoords.length == 2 && endCoords.length == 2) {
                    try {
                        int startX = Integer.parseInt(startCoords[0]);
                        int startY = Integer.parseInt(startCoords[1]);
                        int endX = Integer.parseInt(endCoords[0]);
                        int endY = Integer.parseInt(endCoords[1]);
    
                        String result = board.movePiece(startX, startY, endX, endY, playerId);
                        currentPlayer.sendMessage(result);
    
                        if (result.startsWith("Ruch wykonany")) {
                            System.out.println("Gracz " + playerId + " wykonał ruch: " + move);
                            broadcastMessage("Gracz " + playerId + " wykonał ruch: " + move, playerId);
                            broadcastGameState();

                            if ("Order Out Of Chaos".equals(variant) && board.allPiecesInHomeBase(playerId) && !standings.contains(playerId)) {
                                standings.add(playerId);
                                broadcastMessage("Gracz " + playerId + " zajął miejsce " + standings.size() + "!");
                            } else if (board.isPlayerInOpponentBase(playerId) && !standings.contains(playerId)) {
                                standings.add(playerId);
                                broadcastMessage("Gracz " + playerId + " zajął miejsce " + standings.size() + "!");
                            }
                        } else if (result.startsWith("Nieprawidłowy ruch")) {
                            currentPlayer.sendMessage("Nieprawidłowy ruch. Spróbuj ponownie.");
                        } else {
                            currentPlayer.sendMessage("Błąd: " + result);
                            System.out.println("Błąd: " + result);
                        }
                    } catch (NumberFormatException e) {
                        currentPlayer.sendMessage("Nieprawidłowe współrzędne. Spróbuj ponownie.");
                    }
                }
            }
        } else if(move.startsWith("SKIP TURN")) {
            System.out.println("Gracz " + playerId + " zrezygnował z ruchu.");
            broadcastMessage("Gracz " + playerId + " zrezygnował z ruchu.");

        }

        currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;

      if ((standings.size() + disconnectedPlayers.size()) == maxPlayers - 1) {
            for (int id : playerOrder) {
                if (!standings.contains(id) && !disconnectedPlayers.contains(id)) {
                    standings.add(id);
                    broadcastMessage("Gracz " + id + " zajął miejsce " + standings.size() + "!");
                    break;
                }
            }
        }
    }
/**
 * Metoda addObserver dodaje obserwatora.
 */
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
/**
 * Metoda removeObserver usuwa obserwatora.
 */
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
/**
 * Metoda notifyObservers powiadamia obserwatorów.
 */
    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
/**
 * Metoda broadcastMessage wysyła wiadomość do wszystkich obserwatorów.
 * @param message Wiadomość do wysłania.
 */
    public void broadcastMessage(String message) {
        notifyObservers(message);
    }
/**
 * Metoda broadcastMessage wysyła wiadomość do wszystkich obserwatorów z wyłączeniem określonego gracza.
 * @param message Wiadomość do wysłania.
 * @param excludePlayerId Identyfikator gracza, który ma zostać wykluczony.
 */
    private void broadcastMessage(String message, int excludePlayerId) {
        for (Observer observer : observers) {
            if (observer instanceof ClientHandler && 
                ((ClientHandler) observer).getPlayerId() != excludePlayerId) {
                observer.update(message);
            }
        }
    }
/**
 *  Metoda cleanupDisconnectedPlayers usuwa rozłączonych graczy.
 */
    private void cleanupDisconnectedPlayers() {
        for (ClientHandler player : players) {
            if (!player.isConnected()) {
                player.close();
            }
        }
    }
/**
 * Metoda displayStandings wyświetla ranking graczy.
 */
    private void displayStandings() {
        System.out.println("Kolejność końcowa:");
        broadcastMessage("Gra zakończona! Kolejność końcowa: ");
        for (int i = 0; i < standings.size(); i++) {
            int playerId = standings.get(i);
            System.out.println((i + 1) + ". miejsce: Gracz " + playerId);
            broadcastMessage((i + 1) + ". miejsce: Gracz " + playerId);
        }
        for (int playerId : disconnectedPlayers) {
            System.out.println("Gracz " + playerId + " rozłączył się przed zakończeniem gry");
            broadcastMessage("Gracz " + playerId + " rozłączył się przed zakończeniem gry");
        }
    }
/**
 * Metoda handleNewConnections obsługuje nowe połączenia.
 * @param serverSocket Gniazdo serwera.
 */
    private void handleNewConnections(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                synchronized (players) {
                    if (gameStarted) {
                        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            out.println("Gra już się rozpoczęła.");
                        } finally {
                            clientSocket.close();
                        }
                    } else {
                        ClientHandler player = new ClientHandler(clientSocket, nextPlayerId++,maxPlayers,variant);
                        if (player.isConnected()) {
                            players.add(player);
                            addObserver(player);
                            System.out.println("Gracz " + player.getPlayerId() + " dołączył do gry.");
                            players.notifyAll();
                        } else {
                            System.out.println("Gracz " + player.getPlayerId() + " rozłączył się przed dołączeniem do gry.");
                        }
                        removeDisconnectedPlayersBeforeStart();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Metoda broadcastGameState wysyła stan gry do wszystkich graczy.
     */
    public synchronized void broadcastGameState() {
        for (ClientHandler client : players) {
            client.sendGameState(board);
        }
    }
    /**
     * Metoda updateGameState aktualizuje stan gry.
     * @param move Ruch gracza.
     */
    public synchronized void updateGameState(String move) {
        board.update(move);
        broadcastGameState();
    }
    /**
     * Metoda getPlayers zwraca listę graczy.
     * @return players - lista graczy.
     */
    public List<Observer> getObservers() {
        return observers;
    }
    /**
     * Metoda isRunning sprawdza, czy serwer jest uruchomiony.
     * @return true, jeśli serwer jest uruchomiony, w przeciwnym razie false.
     */
    public boolean isRunning() {
        return running;
    }
    /**
     * Metoda getDisconnectedPlayers zwraca rozłączonych graczy.
     * @return disconnectedPlayers - rozłączeni gracze.
     */
    public Set<Integer> getDisconnectedPlayers() {
        return disconnectedPlayers;
    }
    /**
     * Metoda main uruchamia serwer gry.
     * @param args Argumenty wywołania programu.
     */
    public static void main(String[] args) {
        ServerGUI gui = new ServerGUI();
        GameServer server = new GameServer(12345, gui);
        server.start();
    }
}
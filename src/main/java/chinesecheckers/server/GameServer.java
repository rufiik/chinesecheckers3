package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chinesecheckers.model.Game;
import chinesecheckers.model.Move;
import chinesecheckers.patterns.Observable;
import chinesecheckers.patterns.Observer;
import chinesecheckers.service.GameService;
/**
 * Klasa GameServer reprezentuje serwer gry.
 */
@Service
public class GameServer implements Observable {

    @Autowired
    private GameService gameService;
 
    private int port = 0;
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
    private String variant;
    private int botCount = 0;
    private Game currentGame;

    public GameServer() {
        this.board = new Board();
    }
    /**
     * Metoda initialize inicjalizuje serwer gry z podanym portem.
     * @param port Numer portu serwera.
     */
    public void initialize(int port) {
        this.port = port;
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
        try (Scanner scanner = new Scanner(System.in)) {
          System.out.println("Czy chcesz odtworzyć zapisaną grę? (1 - Tak, 2 - Nie): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                List<Game> games = gameService.getAllGames();
                if (games.isEmpty()) {
                    System.out.println("Brak zapisanych gier. Rozpoczynam nową grę.");
                    initializeNewGameSettings(scanner);
                } else {
                    System.out.println("Wybierz ID gry do odtworzenia:");
                    for (Game game : games) {
                        System.out.println("ID: " + game.getId() + ", Wariant: " + game.getVariant() + ", Liczba graczy: " + game.getMaxPlayers());
                    }
                    Long gameId = scanner.nextLong();
                    loadGame(gameId, serverSocket);
                }
            } else if (choice == 2) {
                initializeNewGameSettings(scanner);
                int humanPlayers = maxPlayers - botCount;
                if ("Order Out Of Chaos".equals(variant)) {
                    board.initializeBoardForChaos(maxPlayers);
                } else {
                    board.initializeBoardForPlayers(maxPlayers);
                }
                currentGame = new Game();
                currentGame.setVariant(variant);
                currentGame.setMaxPlayers(maxPlayers);
                currentGame.setHumanPlayers(humanPlayers);
                gameService.saveGame(currentGame);
                chinesecheckers.model.Board boardModel = new chinesecheckers.model.Board();
                boardModel.setState(board.toString());
                boardModel.setGame(currentGame);
                gameService.saveBoard(boardModel);
            } else {
                System.out.println("Nieprawidłowy wybór! Wybierz 1 lub 2.");
            }
        }
        int humanPlayers = maxPlayers - botCount;

        board.setMaxPlayers(maxPlayers);
        board.setVariant(variant);

   
        System.out.println("Oczekiwanie na graczy..."); 
        new Thread(() -> handleNewConnections(serverSocket, humanPlayers)).start();
        synchronized (players) {
            while (players.size() < humanPlayers) {
                try {
                    players.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Oczekiwanie na graczy przerwane.");
                }
            }
        }

        for (int i = 0; i < botCount; i++) {
            try {
                Socket socket = new Socket("localhost", port);
                BotPlayer bot = new BotPlayer(socket, nextPlayerId++, maxPlayers, variant);
                players.add(bot);
                addObserver(bot);
                System.out.println("Bot " + bot.getPlayerId() + " dołączył do gry.");
            } catch (IOException e) {
                e.printStackTrace();
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
 * Metoda initializeNewGameSettings inicjalizuje ustawienia nowej gry.
 * @param scanner Skaner.
 */
    private void initializeNewGameSettings(Scanner scanner) {
        while (true) {
            System.out.println("Wybierz wariant gry (1 - Klasyczny, 2 - Order Out Of Chaos): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            if (choice == 1) {
                variant = "Klasyczny";
                break;
            } else if (choice == 2) {
                variant = "Order Out Of Chaos";
                break;
            } else {
                System.out.println("Nieprawidłowy wybór! Wybierz 1 lub 2.");
            }
        }
        while (true) {
            System.out.println("Podaj liczbę graczy (2, 3, 4, 6): ");
            int inputPlayers = scanner.nextInt();
            if (inputPlayers == 2 || inputPlayers == 3 || inputPlayers == 4 || inputPlayers == 6) {
                maxPlayers = inputPlayers;
                break;
            } else {
                System.out.println("Niepoprawna liczba graczy! Wybierz 2, 3, 4 lub 6.");
            }
        }
        while (true) {
            System.out.println("Wybierz liczbę botów: ");
            int inputBots = scanner.nextInt();
            if (inputBots >= 0 && inputBots <= maxPlayers) {
                botCount = inputBots;
                break;
            } else {
                System.out.println("Niepoprawna liczba botów! Wybierz liczbę od 0 do " + maxPlayers + ".");
            }
        }
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
        String move =null;
        if (currentPlayer instanceof BotPlayer) {
            move = ((BotPlayer) currentPlayer).getMove();
        } else {
            move = currentPlayer.receiveMessage();
        }
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

                            Move moveEntity = new Move();
                            moveEntity.setStartX(startX);
                            moveEntity.setStartY(startY);
                            moveEntity.setEndX(endX);
                            moveEntity.setEndY(endY);
                            moveEntity.setPlayerId(playerId);
                            moveEntity.setGame(currentGame);
                            gameService.saveMove(moveEntity);

                            chinesecheckers.model.Board boardModel = gameService.getBoardByGame(currentGame);
                            boardModel.setState(board.toString());
                            gameService.saveBoard(boardModel);

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
    private void handleNewConnections(ServerSocket serverSocket, int humanPlayers) {
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
                    } else if (players.size() < humanPlayers) {
                        ClientHandler player = new ClientHandler(clientSocket, nextPlayerId++, maxPlayers, variant, false);
                        if (player.isConnected()) {
                            players.add(player);
                            addObserver(player);
                            System.out.println("Gracz " + player.getPlayerId() + " dołączył do gry.");
                            players.notifyAll();
                        } else {
                            System.out.println("Gracz " + player.getPlayerId() + " rozłączył się przed dołączeniem do gry.");
                        }
                        removeDisconnectedPlayersBeforeStart();
                    } else {
                        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            out.println("Maksymalna liczba graczy osiągnięta.");
                        } finally {
                            clientSocket.close();
                        }
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
        String gameState = board.toString();
        for (ClientHandler client : players) {
            client.sendMessage("Stan planszy:" + gameState);
            if (client instanceof BotPlayer) {
                ((BotPlayer) client).updateBoard(gameState);
            }
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
 * Metoda loadGame odtwarza grę o podanym ID.
 * @param gameId ID gry.
 * @param serverSocket Gniazdo serwera.
 * @throws IOException Wyjątek wejścia/wyjścia.
 */
    public void loadGame(Long gameId, ServerSocket serverSocket) throws IOException {
        while (true) {
            Game game = gameService.getGameById(gameId);
            if (game != null) {
                this.currentGame = game;
                this.variant = game.getVariant();
                this.maxPlayers = game.getMaxPlayers();
                this.botCount = maxPlayers - game.getHumanPlayers();
                this.board.setMaxPlayers(maxPlayers);
                this.board.setVariant(variant);
    
                System.out.println("Gra została załadowana.");
    
                // Odtworzenie stanu planszy

    
                // Inicjalizacja mapowania baz przeciwników
                board.initializeOpponentBaseMapping(maxPlayers);
    
                // Oczekiwanie na graczy
                int humanPlayers = maxPlayers - botCount;
                new Thread(() -> handleNewConnections(serverSocket, humanPlayers)).start();
                synchronized (players) {
                    while (players.size() < humanPlayers) {
                        try {
                            players.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Oczekiwanie na graczy przerwane.");
                        }
                    }
                }
                System.out.println(board.toString());
                chinesecheckers.model.Board boardState = gameService.getBoardByGame(game);
                if (boardState != null) {
                    board.loadState(boardState.getState());
                }
    
                broadcastGameState();
                gameStarted = true;
                break; // Wyjście z pętli po załadowaniu gry
            } else {
                System.out.println("Nie znaleziono gry o podanym ID. Spróbuj ponownie.");
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("Wybierz ID gry do odtworzenia:");
                    gameId = scanner.nextLong();
                }
            }
        }
    }
/**
 * Metoda saveBoardState zapisuje stan planszy.
 */
    public void saveBoardState() {
        chinesecheckers.model.Board boardModel = gameService.getBoardByGame(currentGame);
        if (boardModel != null) {
            boardModel.setState(board.toString());
            gameService.saveBoard(boardModel);
        }
    }
}
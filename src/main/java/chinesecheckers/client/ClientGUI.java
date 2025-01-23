package chinesecheckers.client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

import chinesecheckers.server.Board;
/**
 * Klasa ClientGUI reprezentuje interfejs graficzny klienta gry w chińskie warcaby.
 * Obsługuje wyświetlanie planszy, informacji o turze gracza oraz rankingów.
 */
public class ClientGUI extends JFrame {
    /**
     * Plansza do gry.
     */
    private Board board;
    /**
     * Etykieta informująca o turze gracza.
     */
    private JLabel turnLabel;
    /**
     * Etykieta informująca o kolorze gracza.
     */
    private JLabel colorLabel;
    /**
     * Kolor pionków gracza.
     */
    private int playerColor;
    /**
     * Panel planszy.
     */
    private BoardPanel boardPanel;
    /**
     * Klient gry.
     */
    private GameClient gameClient;
    /**
     * Przycisk pomijania tury.
     */
    private JButton skipButton;
    /**
     * Obszar tekstowy z rankingiem graczy.
     */
    private JTextArea standingsArea;
    /**
     * Lista rankingów graczy.
     */
    private List<String> standings;
/**
 * Konstruktor klasy ClientGUI.
 * @param board Instancja klasy Board reprezentująca planszę do gry.
 * @param playerColor Kolor pionków gracza.
 * @param gameClient Instancja klasy GameClient obsługująca komunikację z serwerem.
 */
    public ClientGUI(Board board, int playerColor, GameClient gameClient) {
        this.board = board;
        this.playerColor = playerColor;
        this.gameClient = gameClient;
        this.standings = new ArrayList<>();
    }
/**
 * Metoda initialize inicjalizuje interfejs graficzny klienta gry.
 */
    public void initialize() {
        setTitle("Chinese Checkers");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        turnLabel = new JLabel("Oczekiwanie na turę...");
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 20));
        turnLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        colorLabel = new JLabel("Twój kolor: " + getColorName(playerColor));
        colorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        colorLabel.setFont(new Font("Serif", Font.BOLD, 20));
        colorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        standingsArea = new JTextArea();
        standingsArea.setEditable(false);
        standingsArea.setFont(new Font("Serif", Font.PLAIN, 16));

        skipButton = new JButton("Pomiń turę");
        skipButton.setEnabled(false);
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipTurn();
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(turnLabel);
        topPanel.add(skipButton);

        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(new JLabel("Aktualny ranking: "), BorderLayout.NORTH);
        sidePanel.add(standingsArea, BorderLayout.CENTER);

        boardPanel = new BoardPanel(board, playerColor, gameClient);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(colorLabel, BorderLayout.SOUTH);
        mainPanel.add(sidePanel, BorderLayout.EAST);
            
        add(mainPanel);
        setVisible(true);        
    }
/**
 * Metoda showPlayerTurnMessage wyświetla informację o turze gracza.
 */
    public void showPlayerTurnMessage() {
        turnLabel.setText("Twoja tura!");
        boardPanel.setPlayerTurn(true);
        skipButton.setEnabled(true);
    }
/**
 *  Metoda endPlayerTurn kończy turę gracza.
 */
    public void endPlayerTurn() {
        turnLabel.setText("Oczekiwanie na turę...");
        boardPanel.setPlayerTurn(false);
        skipButton.setEnabled(false);
    }
/**
 * Metoda endGame kończy grę.
 */
    public void endGame() {
        turnLabel.setText("Koniec gry!");
        boardPanel.setPlayerTurn(false);
        skipButton.setEnabled(false);
    }
/**
 *  Metoda skipTurn pomija turę gracza. 
 */  
    private void skipTurn() {
        gameClient.skipTurn();
    }
/**
 * Metoda getColorName zwraca nazwę koloru gracza.
 * @param playerColor Kolor gracza.
 * @return Nazwa koloru gracza.
 */
    private String getColorName(int playerColor) {
        switch (playerColor) {
            case 1: return "Czerwony";
            case 2: return "Niebieski";
            case 3: return "Żółty";
            case 4: return "Zielony";
            case 5: return "Pomarańczowy";
            case 6: return "Fioletowy";
            default: return "Nieznany";
        }
    }
/**
 * Metoda updateStandings aktualizuje ranking graczy.
 * @param rankMessage Wiadomość z rankingiem graczy.
 * @throws NumberFormatException Jeśli nie udało się przekonwertować numeru gracza na liczbę.
 */
    public void updateStandings(String rankMessage) {
        String[] rankParts = rankMessage.split(" ");
        int playerId = Integer.parseInt(rankParts[1]);

        standings.add(getColorName(playerId));
        StringBuilder standingsText = new StringBuilder();
        for (String standing : standings) {
            standingsText.append(standing).append("\n");
        }
        standingsArea.setText(standingsText.toString());
    }
}
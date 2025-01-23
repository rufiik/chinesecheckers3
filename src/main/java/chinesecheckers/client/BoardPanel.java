package chinesecheckers.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

import chinesecheckers.server.Board;
/**
 * Klasa BoardPanel reprezentuje panel graficzny, na którym rysowana jest plansza do gry w chińskie warcaby.
 * Obsługuje również interakcje użytkownika, takie jak wybieranie i przeciąganie pionków.
 */
public class BoardPanel extends JPanel {
      /**
     * Plansza do gry.
     */
    private Board board;

    /**
     * Kolor pionków gracza.
     */
    private int playerColor;

    /**
     * Czy jest tura gracza.
     */
    private boolean isPlayerTurn = false;

    /**
     * Klient gry.
     */
    private GameClient gameClient;

    /**
     * Rozmiar komórki planszy.
     */
    private int cellSize = 30;

    /**
     * Szerokość planszy.
     */
    private int boardWidth;

    /**
     * Wysokość planszy.
     */
    private int boardHeight;

    /**
     * Współrzędna X początku planszy.
     */
    private int startX;

    /**
     * Współrzędna Y początku planszy.
     */
    private int startY;

    /**
     * Wiersz wybranego pionka.
     */
    private int selectedRow = -1;

    /**
     * Kolumna wybranego pionka.
     */
    private int selectedCol = -1;

    /**
     * Współrzędna X przeciąganego pionka.
     */
    private int draggedX = -1;

    /**
     * Współrzędna Y przeciąganego pionka.
     */
    private int draggedY = -1;

    /**
     * Czy pionek jest przeciągany.
     */
    private boolean dragging = false;
   /**
     * Konstruktor klasy BoardPanel.
     *
     * @param board       Instancja klasy Board reprezentująca planszę do gry.
     * @param playerColor Kolor pionków gracza.
     * @param gameClient  Instancja klasy GameClient obsługująca komunikację z serwerem.
     */
    public BoardPanel(Board board, int playerColor, GameClient gameClient) {
        this.board = board;
        this.playerColor = playerColor;
        this.gameClient = gameClient;
        
        calculateBoardDimensions();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isPlayerTurn) return;

                int col = (e.getX() - startX) / cellSize;
                int row = (e.getY() - startY) / cellSize;

                if (isWithinBoard(row, col)) {
                    int pieceColor = board.getBoard()[row][col];
                    if (pieceColor == playerColor) {
                        selectedRow = row;
                        selectedCol = col;
                        dragging = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isPlayerTurn) return;

                if (dragging) {
                    int col = (e.getX() - startX) / cellSize;
                    int row = (e.getY() - startY) / cellSize;

                    if (isWithinBoard(row, col) && board.getBoard()[row][col] == 0 && !(row == selectedRow && col == selectedCol)) {
                        if (board.isValidMove(selectedRow, selectedCol, row, col, playerColor)) {
                            board.movePiece(selectedRow, selectedCol, row, col, playerColor);
                            notifyMove(selectedRow, selectedCol, row, col);
                        }
                    }
                    
                    repaint();
                    selectedRow = -1;
                    selectedCol = -1;
                    dragging = false;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isPlayerTurn) return;

                if (dragging) {
                    draggedX = e.getX();
                    draggedY = e.getY();
                    repaint();
                }
            }
        });
    }
/**
 * Oblicza wymiary planszy na podstawie rozmiaru komórki i rozmiaru planszy.
 */
    private void calculateBoardDimensions() {
        boardWidth = board.getBoard()[0].length * cellSize;
        boardHeight = board.getBoard().length * cellSize;
        startX = (getWidth() - boardWidth) / 2;
        startY = (getHeight() - boardHeight) / 2;
    }
   /**
     * Sprawdza, czy podane współrzędne znajdują się w obrębie planszy.
     *
     * @param row Wiersz na planszy.
     * @param col Kolumna na planszy.
     * @return true, jeśli współrzędne znajdują się w obrębie planszy, w przeciwnym razie false.
     */
    private boolean isWithinBoard(int row, int col) {
        return row >= 0 && row < board.getBoard().length && col >= 0 && col < board.getBoard()[row].length;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculateBoardDimensions();
        drawBoard(g);

        if (dragging && selectedRow != -1 && selectedCol != -1) {
            g.setColor(getPieceColor(playerColor));
            g.fillOval(draggedX - cellSize / 2, draggedY - cellSize / 2, cellSize, cellSize);
        }
    }
   /**
     * Rysuje planszę do gry i pionki na niej.
     * @param g Obiekt Graphics używany do rysowania.
     */
    private void drawBoard(Graphics g) {
        for (int i = 0; i < board.getBoard().length; i++) {
            for (int j = 0; j < board.getBoard()[i].length; j++) {
                switch (board.getBoard()[i][j]) {
                    case 0:
                        g.setColor(Color.WHITE);
                        break;
                    case 7:
                        continue;
                    default:
                        g.setColor(getPieceColor(board.getBoard()[i][j]));
                        break;
                }
                g.fillOval(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);
            }
        }
        if ("Order Out Of Chaos".equals(gameClient.getVariant())) {
            for (int playerId = 1; playerId <= gameClient.getMaxPlayers(); playerId++) {
                Set<int[]> homeBasePositions = board.getHomeBasePositions(playerId);
                g.setColor(getPieceColor(playerId));
                for (int[] pos : homeBasePositions) {
                    g.drawOval(startX + pos[1] * cellSize, startY + pos[0] * cellSize, cellSize, cellSize);
                }
            }
        } else {
            Set<int[]> opponentBasePositions = board.getOpponentBasePositions(playerColor);
            g.setColor(getPieceColor(playerColor));
            for (int[] pos : opponentBasePositions) {
                g.drawOval(startX + pos[1] * cellSize, startY + pos[0] * cellSize, cellSize, cellSize);
            }
        }
    }

    /**
     * Zwraca kolor pionka na podstawie jego numeru.
     * @param pieceColor Numer koloru pionka.
     * @return Kolor pionka.
     */
    private Color getPieceColor(int pieceColor) {
        switch (pieceColor) {
            case 1:
                return Color.RED;
            case 2:
                return Color.BLUE;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.GREEN;
            case 5:
                return Color.ORANGE;
            case 6:
                return new Color(128, 0, 128);
            default:
                return Color.WHITE;
        }
    }
    /**
     * Ustawia, czy jest tura gracza.
     * @param isPlayerTurn true, jeśli jest tura gracza, w przeciwnym razie false.
     */
    public void setPlayerTurn(boolean isPlayerTurn) {
        this.isPlayerTurn = isPlayerTurn;
    }
   /**
     * Powiadamia serwer o wykonanym ruchu.
     *
     * @param startX Początkowa współrzędna X.
     * @param startY Początkowa współrzędna Y.
     * @param endX   Końcowa współrzędna X.
     * @param endY   Końcowa współrzędna Y.
     */
    private void notifyMove(int startX, int startY, int endX, int endY) {
        gameClient.sendMove(startX, startY, endX, endY);
    }
}

package chinesecheckers.server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import org.springframework.stereotype.Component;

/**
 * Klasa ServerGUI reprezentuje interfejs graficzny serwera gry w chińskie warcaby.
 */
@Component
public class ServerGUI {
    private int selectedPlayers;
    private String selectedVariant;
    private int selectedBots;
    private String gameChoice;
    private Long selectedGameId;
    private final JFrame frame;
    private final JTextArea logArea;
/**
 * Konstruktor klasy ServerGUI.
 */
    public ServerGUI() {
        frame = new JFrame("Chińskie warcaby - serwer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel choicePanel = createGameChoicePanel();
        JPanel variantPanel = createVariantPanel();
        JPanel playerSelectionPanel = createPlayerSelectionPanel();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        PrintStream printStream = new PrintStream(new CustomOutputStream(logArea));
        System.setOut(printStream);
        System.setErr(printStream);

        frame.add(choicePanel, BorderLayout.NORTH);
        frame.setVisible(true);

        for (java.awt.Component component : ((JPanel) choicePanel.getComponent(1)).getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.addActionListener((ActionEvent e) -> {
                    if (button.getText().equals("Wczytaj zapisaną grę")) {
                        gameChoice = button.getText();
                        synchronized (this) {
                            this.notify();
                        }
                        frame.getContentPane().remove(choicePanel);
                        frame.add(scrollPane, BorderLayout.CENTER);
                        frame.revalidate();
                        frame.repaint();
                    } else if (button.getText().equals("Rozpocznij nową grę")) {
                        gameChoice = button.getText();
                        synchronized (this) {
                            this.notify();
                        }
                        frame.getContentPane().remove(choicePanel);
                        frame.add(variantPanel, BorderLayout.NORTH);
                        frame.revalidate();
                        frame.repaint();
                    }
                });
            }
        }

        for (java.awt.Component component : ((JPanel) variantPanel.getComponent(1)).getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.addActionListener((ActionEvent e) -> {
                    selectedVariant = button.getText();
                    synchronized (this) {
                        this.notify();
                    }
                    frame.getContentPane().remove(variantPanel);
                    frame.add(playerSelectionPanel, BorderLayout.NORTH);
                    frame.revalidate();
                    frame.repaint();
                });
            }
        }

        for (java.awt.Component component : ((JPanel) playerSelectionPanel.getComponent(1)).getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.addActionListener((ActionEvent e) -> {
                    selectedPlayers = Integer.parseInt(button.getText());
                    synchronized (this) {
                        this.notify();
                    }
                    JPanel botSelectionPanel = createBotSelectionPanel();
                    frame.getContentPane().remove(playerSelectionPanel);
                    frame.add(botSelectionPanel, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();

                    for (java.awt.Component botComponent : ((JPanel) botSelectionPanel.getComponent(1)).getComponents()) {
                        if (botComponent instanceof JButton) {
                            JButton botButton = (JButton) botComponent;
                            botButton.addActionListener((ActionEvent ev) -> {
                                selectedBots = Integer.parseInt(botButton.getText());
                                synchronized (this) {
                                    this.notify();
                                }
                                frame.getContentPane().remove(botSelectionPanel);
                                frame.add(scrollPane, BorderLayout.CENTER);
                                frame.revalidate();
                                frame.repaint();
                            });
                        }
                    }
                });
            }
        }      
    }
/**
 * Metoda createVariantPanel tworzy panel wyboru wariantu gry.
 * @return variantPanel - panel wybranego wariantu gry
 */
    private JPanel createVariantPanel() {
        JPanel variantPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Wybierz wariant gry:", SwingConstants.CENTER);
        label.setFont(new Font("FF DIN", Font.BOLD, 36));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        variantPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        String[] variants = {"Rozgrywka klasyczna", "Order Out Of Chaos"};
        for (String variant : variants) {
            JButton button = new JButton(variant);
            button.setFont(new Font("Serif", Font.BOLD, 28));
            button.setBackground(new Color(35, 65, 225));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setMaximumSize(new Dimension(300, 100));
            button.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            buttonPanel.add(button);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        variantPanel.add(buttonPanel, BorderLayout.CENTER);
        return variantPanel;
    }
/**
 * Metoda createPlayerSelectionPanel tworzy panel wyboru liczby graczy.
 * @return playerSelectionPanel - panel wybranej liczby graczy
 */
    private JPanel createPlayerSelectionPanel() {
        JPanel playerSelectionPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Wybierz liczbę graczy:", SwingConstants.CENTER);
        label.setFont(new Font("FF DIN", Font.BOLD, 36));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        playerSelectionPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        int[] playerOptions = {2, 3, 4, 6};
        Color[] buttonColors = {new Color(139, 0, 0), new Color(0, 100, 0), new Color(0, 0, 139), new Color(255, 140, 0)};
        for (int i = 0; i < playerOptions.length; i++) {
            int option = playerOptions[i];
            JButton button = new JButton(String.valueOf(option));
            button.setFont(new Font("Serif", Font.BOLD, 40));
            button.setBackground(buttonColors[i]);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setPreferredSize(new Dimension(80, 100));
            buttonPanel.add(button);
        }

        playerSelectionPanel.add(buttonPanel, BorderLayout.CENTER);
        return playerSelectionPanel;
    }
/**
 * Metoda createBotSelectionPanel tworzy panel wyboru liczby botów.
 * @return botSelectionPanel - panel wybranej liczby botów
 */
    private JPanel createBotSelectionPanel() {
        JPanel botSelectionPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Wybierz liczbę botów:", SwingConstants.CENTER);
        label.setFont(new Font("FF DIN", Font.BOLD, 36));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        botSelectionPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        int maxBots = selectedPlayers;
        Color[] buttonColors = {new Color(139, 0, 0), new Color(0, 100, 0), new Color(0, 0, 139), new Color(255, 140, 0), new Color(139, 0, 139), new Color(60, 40, 20), new Color(69, 69, 69)};
        for (int i = 0; i < maxBots; i++) {
            JButton button = new JButton(String.valueOf(i));
            button.setFont(new Font("Serif", Font.BOLD, 40));
            button.setBackground(buttonColors[i]);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setPreferredSize(new Dimension(80, 100));
            buttonPanel.add(button);
        }

        botSelectionPanel.add(buttonPanel, BorderLayout.CENTER);
        return botSelectionPanel;
    }
/**
 * Metoda createGameChoicePanel tworzy panel wyboru czy chcemy wczytać grę czy rozpocząć nową.
 * @return gameChoicePanel - panel wyboru
 */
    private JPanel createGameChoicePanel() {
        JPanel gameChoicePanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Chińskie Warcaby", SwingConstants.CENTER);
        label.setFont(new Font("Comic Sans MS", Font.BOLD, 48));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        gameChoicePanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        String[] choices = {"Rozpocznij nową grę", "Wczytaj zapisaną grę"};
        for (String choice : choices) {
            JButton button = new JButton(choice);
            button.setFont(new Font("Serif", Font.BOLD, 28));
            button.setBackground(new Color(35, 65, 225));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setMaximumSize(new Dimension(300, 100));
            button.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            buttonPanel.add(button);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        gameChoicePanel.add(buttonPanel, BorderLayout.CENTER);
        return gameChoicePanel;
    }
/**
 * Metoda openSelectionDialog otwiera okno dialogowe do wprowadzenia ID gry do wczytania.
 * @return selectedGameId - wybrane ID gry
 */
    public Long openSelectionDialog() {
        String input = JOptionPane.showInputDialog(null, "Podaj ID gry do wczytania:", "Wprowadź wartość", JOptionPane.QUESTION_MESSAGE);

        if (input != null) {
            try {
                selectedGameId = Long.parseLong(input);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Wprowadź prawidłowe ID.", "Błąd", JOptionPane.ERROR_MESSAGE);
                openSelectionDialog();
            }
        } else {
            System.out.println("Użytkownik anulował wprowadzenie ID.");
        }

        return selectedGameId;
    }
/**
 * Metoda getSelectedPlayers zwraca wybraną liczbę graczy.
 * @return selectedPlayers - wybrana liczba graczy
 */
    public synchronized int getSelectedPlayers() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return selectedPlayers;
    }
/**
 * Metoda getSelectedVariant zwraca wybrany wariant gry.
 * @return selectedVariant - wybrany wariant gry
 */
    public synchronized String getSelectedVariant() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return selectedVariant;
    }
/**
 * Metoda getSelectedBots zwraca wybraną liczbę botów.
 * @return selectedBots - wybrana liczba botów
 */
    public int getSelectedBots() {
        return selectedBots;
    }

/**
 * Metoda getGameChoice zwraca czy chemy wczytać grę czy rozpocząć nową.
 * @return gameChoice - nasz wybór
 */
    public synchronized String getGameChoice() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return gameChoice;
    }
/**
 * Metoda waitForWindowClose oczekuje na zamknięcie okna.
 */
    public void waitForWindowClose() {
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                synchronized (ServerGUI.this) {
                    ServerGUI.this.notify();
                }
            }
        });

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Serwer został przerwany podczas oczekiwania na zamknięcie okna.");
            }
        }
    }
/**
 * Klasa CustomOutputStream reprezentuje niestandardowy strumień wyjściowy.
 */
    class CustomOutputStream extends OutputStream {
        private final JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String text = new String(b, off, len, StandardCharsets.UTF_8);
            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }
}
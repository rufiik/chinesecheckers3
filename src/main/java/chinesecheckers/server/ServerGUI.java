package chinesecheckers.server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
/**
 * Klasa ServerGUI reprezentuje interfejs graficzny serwera gry w chińskie warcaby.
 */
public class ServerGUI {
    private int selectedPlayers;
    private String selectedVariant;
    private final JFrame frame;
    private final JTextArea logArea;
/**
 * Konstruktor klasy ServerGUI.
 */
    public ServerGUI() {
        frame = new JFrame("Chińskie warcaby - serwer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel variantPanel = createVariantPanel();
        JPanel playerSelectionPanel = createPlayerSelectionPanel();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        PrintStream printStream = new PrintStream(new CustomOutputStream(logArea));
        System.setOut(printStream);
        System.setErr(printStream);

        frame.add(variantPanel, BorderLayout.NORTH);
        frame.setVisible(true);

        for (Component component : ((JPanel) variantPanel.getComponent(1)).getComponents()) {
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

        for (Component component : ((JPanel) playerSelectionPanel.getComponent(1)).getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.addActionListener((ActionEvent e) -> {
                    selectedPlayers = Integer.parseInt(button.getText());
                    synchronized (this) {
                        this.notify();
                    }
                    frame.getContentPane().remove(playerSelectionPanel);
                    frame.add(scrollPane, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();
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
        label.setFont(new Font("FF DIN", Font.BOLD, 30));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        variantPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        String[] variants = {"Rozgrywka klasyczna", "Order Out Of Chaos"};
        for (String variant : variants) {
            JButton button = new JButton(variant);
            button.setFont(new Font("Serif", Font.BOLD, 30));
            button.setBackground(new Color(35, 65, 225));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            buttonPanel.add(button);
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
        label.setFont(new Font("FF DIN", Font.BOLD, 30));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        playerSelectionPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
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
            buttonPanel.add(button);
        }

        playerSelectionPanel.add(buttonPanel, BorderLayout.CENTER);
        return playerSelectionPanel;
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
package memorycard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

public class MemoryCard extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MemoryCard::new);
    }

    private final int rows = 3;
    private final int cols = 4;
    private final ArrayList<Color> colors = new ArrayList<>();
    private final JButton[][] buttons = new JButton[rows][cols];
    private final Color[] colorSet = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN};
    private final int[][] numbers = new int[rows][cols]; 

    private JButton firstSelected = null;
    private JButton secondSelected = null;
    private Timer flipBackTimer;
    private int matchesFound = 0;
    private int attemptsLeft = 15;
    private Timer gameTimer;
    private int timeLeft = 60;

    private JLabel attemptsLabel;
    private JLabel timerLabel;

    public MemoryCard() {
        setupColors();
        setupUI();
        startGameTimer();
        printMatchingPairs(); 
    }

    private void setupColors() {
        for (Color color : colorSet) {
            colors.add(color);
            colors.add(color);
        }
        Collections.shuffle(colors);
        
        
        int number = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                numbers[i][j] = number++;
            }
        }
    }

    private void setupUI() {
        setTitle("MemoryCard");  
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        int colorIndex = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(100, 100)); 
                button.putClientProperty("color", colors.get(colorIndex++));
                button.setBackground(Color.LIGHT_GRAY);
                button.addActionListener(new ButtonClickListener());
                buttons[i][j] = button;
                gridPanel.add(button);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        attemptsLabel = new JLabel("Attempts Left: " + attemptsLeft);
        timerLabel = new JLabel("Time Left: " + timeLeft + "s");
        infoPanel.add(attemptsLabel);
        infoPanel.add(timerLabel);
        add(infoPanel, BorderLayout.SOUTH);

        setVisible(true);

        
        showAnswersTemporarily();
    }

    private void showAnswersTemporarily() {
       
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Color color = (Color) buttons[i][j].getClientProperty("color");
                buttons[i][j].setBackground(color);
            }
        }

        
        Timer hideColorsTimer = new Timer(5000, (ActionEvent e) -> {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    buttons[i][j].setBackground(Color.LIGHT_GRAY);
                }
            }
        });
        hideColorsTimer.setRepeats(false);  
        hideColorsTimer.start();
    }

    private void startGameTimer() {
        gameTimer = new Timer(1000, (ActionEvent e) -> {
            timeLeft--;
            timerLabel.setText("Time Left: " + timeLeft + "s");

            if (timeLeft <= 0) {
                gameTimer.stop();
                endGame(false);
            }
        });
        gameTimer.start();
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();

            if (clickedButton.getBackground() != Color.LIGHT_GRAY || (firstSelected != null && secondSelected != null)) {
                return;
            }

            Color color = (Color) clickedButton.getClientProperty("color");
            clickedButton.setBackground(color);

            if (firstSelected == null) {
                firstSelected = clickedButton;
            } else {
                secondSelected = clickedButton;

                Color firstColor = (Color) firstSelected.getClientProperty("color");
                Color secondColor = (Color) secondSelected.getClientProperty("color");

                
                if (firstColor.equals(secondColor)) {
                    firstSelected.setEnabled(false);
                    secondSelected.setEnabled(false);
                    firstSelected = null;
                    secondSelected = null;
                    matchesFound++;

                    if (matchesFound == (rows * cols) / 2) {
                        endGame(true);
                    }
                } else {
                    flipBackTimer = new Timer(1000, (ActionEvent e1) -> {
                        firstSelected.setBackground(Color.LIGHT_GRAY);
                        secondSelected.setBackground(Color.LIGHT_GRAY);
                        firstSelected = null;
                        secondSelected = null;
                        flipBackTimer.stop();
                    });
                    flipBackTimer.start();
                }
                attemptsLeft--;
                attemptsLabel.setText("Attempts Left: " + attemptsLeft);

                if (attemptsLeft <= 0) {
                    endGame(false);
                }
            }
        }
    }

    private void endGame(boolean won) {
        gameTimer.stop();
        String message = won ? "Congratulations! You've matched all pairs!" : "Game Over! Try again.";
        JOptionPane.showMessageDialog(this, message);
        resetGame();
    }

    private void resetGame() {
        matchesFound = 0;
        attemptsLeft = 15;
        timeLeft = 60;
        firstSelected = null;
        secondSelected = null;
        colors.clear();
        setupColors();

        int colorIndex = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j].setBackground(Color.LIGHT_GRAY);
                buttons[i][j].setEnabled(true);
                buttons[i][j].putClientProperty("color", colors.get(colorIndex++));
            }
        }
        attemptsLabel.setText("Attempts Left: " + attemptsLeft);
        timerLabel.setText("Time Left: " + timeLeft + "s");
        gameTimer.start();
    }

    private int getButtonNumber(JButton button) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (buttons[i][j] == button) {
                    return numbers[i][j];
                }
            }
        }
        return -1; 
    }

    
    private void printMatchingPairs() {
        System.out.println("Matching Pairs:");
        ArrayList<String> matchedPairs = new ArrayList<>();
        
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Color color = (Color) buttons[i][j].getClientProperty("color");
                for (int x = i; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        if (x == i && y <= j) continue; 
                        Color compareColor = (Color) buttons[x][y].getClientProperty("color");
                        if (color.equals(compareColor)) {
                            matchedPairs.add("(" + numbers[i][j] + " - " + numbers[x][y] + ")");
                        }
                    }
                }
            }
        }

        
        for (String pair : matchedPairs) {
            System.out.println(pair);
        }
    }
}

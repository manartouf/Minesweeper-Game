package com.mycompany.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MineSweeper {
    private class MineTile extends JButton {
        int r, c;
        boolean isRevealed = false;
        boolean isFlagged = false;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
            setText("?");
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        }
    }

    int tileSize = 70;
    int numRows = 8, numCols = 8, mineCount = 10;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel("Welcome to Minesweeper!");
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    Timer swingTimer;
    int elapsedTime = 0;

    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;
    boolean timerStarted = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MineSweeper().startGame());
    }

    void startGame() {
        showInstructions();
        setupFrame();
        setupBoard();
        setMines();
    }

    void setupFrame() {
        frame.setSize(boardWidth, boardHeight + 100);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 20));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setOpaque(true);
        textLabel.setBackground(Color.LIGHT_GRAY);
        textLabel.setText("Time: 0s | Mines: " + mineCount);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel, BorderLayout.CENTER);

        swingTimer = new Timer(1000, e -> {
            elapsedTime++;
            textLabel.setText("Time: " + elapsedTime + "s | Mines: " + mineCount);
        });

        frame.setVisible(true);
    }

    void showInstructions() {
        JOptionPane.showMessageDialog(frame,
                "Instructions:\n- Left-click to reveal a tile.\n- Right-click to flag/unflag.\n- Avoid the bombs!\n- Clear all safe tiles to win.\n\nGood luck!",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    void setupBoard() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;
                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));

                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver || tile.isRevealed) return;

                        if (!timerStarted) {
                            swingTimer.start();
                            timerStarted = true;
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (!tile.isEnabled()) return;
                            if (tile.getText().equals("?")) {
                                tile.setText("🚩");
                                tile.isFlagged = true;
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("?");
                                tile.isFlagged = false;
                            }
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (tile.isFlagged) return;
                            if (mineList.contains(tile)) {
                                revealMines(tile);
                                loseGame();
                            } else {
                                revealTile(tile.r, tile.c);
                                checkWin();
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
    }

    void setMines() {
        mineList = new ArrayList<>();
        int mineLeft = mineCount;

        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    void revealTile(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return;

        MineTile tile = board[r][c];
        if (!tile.isEnabled() || tile.isRevealed || tile.isFlagged) return;

        tile.isRevealed = true;
        tile.setEnabled(false);
        tilesClicked++;

        int minesAround = countMine(r - 1, c - 1) + countMine(r - 1, c) + countMine(r - 1, c + 1)
                + countMine(r, c - 1) + countMine(r, c + 1)
                + countMine(r + 1, c - 1) + countMine(r + 1, c) + countMine(r + 1, c + 1);

        if (minesAround > 0) {
            tile.setText(Integer.toString(minesAround));
        } else {
            tile.setText("");
            revealTile(r - 1, c - 1); revealTile(r - 1, c); revealTile(r - 1, c + 1);
            revealTile(r, c - 1); revealTile(r, c + 1);
            revealTile(r + 1, c - 1); revealTile(r + 1, c); revealTile(r + 1, c + 1);
        }
    }

    void revealMines(MineTile triggered) {
        for (MineTile tile : mineList) {
            tile.setText("💣");
            tile.setEnabled(false);
        }
        triggered.setBackground(Color.RED);
    }

    void checkWin() {
        if (tilesClicked == numRows * numCols - mineList.size()) {
            swingTimer.stop();
            gameOver = true;
            textLabel.setText("You Win! Time: " + elapsedTime + "s | Mines: " + mineCount);
            int option = JOptionPane.showConfirmDialog(frame,
                    "You Win! 🎉\nTime: " + elapsedTime + "s\nPlay Again?",
                    "Victory", JOptionPane.YES_NO_OPTION);
            handleReplay(option);
        }
    }

    void loseGame() {
        swingTimer.stop();
        gameOver = true;
        textLabel.setText("Game Over! You hit a mine. | Time: " + elapsedTime + "s | Mines: " + mineCount);
        int option = JOptionPane.showConfirmDialog(frame,
                "Boom! 💥 You hit a mine.\nPlay Again?",
                "Game Over", JOptionPane.YES_NO_OPTION);
        handleReplay(option);
    }

    void handleReplay(int option) {
        if (option == JOptionPane.YES_OPTION) {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new MineSweeper().startGame());
        } else {
            System.exit(0);
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) return 0;
        return mineList.contains(board[r][c]) ? 1 : 0;
    }
}

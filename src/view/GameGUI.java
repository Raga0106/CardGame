package view;

import controller.GameController;
import model.Card;
import service.BattleService.BattleResult;
import database.GameRecordService; // Import GameRecordService

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * GUI-based interface for the game using Swing.
 */
public class GameGUI extends JFrame {
    private final GameController gameController;
    private JTextArea gameLog;
    private JPanel cardPanel;
    private JLabel scoreLabel;
    private JLabel roundLabel;
    private JPanel computerCardPanel;
    private final JPanel mainPanel; // Main panel with CardLayout
    private final JPanel drawCardPanel; // Panel for card drawing
    private final JPanel battlePanel; // Panel for battle
    private final JPanel loginPanel; // Panel for login and registration
    private final JPanel lobbyPanel; // Panel for the game lobby
    private final JPanel drawOptionsPanel; // Panel for choosing single or ten draw
    private final JPanel selectionPanel; // Panel for selecting battle cards
    private DefaultListModel<String> deckListModel;
    private JList<String> deckList;
    private GameRecordService recordService;  // Database service for users and records
    private String currentUser; // Track logged-in user

    /**
     * Constructor for GameGUI.
     */
    public GameGUI() {
        gameController = new GameController();
        gameController.startGame();
        recordService = new GameRecordService(); // init database and tables

        setTitle("Card Clash: Elemental Gacha Arena");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel with CardLayout
        mainPanel = new JPanel(new CardLayout());
        add(mainPanel);

        // Initialize components
        scoreLabel = new JLabel("Player: 0 | Computer: 0", SwingConstants.CENTER);
        roundLabel = new JLabel("Round: 1/10", SwingConstants.CENTER);
        gameLog = new JTextArea();
        cardPanel = new JPanel();
        computerCardPanel = new JPanel();

        // Initialize login panel
        loginPanel = new JPanel(new BorderLayout());
        initializeLoginPanel();

        // Initialize lobby panel
        lobbyPanel = new JPanel(new GridLayout(3, 1));
        initializeLobbyPanel();

        drawOptionsPanel = new JPanel(new GridLayout(3, 1));
        initializeDrawOptionsPanel();

        // Initialize draw card panel
        drawCardPanel = new JPanel(new BorderLayout());
        initializeDrawCardPanel();

        // Initialize battle panel
        battlePanel = new JPanel(new BorderLayout());
        initializeBattlePanel();

        selectionPanel = new JPanel(new BorderLayout());
        initializeSelectionPanel();

        // Add panels to main panel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(lobbyPanel, "Lobby");
        mainPanel.add(drawOptionsPanel, "DrawOptions");
        mainPanel.add(drawCardPanel, "DrawCard");
        mainPanel.add(selectionPanel, "SelectBattleCards");
        mainPanel.add(battlePanel, "Battle");

        // Show login panel first
        showLoginPanel();
    }

    private void initializeLoginPanel() {
        JLabel titleLabel = new JLabel("Welcome to Card Clash!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loginPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            String user = JOptionPane.showInputDialog(this, "Enter username:");
            if (user != null && !user.isEmpty()) {
                String pass = JOptionPane.showInputDialog(this, "Enter password:");
                if (pass != null && !pass.isEmpty()) {
                    if (recordService.registerUser(user, pass)) {
                        JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String user = JOptionPane.showInputDialog(this, "Username:");
            String pass = JOptionPane.showInputDialog(this, "Password:");
            if (user != null && pass != null) {
                if (recordService.loginUser(user, pass)) {
                    currentUser = user; // 記錄當前登入用戶
                    gameController.loadPlayerDeck(currentUser, recordService); // 從資料庫載入卡片庫
                    showLobbyPanel();
                } else {
                    JOptionPane.showMessageDialog(this, "Login failed. Check credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        loginPanel.add(buttonPanel, BorderLayout.CENTER);
    }

    private void initializeLobbyPanel() {
        // Build lobby components based on login state
        lobbyPanel.setLayout(new GridLayout(0, 1));
        JLabel titleLabel = new JLabel("Game Lobby", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        lobbyPanel.add(titleLabel);

        JButton drawEntry = new JButton("抽卡");
        drawEntry.addActionListener(e -> showDrawOptionsPanel());
        lobbyPanel.add(drawEntry);

        JButton battleButton = new JButton("Battle");
        battleButton.addActionListener(e -> showSelectionPanel());
        lobbyPanel.add(battleButton);

        JButton checkDeckButton = new JButton("Check Deck");
        checkDeckButton.addActionListener(e -> showDeck());
        lobbyPanel.add(checkDeckButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            JOptionPane.showMessageDialog(this, "You have been logged out.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            showLoginPanel();
        });
        lobbyPanel.add(logoutButton);

        // If admin is logged in, add database initialize button
        if ("admin".equals(currentUser)) {
            JButton initDbButton = new JButton("Initialize Database");
            initDbButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all game records?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabase(currentUser)) {
                        JOptionPane.showMessageDialog(this, "Database cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to clear database.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            lobbyPanel.add(initDbButton);
        }

        // Add a button for admin to clear database by type
        if ("admin".equals(currentUser)) {
            JButton clearRecordsButton = new JButton("Clear All Records");
            clearRecordsButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all game records?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabaseByType(currentUser, "records")) {
                        JOptionPane.showMessageDialog(this, "All game records cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to clear game records.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            JButton clearCardsButton = new JButton("Clear All Card Libraries");
            clearCardsButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all card libraries?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabaseByType(currentUser, "cards")) {
                        JOptionPane.showMessageDialog(this, "All card libraries cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to clear card libraries.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            lobbyPanel.add(clearRecordsButton);
            lobbyPanel.add(clearCardsButton);
        }
    }

    private void initializeDrawOptionsPanel() {
        JButton singleDraw = new JButton("單抽");
        singleDraw.addActionListener(e -> {
            Card newCard = gameController.drawCard();
            recordService.saveCardToDeck(currentUser, newCard); // 保存卡片到資料庫
            String msg = String.format("You drew: %s (%s %s, Type: %s, Power: %d)\n%s", 
                newCard.getName(), newCard.getRarity(), newCard.getAttribute(), newCard.getType(), newCard.getBasePower(), newCard.getDescription());
            JOptionPane.showMessageDialog(this, msg, "單抽結果", JOptionPane.INFORMATION_MESSAGE);
        });
        JButton tenDraw = new JButton("十連抽");
        tenDraw.addActionListener(e -> {
            List<Card> newCards = gameController.drawMultiple(10);
            for (Card card : newCards) {
                recordService.saveCardToDeck(currentUser, card); // 保存每張卡片到資料庫
            }
            updateCardButtons();
            showDrawCardPanel();
        });
        drawOptionsPanel.add(singleDraw);
        drawOptionsPanel.add(tenDraw);
        // Add Back to Lobby button
        JButton backToLobby = new JButton("返回大廳");
        backToLobby.addActionListener(e -> showLobbyPanel());
        drawOptionsPanel.add(backToLobby);
    }

    private void initializeDrawCardPanel() {
        // Use dynamic panel rebuilding
        showDrawCardPanel();
    }

    private void initializeBattlePanel() {
        // Top panel for score and round display
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        scoreLabel = new JLabel("Player: 0 | Computer: 0", SwingConstants.CENTER);
        roundLabel = new JLabel("Round: 1/10", SwingConstants.CENTER);
        topPanel.add(scoreLabel);
        topPanel.add(roundLabel);
        JButton backToLobbyFromBattle = new JButton("Back to Lobby");
        backToLobbyFromBattle.addActionListener(e -> showLobbyPanel());
        topPanel.add(backToLobbyFromBattle);
        battlePanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for game log
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(gameLog);
        battlePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for player cards
        cardPanel = new JPanel();
        cardPanel.setLayout(new GridLayout(2, 5));
        battlePanel.add(cardPanel, BorderLayout.SOUTH);

        // Left panel for computer card display
        computerCardPanel = new JPanel();
        computerCardPanel.setLayout(new GridLayout(1, 1));
        computerCardPanel.setBorder(BorderFactory.createTitledBorder("Computer's Card"));
        battlePanel.add(computerCardPanel, BorderLayout.WEST);

        updateCardButtons();
    }

    private void updateCardButtons() {
        cardPanel.removeAll();
        int index = 0;
        for (Card card : gameController.getPlayerCards()) {
            // Format button text using HTML for better readability
            String buttonText = String.format("<html><center>%s<br>%s %s<br>Power: %d</center></html>",
                    card.getName(),
                    card.getRarity(),
                    card.getAttribute(),
                    card.getBasePower());
            JButton cardButton = new JButton(buttonText);
            int cardIndex = index++;
            cardButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Disable the button after clicking to prevent re-use
                    ((JButton)e.getSource()).setEnabled(false);
                    playRound(cardIndex);
                }
            });
            cardPanel.add(cardButton);
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void playRound(int playerCardIndex) {
        try {
            // Get player and computer cards before the battle for logging
            Card playerCard = gameController.getPlayerCards().get(playerCardIndex); // Get the card before removing it
            Card computerCard = gameController.getComputerCards().get(0); // Assuming computer picks first

            BattleResult result = gameController.playRound(playerCardIndex);

            // Update computer card display
            computerCardPanel.removeAll();
            String computerCardText = String.format("<html><center>%s<br>%s %s<br>Power: %d</center></html>",
                    computerCard.getName(),
                    computerCard.getRarity(),
                    computerCard.getAttribute(),
                    computerCard.getBasePower());
            JLabel computerCardLabel = new JLabel(computerCardText, SwingConstants.CENTER);
            computerCardPanel.add(computerCardLabel);
            computerCardPanel.revalidate();
            computerCardPanel.repaint();

            // Log more detailed battle information with correct round number
            int logRound = 10 - gameController.getPlayerCards().size();
            gameLog.append(String.format("\nRound %d:\n", logRound));
            gameLog.append(String.format("Player plays: %s (%s %s, Power: %d)\n",
                    playerCard.getName(), playerCard.getRarity(), playerCard.getAttribute(), playerCard.getBasePower()));
            gameLog.append(String.format("Computer plays: %s (%s %s, Power: %d)\n",
                    computerCard.getName(), computerCard.getRarity(), computerCard.getAttribute(), computerCard.getBasePower())); // Log computer card details
            gameLog.append(result + "\n"); // Append the winner/loser info

            // Refresh card buttons after the round
            updateCardButtons();

            // Update score
            scoreLabel.setText(String.format("Player: %d | Computer: %d",
                    gameController.getPlayerScore(), gameController.getComputerScore()));
            // Only update round number if there are remaining cards
            if (!gameController.getPlayerCards().isEmpty()) {
                int currentRound = 10 - gameController.getPlayerCards().size() + 1; // Correct round calculation
                roundLabel.setText(String.format("Round: %d/10", currentRound));
            }

            if (gameController.getPlayerCards().isEmpty()) {
                endGame();
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) { // Catch potential index errors too
            JOptionPane.showMessageDialog(this, "Invalid card selection or card already played.", "Error", JOptionPane.ERROR_MESSAGE);
            // Re-enable the specific button if an error occurred
            if (playerCardIndex >= 0 && playerCardIndex < cardPanel.getComponentCount()) {
                 Component button = cardPanel.getComponent(playerCardIndex);
                 if (button instanceof JButton) {
                     ((JButton) button).setEnabled(true);
                 }
            }
        }
    }

    private void endGame() {
        String winner = gameController.determineWinner();
        String finalScore = String.format("Final Score - Player: %d, Computer: %d",
                gameController.getPlayerScore(), gameController.getComputerScore());

        gameLog.append("\nGame Over!\n");
        gameLog.append(finalScore + "\n");
        gameLog.append("Winner: " + winner + "\n");

        // Also save the record to the database
        // 修正 saveRecord 調用，使用 currentUser 作為 username
        recordService.saveRecord(currentUser, "Player", gameController.getPlayerScore(), gameController.getComputerScore());
        gameLog.append("Game record saved.\n");


        JOptionPane.showMessageDialog(this, "Game Over! Winner: " + winner + "\n" + finalScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        // Disable all card buttons at the end
        for (Component comp : cardPanel.getComponents()) {
            comp.setEnabled(false);
        }
    }

    private void addRestartButton() {
        JButton restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> {
            gameController.startGame();
            gameLog.setText("");
            scoreLabel.setText("Player: 0 | Computer: 0");
            roundLabel.setText("Round: 1/10");
            updateCardButtons();
        });
        add(restartButton, BorderLayout.EAST);
    }

    private void addHistoryButton() {
        JButton historyButton = new JButton("View History");
        historyButton.addActionListener(e -> {
            // 使用 GameRecordService 的方法來查詢對戰紀錄
            List<String> records = recordService.getAllRecords(currentUser);
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found for user: " + currentUser, "Game History", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder history = new StringBuilder("Game History:\n");
                for (String record : records) {
                    history.append(record).append("\n");
                }
                JOptionPane.showMessageDialog(this, history.toString(), "Game History", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(historyButton, BorderLayout.WEST);
    }

    /**
     * Displays the player's current deck with full details and counts for duplicates.
     */
    private void showDeck() {
        List<Card> deck = gameController.getPlayerDeck();
        // Aggregate by card name, preserving draw order
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, Card> repMap = new LinkedHashMap<>();
        for (Card c : deck) {
            countMap.put(c.getName(), countMap.getOrDefault(c.getName(), 0) + 1);
            repMap.putIfAbsent(c.getName(), c);
        }
        StringBuilder sb = new StringBuilder("Your Deck:\n");
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Card rep = repMap.get(entry.getKey());
            int cnt = entry.getValue();
            sb.append(String.format("%s x%d (%s %s, Type: %s, Power: %d)\n  %s\n", 
                rep.getName(), cnt, rep.getRarity(), rep.getAttribute(), rep.getType(), rep.getBasePower(), rep.getDescription()));
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Deck Contents", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoginPanel() {
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "Login");
    }

    private void showLobbyPanel() {
        // Rebuild lobby panel to reflect current user and admin rights
        lobbyPanel.removeAll();
        initializeLobbyPanel();
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "Lobby");
    }

    private void showDrawOptionsPanel() {
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "DrawOptions");
    }

    private void showDrawCardPanel() {
        // 動態重建抽卡面板以顯示最新卡片
        drawCardPanel.removeAll();
        // Title
        JLabel titleLabel = new JLabel("Draw Your Cards!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        drawCardPanel.add(titleLabel, BorderLayout.NORTH);
        // Card display
        JPanel cardDisplayPanel = new JPanel(new GridLayout(2, 5));
        for (Card card : gameController.getPlayerCards()) {
            String cardText = String.format(
                "<html><center>%s<br>%s %s<br>Type: %s<br>Power: %d<br><i>%s</i></center></html>",
                card.getName(), card.getRarity(), card.getAttribute(), card.getType(), card.getBasePower(), card.getDescription());
            JLabel cardLabel = new JLabel(cardText, SwingConstants.CENTER);
            cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardDisplayPanel.add(cardLabel);
        }
        drawCardPanel.add(cardDisplayPanel, BorderLayout.CENTER);
        // Control buttons
        JButton startGameButton = new JButton("Start Game");
        startGameButton.addActionListener(e -> showBattlePanel());
        JButton backToLobbyFromDraw = new JButton("Back to Lobby");
        backToLobbyFromDraw.addActionListener(e -> showLobbyPanel());
        JPanel drawSouth = new JPanel();
        drawSouth.add(startGameButton);
        drawSouth.add(backToLobbyFromDraw);
        drawCardPanel.add(drawSouth, BorderLayout.SOUTH);
        // 顯示該面板
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "DrawCard");
    }

    private void showBattlePanel() {
        // 更新玩家手牌顯示，並重置戰鬥區域
        updateCardButtons();
        gameLog.setText("");
        computerCardPanel.removeAll();
        computerCardPanel.revalidate();
        computerCardPanel.repaint();
        // 切換到戰鬥面板
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "Battle");
    }

    private void initializeSelectionPanel() {
        JLabel title = new JLabel("Select 10 Cards for Battle", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        selectionPanel.add(title, BorderLayout.NORTH);

        // Initialize empty deck list model and JList
        deckListModel = new DefaultListModel<>();
        deckList = new JList<>(deckListModel);
        deckList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(deckList);
        selectionPanel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton confirm = new JButton("Confirm");
        confirm.addActionListener(e -> {
            List<Card> deck = gameController.getPlayerDeck(); // Ensure deck is fetched from GameController
            int[] sel = deckList.getSelectedIndices();
            if (sel.length != 10) {
                JOptionPane.showMessageDialog(this, "Please select exactly 10 cards.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Card> selected = new ArrayList<>();
            for (int idx : sel) {
                selected.add(deck.get(idx));
            }
            gameController.setBattleCards(selected);
            showBattlePanel();
        });
        JButton back = new JButton("Back to Lobby");
        back.addActionListener(e -> showLobbyPanel());
        btnPanel.add(confirm);
        btnPanel.add(back);
        selectionPanel.add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Populate the deck list from the player's deck, then show selection panel.
     */
    private void showSelectionPanel() {
        // Update list model
        deckListModel.clear();
        List<Card> deck = gameController.getPlayerDeck();
        for (int i = 0; i < deck.size(); i++) {
            Card c = deck.get(i);
            deckListModel.addElement(String.format("%s (%s %s, Type:%s, Pwr:%d)",
                c.getName(), c.getRarity(), c.getAttribute(), c.getType(), c.getBasePower()));
        }
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "SelectBattleCards");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        addRestartButton();
        addHistoryButton();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }
}
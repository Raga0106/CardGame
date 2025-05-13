package view;

import controller.GameController;
import model.Card;
import model.Player; // Import Player for stats
import service.BattleService.BattleResult;
import database.GameRecordService; // Import GameRecordService

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.JProgressBar;
import java.awt.event.KeyEvent;

/**
 * GUI-based interface for the game using Swing.
 */
@SuppressWarnings("unused")
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
    private JLabel playerLevelLabel; // Label for player level
    private JLabel playerXpLabel;   // Label for player XP
    private JLabel playerCurrencyLabel; // Label for player currency
    private JLabel playerRatingLabel; // Label for player rating
    private JPanel rankingPanel; // Panel for rankings
    private JComboBox<String> rankingCombo; // Choose ranking type
    private DefaultListModel<Player> rankingListModel; // 改為Player型別
    private JList<Player> rankingList; // 改為Player型別
    private JProgressBar xpBar;      // Progress bar for XP

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

        rankingPanel = new JPanel(new BorderLayout());
        initializeRankingPanel();

        // Add panels to main panel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(lobbyPanel, "Lobby");
        mainPanel.add(drawOptionsPanel, "DrawOptions");
        mainPanel.add(drawCardPanel, "DrawCard");
        mainPanel.add(selectionPanel, "SelectBattleCards");
        mainPanel.add(battlePanel, "Battle");
        mainPanel.add(rankingPanel, "Ranking");

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
                    currentUser = user;
                    // Load or create player data, and set as current player
                    Player player = recordService.loadPlayerData(currentUser);
                    gameController.setCurrentPlayer(player);
                    gameController.loadPlayerDeck(currentUser, recordService);
                    updatePlayerStatsDisplay();
                    // Debug: print entire players table content
                    recordService.checkDatabaseContent();
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

        // Stats panel for level, XP, currency, rating
        // Stats panel with XP progress bar
        JPanel statsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcStats = new GridBagConstraints();
        gbcStats.insets = new Insets(5,5,5,5);
        gbcStats.gridy = 0;
        gbcStats.gridx = 0;
        playerLevelLabel = new JLabel();
        statsPanel.add(playerLevelLabel, gbcStats);
        gbcStats.gridx = 1;
        playerXpLabel = new JLabel();
        statsPanel.add(playerXpLabel, gbcStats);
        gbcStats.gridx = 2;
        gbcStats.weightx = 1;
        gbcStats.fill = GridBagConstraints.HORIZONTAL;
        xpBar = new JProgressBar();
        xpBar.setStringPainted(true);
        statsPanel.add(xpBar, gbcStats);
        gbcStats.fill = GridBagConstraints.NONE;
        gbcStats.weightx = 0;
        gbcStats.gridx = 3;
        playerCurrencyLabel = new JLabel();
        statsPanel.add(playerCurrencyLabel, gbcStats);
        gbcStats.gridx = 4;
        playerRatingLabel = new JLabel();
        statsPanel.add(playerRatingLabel, gbcStats);
        lobbyPanel.add(statsPanel);
        updatePlayerStatsDisplay();

        JButton drawEntry = new JButton("抽卡");
        drawEntry.addActionListener(e -> { showDrawOptionsPanel(); });
        lobbyPanel.add(drawEntry);

        JButton battleButton = new JButton("Battle");
        battleButton.addActionListener(e -> { showSelectionPanel(); });
        lobbyPanel.add(battleButton);

        JButton checkDeckButton = new JButton("Check Deck");
        checkDeckButton.addActionListener(e -> { showDeck(); });
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

        JButton rankingButton = new JButton("排名");
        rankingButton.addActionListener(e -> { showRankingPanel(); });
        lobbyPanel.add(rankingButton);
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
        backToLobby.addActionListener(e -> { showLobbyPanel(); });
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
        backToLobbyFromBattle.addActionListener(e -> { showLobbyPanel(); });
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

        // Apply rating change based on wins/losses and save
        gameController.applyRatingChange();
        recordService.savePlayerData(gameController.getCurrentPlayer());
        updatePlayerStatsDisplay();

        JOptionPane.showMessageDialog(this, "Game Over! Winner: " + winner + "\n" + finalScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        // Disable all card buttons at the end
        for (Component comp : cardPanel.getComponents()) {
            comp.setEnabled(false);
        }
        // Return to lobby to show updated stats
        showLobbyPanel();
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
        startGameButton.addActionListener(e -> { showBattlePanel(); });
        JButton backToLobbyFromDraw = new JButton("Back to Lobby");
        backToLobbyFromDraw.addActionListener(e -> { showLobbyPanel(); });
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
        back.addActionListener(e -> { showLobbyPanel(); });
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

    private void updatePlayerStatsDisplay() {
        if (currentUser != null && gameController.getCurrentPlayer() != null) {
            Player player = gameController.getCurrentPlayer();
            playerLevelLabel.setText("Level: " + player.getLevel());
            playerXpLabel.setText(String.format("XP: %d/%d", player.getXp(), player.getXpToNextLevel()));
            xpBar.setMaximum(player.getXpToNextLevel());
            xpBar.setValue(player.getXp());
            xpBar.setString(player.getXp() + " / " + player.getXpToNextLevel());
            playerCurrencyLabel.setText("Currency: " + player.getCurrency());
            playerRatingLabel.setText("Rating: " + player.getRating());
        } else {
            playerLevelLabel.setText("Level: -");
            playerXpLabel.setText("XP: -/-");
            xpBar.setValue(0);
            xpBar.setString("0 / 0");
            playerCurrencyLabel.setText("Currency: -");
            playerRatingLabel.setText("Rating: -");
        }
    }

    /**
     * Initializes the ranking panel with controls and list.
     */
    private void initializeRankingPanel() {
        rankingPanel.removeAll(); // Clear previous components if re-initializing
        rankingPanel.setLayout(new BorderLayout(10, 10)); // Add gaps between BorderLayout regions
        rankingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the panel

        // Title
        JLabel title = new JLabel("排行榜", SwingConstants.CENTER); // Changed to Chinese for consistency
        title.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 28)); // Example of a more modern font
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); // Add padding below title
        rankingPanel.add(title, BorderLayout.NORTH);

        // Ranking list in center
        rankingListModel = new DefaultListModel<>();
        rankingList = new JList<>(rankingListModel);
        rankingList.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        // Apply custom cell renderer for better list item appearance
        rankingList.setCellRenderer(new CustomRankingRenderer());
        JScrollPane scrollPane = new JScrollPane(rankingList);
        rankingPanel.add(scrollPane, BorderLayout.CENTER);

        // Controls (combo, sort, back) at bottom
        JPanel controlPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for more control
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Padding above control panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sortByLabel = new JLabel("排序依據：");
        sortByLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Label doesn't expand
        gbc.anchor = GridBagConstraints.LINE_START;
        controlPanel.add(sortByLabel, gbc);

        String[] options = {"等級", "貨幣", "牌位積分"};
        rankingCombo = new JComboBox<>(options);
        rankingCombo.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        rankingCombo.setToolTipText("選擇排序依據");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // ComboBox takes available space
        controlPanel.add(rankingCombo, gbc);

        JButton sortButton = new JButton("排序");
        sortButton.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        sortButton.setToolTipText("依照選擇的依據對玩家排名排序");
        sortButton.setMnemonic(KeyEvent.VK_S);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.25; // Button takes some space
        controlPanel.add(sortButton, gbc);

        JButton backButton = new JButton("返回大廳"); // Changed to Chinese
        backButton.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        backButton.setToolTipText("返回大廳");
        backButton.setMnemonic(KeyEvent.VK_B);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.25; // Button takes some space
        controlPanel.add(backButton, gbc);
        // Add action listener for back to lobby button in ranking panel
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLobbyPanel();
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        rankingPanel.add(controlPanel, BorderLayout.SOUTH);

        // Action listener for sort button
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rankingListModel.clear();
                List<Player> all = recordService.loadAllPlayers();
                String key = (String) rankingCombo.getSelectedItem();
                Comparator<Player> comp;
                if ("貨幣".equals(key)) {
                    comp = Comparator.comparingInt(Player::getCurrency).reversed();
                } else if ("牌位積分".equals(key)) {
                    comp = Comparator.comparingInt(Player::getRating).reversed();
                } else {
                    comp = Comparator.comparingInt(Player::getLevel).reversed();
                }
                all.stream().sorted(comp).forEach(p -> rankingListModel.addElement(p));
            }
        });

        // Back to lobby button action listener for ranking panel
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLobbyPanel();
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

    private void showRankingPanel() {
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "Ranking");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        addRestartButton();
        addHistoryButton();
    }

    // Custom renderer for ranking list items
    private class CustomRankingRenderer extends JPanel implements ListCellRenderer<Player> {
        private JLabel nameLabel = new JLabel();
        private JLabel levelLabel = new JLabel();
        private JLabel currencyLabel = new JLabel();
        private JLabel ratingLabel = new JLabel();
        public CustomRankingRenderer() {
            setLayout(new GridLayout(2, 2, 5, 5));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            add(nameLabel);
            add(levelLabel);
            add(currencyLabel);
            add(ratingLabel);
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends Player> list, Player player, int index, boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(player.getUsername());
            levelLabel.setText("等級: " + player.getLevel());
            currencyLabel.setText("貨幣: " + player.getCurrency());
            ratingLabel.setText("牌位積分: " + player.getRating());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }
}
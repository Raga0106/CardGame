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
import javax.swing.plaf.FontUIResource;
import java.util.Enumeration;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingWorker;

/**
 * GUI-based interface for the game using Swing.
 */
@SuppressWarnings("unused")
public class GameGUI extends JFrame {
    private final GameController gameController;
    private boolean isDarkTheme = false; // 追蹤當前使用的是否為暗色主題
    private JTextArea gameLog;
    private JPanel cardPanel;
    private JLabel scoreLabel;
    private JLabel roundLabel;
    private JPanel computerCardPanel;
    private final CardLayout cardLayout; // Added
    private final JPanel mainPanel; // Main panel with CardLayout
    private final JPanel drawCardPanel; // Panel for card drawing
    private final JPanel battlePanel; // Panel for battle
    private JPanel loginPanel; // Panel for login and registration - Removed final
    private final JPanel lobbyPanel; // Panel for the game lobby
    private final JPanel drawOptionsPanel; // Panel for choosing single or ten draw
    private final JPanel selectionPanel; // Panel for selecting battle cards
    private DefaultListModel<String> deckListModel;
    private JList<String> deckList;
    private GameRecordService recordService;  // Database service for users and records
    private Player currentPlayer; // Changed from String to Player
    private JLabel playerLevelLabel; // Label for player level
    private JLabel playerXpLabel;   // Label for player XP
    private JLabel playerCurrencyLabel; // Label for player currency
    private JLabel playerRatingLabel; // Label for player rating
    private JPanel rankingPanel; // Panel for rankings
    private JComboBox<String> rankingCombo; // Choose ranking type
    private DefaultListModel<Player> rankingListModel; // 改為Player型別
    private JList<Player> rankingList; // 改為Player型別
    private JProgressBar xpBar;      // Progress bar for XP

    // Fields for login panel components that need to be accessed by LoginWorker
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;


    /**
     * Constructor for GameGUI.
     */
    public GameGUI() {
        gameController = new GameController();
        gameController.startGame();
        recordService = new GameRecordService(); // 初始化資料庫和表格        // 設置基本窗口属性
        setTitle("卡牌對決：元素抽卡競技場");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中顯示
        
        try {
            // 嘗試設置統一的視覺風格 - 使用系統外觀
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 添加菜單欄
        setJMenuBar(createMenuBar());

        // 主面板使用CardLayout布局
        cardLayout = new CardLayout(); // Initialize cardLayout
        mainPanel = new JPanel(cardLayout); // Use initialized cardLayout
        add(mainPanel);

        // 初始化组件
        scoreLabel = new JLabel("玩家: 0 | 電腦: 0", SwingConstants.CENTER);
        roundLabel = new JLabel("回合: 1/10", SwingConstants.CENTER);
        gameLog = new JTextArea();
        cardPanel = new JPanel();
        computerCardPanel = new JPanel();

        // 初始化登入面板
        loginPanel = new JPanel(new BorderLayout());
        initializeLoginPanel();

        // 初始化大廳面板
        lobbyPanel = new JPanel(new GridLayout(3, 1));
        initializeLobbyPanel();

        // 初始化抽卡選項面板
        drawOptionsPanel = new JPanel(new GridLayout(3, 1));
        initializeDrawOptionsPanel();

        // 初始化抽卡結果面板
        drawCardPanel = new JPanel(new BorderLayout());
        initializeDrawCardPanel();        // 初始化對戰面板
        battlePanel = new JPanel(new BorderLayout());
        initializeBattlePanel();

        // 初始化卡牌選擇面板 - 先不調用initializeSelectionPanel，在showSelectionPanel中完整初始化
        selectionPanel = new JPanel(new BorderLayout());

        // 初始化排行榜面板
        rankingPanel = new JPanel(new BorderLayout());
        initializeRankingPanel();

        // 將各個面板添加到主面板
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(lobbyPanel, "Lobby");
        mainPanel.add(drawOptionsPanel, "DrawOptions");
        mainPanel.add(drawCardPanel, "DrawCard");
        mainPanel.add(selectionPanel, "SelectBattleCards");
        mainPanel.add(battlePanel, "Battle");
        mainPanel.add(rankingPanel, "Ranking");        // 首先顯示登入面板
        showLoginPanel();
        
        // 設置菜單欄
        setJMenuBar(createMenuBar());
        
        // 初始應用明亮主題
        applyTheme("light");
    }
    
    /**
     * 為整個UI設置統一字體
     */
    private void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private void initializeLoginPanel() {
        // loginPanel = new JPanel(new GridBagLayout()); // This line was causing the final field assignment error.
        // loginPanel is already initialized in the constructor or as a field.
        // We should clear it and set its layout if we are re-initializing it.
        if (loginPanel == null) {
            loginPanel = new JPanel(new GridBagLayout());
        } else {
            loginPanel.removeAll();
            loginPanel.setLayout(new GridBagLayout());
        }
        
        // 設置面板邊距
        loginPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // 使用GridBagLayout管理佈局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 標題
        JLabel titleLabel = new JLabel("卡牌對決：元素抽卡競技場", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        // 副標題
        JLabel subtitleLabel = new JLabel("歡迎來到遊戲世界", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 16));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
        gbc.gridy = 1;
        loginPanel.add(subtitleLabel, gbc);
        
        // 使用者名稱標籤和文本框
        JLabel usernameLabel = new JLabel("使用者名稱:");
        usernameLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginPanel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20); // Assign to field
        usernameField.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);
        
        // 密碼標籤和文本框
        JLabel passwordLabel = new JLabel("密碼:");
        passwordLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20); // Assign to field
        passwordField.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);
        
        // 登入狀態標籤
        statusLabel = new JLabel("請輸入您的帳號和密碼"); // Assign to field
        statusLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginPanel.add(statusLabel, gbc);
        
        // 登入按鈕
        loginButton = createStyledButton("登入", e -> { // Assign to field
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            // TODO: Add input validation here later

            loginButton.setEnabled(false); // Disable button while logging in
            // Optionally, show a loading indicator here
            statusLabel.setText("正在登入...");

            LoginWorker worker = new LoginWorker(username, password);
            worker.execute();
        });
        
        // 註冊新帳號按鈕
        JButton registerButton = createStyledButton("註冊新帳號", e -> { 
            String user = JOptionPane.showInputDialog(this, "請輸入使用者名稱:");
            if (user != null && !user.isEmpty()) {
                String pass = JOptionPane.showInputDialog(this, "請輸入密碼:");
                if (pass != null && !pass.isEmpty()) { 
                    if (recordService.registerUser(user, pass)) {
                        JOptionPane.showMessageDialog(this, "註冊成功！請登入。");
                    } else {
                        JOptionPane.showMessageDialog(this, "使用者名稱已存在。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        // 退出遊戲按鈕
        JButton exitButton = createStyledButton("退出遊戲", e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "確定要退出遊戲嗎？", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        gbc.gridy = 5;
        loginPanel.add(buttonPanel, gbc);
        
        // 底部版權資訊
        JPanel footerPanel = new JPanel();
        JLabel footerLabel = new JLabel("© 2025 卡牌遊戲公司。保留所有權利。", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 12));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        
        gbc.gridy = 6;
        loginPanel.add(footerPanel, gbc);
        
        // 重新驗證和重新繪製
        loginPanel.revalidate();
        loginPanel.repaint();
    }    private void initializeLobbyPanel() {
        // Build lobby components based on login state
        lobbyPanel.removeAll();
        
        // 使用BorderLayout替代GridLayout以獲得更好的空間管理
        lobbyPanel.setLayout(new BorderLayout(10, 10));
        
        // 頂部標題面板
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("遊戲大廳", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        lobbyPanel.add(titlePanel, BorderLayout.NORTH);

        // 玩家統計資訊面板 (使用GridBagLayout配置更精細)
        JPanel statsPanel = createStatsPanel();
        
        // 主要操作按鈕面板 (中央)
        JPanel mainButtonsPanel = new JPanel(new GridBagLayout());
        mainButtonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // 增加邊距
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 按鈕之間的間距
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        
        // 主要功能按鈕
        JButton drawEntry = createStyledButton("抽卡", e -> showDrawOptionsPanel());
        JButton battleButton = createStyledButton("對戰", e -> {
            // 確保玩家至少有10張牌可以選擇
            if (gameController.getPlayerDeck().size() < 10) {
                JOptionPane.showMessageDialog(this, 
                    "你的牌組中至少需要10張卡牌才能進行對戰。\n請先抽卡獲取更多卡片。", 
                    "卡片不足", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            showSelectionPanel();
        });
        JButton checkDeckButton = createStyledButton("檢視牌組", e -> showDeck());
        JButton rankingButton = createStyledButton("排行榜", e -> showRankingPanel());
        
        // 布局按鈕 (2x2網格)
        gbc.gridx = 0; gbc.gridy = 0;
        mainButtonsPanel.add(drawEntry, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        mainButtonsPanel.add(battleButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainButtonsPanel.add(checkDeckButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        mainButtonsPanel.add(rankingButton, gbc);
        
        // 管理面板 - 包含退出和管理功能
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        adminPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "系統功能"));
            
        JButton logoutButton = createStyledButton("登出", e -> {
            currentPlayer = null; // Fix: Use currentPlayer
            JOptionPane.showMessageDialog(this, "您已成功登出。", "登出", JOptionPane.INFORMATION_MESSAGE);
            showLoginPanel();
        });
        adminPanel.add(logoutButton);
        
        // 管理員功能 (只有admin才能看到)
        if (currentPlayer != null && "admin".equals(currentPlayer.getUsername())) { // Fix: Use currentPlayer.getUsername()
            JButton initDbButton = createStyledButton("初始化資料庫", e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "確定要清除所有遊戲紀錄嗎？", "確認", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabase(currentPlayer.getUsername())) { // Fix: Use currentPlayer.getUsername()
                        JOptionPane.showMessageDialog(this, "資料庫已清除。", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "資料庫清除失敗。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            adminPanel.add(initDbButton);
            
            JButton clearRecordsButton = createStyledButton("清除所有紀錄", e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "確定要清除所有遊戲紀錄嗎？", "確認", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabaseByType(currentPlayer.getUsername(), "records")) { // Fix: Use currentPlayer.getUsername()
                        JOptionPane.showMessageDialog(this, "所有遊戲紀錄已清除。", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "遊戲紀錄清除失敗。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            adminPanel.add(clearRecordsButton);

            JButton clearCardsButton = createStyledButton("清除所有卡牌", e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "確定要清除所有卡牌嗎？", "確認", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (recordService.clearDatabaseByType(currentPlayer.getUsername(), "cards")) { // Fix: Use currentPlayer.getUsername()
                        JOptionPane.showMessageDialog(this, "所有卡牌已清除。", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "卡牌清除失敗。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            adminPanel.add(clearCardsButton);
        }
        
        // 將各個面板添加到主面板中
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(mainButtonsPanel, BorderLayout.CENTER);
        
        lobbyPanel.add(centerPanel, BorderLayout.CENTER);
        lobbyPanel.add(adminPanel, BorderLayout.SOUTH);
        
        // 更新玩家統計資訊
        updatePlayerStatsDisplay();
    }
    
    /**
     * 創建統計面板，顯示玩家等級、XP等資訊
     */
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "玩家資訊"),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        GridBagConstraints gbcStats = new GridBagConstraints();
        gbcStats.insets = new Insets(5, 5, 5, 5);
        gbcStats.anchor = GridBagConstraints.WEST;
        
        // 第一行 - 等級和XP
        gbcStats.gridy = 0;
        gbcStats.gridx = 0;
        gbcStats.gridwidth = 1;
        playerLevelLabel = new JLabel();
        playerLevelLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        statsPanel.add(playerLevelLabel, gbcStats);
        
        gbcStats.gridx = 1;
        playerXpLabel = new JLabel();
        playerXpLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        statsPanel.add(playerXpLabel, gbcStats);
        
        // 第二行 - XP進度條
        gbcStats.gridy = 1;
        gbcStats.gridx = 0;
        gbcStats.gridwidth = 4;
        gbcStats.weightx = 1;
        gbcStats.fill = GridBagConstraints.HORIZONTAL;
        xpBar = new JProgressBar();
        xpBar.setStringPainted(true);
        xpBar.setPreferredSize(new Dimension(300, 20));
        statsPanel.add(xpBar, gbcStats);
        
        // 第三行 - 貨幣和評分
        gbcStats.gridy = 2;
        gbcStats.gridx = 0;
        gbcStats.gridwidth = 1;
        gbcStats.weightx = 0;
        gbcStats.fill = GridBagConstraints.NONE;
        playerCurrencyLabel = new JLabel();
        playerCurrencyLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        statsPanel.add(playerCurrencyLabel, gbcStats);
        
        gbcStats.gridx = 1;
        playerRatingLabel = new JLabel();
        playerRatingLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        statsPanel.add(playerRatingLabel, gbcStats);
        
        return statsPanel;
    }
    
    /**
     * 創建統一風格的按鈕
     */
    private JButton createStyledButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        button.setMargin(new Insets(10, 20, 10, 20));
        button.addActionListener(listener);
        return button;
    }

    private void initializeDrawOptionsPanel() {
        // 清空面板並設置更好的佈局
        drawOptionsPanel.removeAll();
        drawOptionsPanel.setLayout(new BorderLayout(10, 10));
        drawOptionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 標題面板
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("抽卡系統", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        drawOptionsPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 中央選項面板
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
          JButton singleDraw = createStyledButton("單抽", e -> { 
            Card newCard = gameController.drawCard();
            if (currentPlayer != null) { // Fix: Check if currentPlayer is not null
                recordService.saveCardToDeck(currentPlayer.getUsername(), newCard); // Fix: Use currentPlayer.getUsername()
            }
            
            // 顯示抽卡動畫
            showAnimationEffect("card_draw");
            
            String msg = String.format("你抽到了: %s (%s %s, 類型: %s, 力量: %d)\n%s", 
                newCard.getName(), newCard.getRarity(), newCard.getAttribute(), newCard.getType(), newCard.getBasePower(), newCard.getDescription());
            JOptionPane.showMessageDialog(this, msg, "單抽結果", JOptionPane.INFORMATION_MESSAGE);
        });
          JButton tenDraw = createStyledButton("十連抽", e -> { 
            // 播放抽卡動畫
            showAnimationEffect("card_draw");
            
            List<Card> newCards = gameController.drawMultiple(10);
            for (Card card : newCards) {
                if (currentPlayer != null) { // Fix: Check if currentPlayer is not null
                    recordService.saveCardToDeck(currentPlayer.getUsername(), card); // Fix: Use currentPlayer.getUsername()
                }
            }
            updateCardButtons();
            showDrawCardPanel();
        });
        
        JButton backToLobby = createStyledButton("返回大廳", e -> { showLobbyPanel(); });
        
        gbc.gridy = 0; optionsPanel.add(singleDraw, gbc);
        gbc.gridy = 1; optionsPanel.add(tenDraw, gbc);
        gbc.gridy = 2; optionsPanel.add(backToLobby, gbc);
        
        drawOptionsPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // 底部說明面板
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("每日首次抽卡可獲得額外獎勵！", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 14));
        infoPanel.add(infoLabel);
        drawOptionsPanel.add(infoPanel, BorderLayout.SOUTH);
        
        drawOptionsPanel.revalidate();
        drawOptionsPanel.repaint();
    }

    private void initializeDrawCardPanel() {
        // Use dynamic panel rebuilding
        showDrawCardPanel();
    }

    private void initializeBattlePanel() {
        // 清除面板內容
        battlePanel.removeAll();
        battlePanel.setLayout(new BorderLayout(10, 10));
        battlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 頂部面板 - 顯示分數和回合
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        scoreLabel = new JLabel("玩家: 0 | 電腦: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 16));
        roundLabel = new JLabel("回合: 1/10", SwingConstants.CENTER);
        roundLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 16));
        JButton backToLobbyFromBattle = createStyledButton("返回大廳", e -> { showLobbyPanel(); });
        
        topPanel.add(scoreLabel);
        topPanel.add(roundLabel);
        topPanel.add(backToLobbyFromBattle);
        battlePanel.add(topPanel, BorderLayout.NORTH);
        
        // 中央面板 - 遊戲日誌
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gameLog.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gameLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "遊戲日誌"));
        battlePanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板 - 玩家卡牌
        cardPanel = new JPanel();
        cardPanel.setLayout(new GridLayout(2, 5, 8, 8));
        cardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "你的手牌"));
        battlePanel.add(cardPanel, BorderLayout.SOUTH);
        
        // 左側面板 - 電腦卡牌顯示
        computerCardPanel = new JPanel();
        computerCardPanel.setLayout(new BorderLayout());
        computerCardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "電腦的卡牌"));
        computerCardPanel.setPreferredSize(new Dimension(180, 200));
        battlePanel.add(computerCardPanel, BorderLayout.WEST);
        
        // 初始更新卡牌按鈕
        updateCardButtons();
        
        // 重新驗證和繪製
        battlePanel.revalidate();
        battlePanel.repaint();
    }

    private void updateCardButtons() {
        cardPanel.removeAll();
        int index = 0;
        for (Card card : gameController.getPlayerCards()) {
            // 使用HTML格式化按鈕文本以增強可讀性
            String buttonText = String.format("<html><center><b>%s</b><br>%s %s<br>力量: %d</center></html>",
                    card.getName(),
                    card.getRarity(),
                    card.getAttribute(),
                    card.getBasePower());
            JButton cardButton = new JButton(buttonText);
            cardButton.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 12));
            
            // 根據稀有度設置邊框顏色
            String rarity = card.getRarity().toString();
            if (rarity.equals("LEGENDARY")) {
                cardButton.setBackground(new Color(255, 250, 205)); // 淺金色
            } else if (rarity.equals("RARE")) {
                cardButton.setBackground(new Color(230, 230, 250)); // 淺藍色
            } else if (rarity.equals("UNCOMMON")) {
                cardButton.setBackground(new Color(240, 255, 240)); // 淺綠色
            }
            
            int cardIndex = index++;
            cardButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 點擊後禁用按鈕以防止重複使用
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
            // 在戰鬥前獲取玩家和電腦卡牌用於日誌記錄
            Card playerCard = gameController.getPlayerCards().get(playerCardIndex);
            Card computerCard = gameController.getComputerCards().get(0); // 假設電腦選擇第一張

            BattleResult result = gameController.playRound(playerCardIndex);

            // 更新電腦卡牌顯示
            computerCardPanel.removeAll();
            String computerCardText = String.format("<html><center><b>%s</b><br>%s %s<br>力量: %d</center></html>",
                    computerCard.getName(),
                    computerCard.getRarity(),
                    computerCard.getAttribute(),
                    computerCard.getBasePower());
            JLabel computerCardLabel = new JLabel(computerCardText, SwingConstants.CENTER);
            computerCardLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
            // 添加背景色以增強視覺效果
            String rarity = computerCard.getRarity().toString();
            if (rarity.equals("LEGENDARY")) {
                computerCardLabel.setOpaque(true);
                computerCardLabel.setBackground(new Color(255, 250, 205)); // 淺金色
            } else if (rarity.equals("RARE")) {
                computerCardLabel.setOpaque(true);
                computerCardLabel.setBackground(new Color(230, 230, 250)); // 淺藍色
            } else if (rarity.equals("UNCOMMON")) {
                computerCardLabel.setOpaque(true);
                computerCardLabel.setBackground(new Color(240, 255, 240)); // 淺綠色
            }
            computerCardPanel.add(computerCardLabel);
            computerCardPanel.revalidate();
            computerCardPanel.repaint();

            // 使用正確的回合数記錄更詳細的戰鬥信息
            int logRound = 10 - gameController.getPlayerCards().size();
            gameLog.append(String.format("\n回合 %d:\n", logRound));
            gameLog.append(String.format("玩家出牌: %s (%s %s, 力量: %d)\n",
                    playerCard.getName(), playerCard.getRarity(), playerCard.getAttribute(), playerCard.getBasePower()));
            gameLog.append(String.format("電腦出牌: %s (%s %s, 力量: %d)\n",
                    computerCard.getName(), computerCard.getRarity(), computerCard.getAttribute(), computerCard.getBasePower()));
              // 根據勝者判斷結果並轉為中文
            String resultText;
            if (result.getWinner() == playerCard) {
                resultText = "玩家獲勝！";
            } else if (result.getWinner() == computerCard) {
                resultText = "電腦獲勝！";
            } else {
                resultText = "平局！";
            }
            gameLog.append(resultText + "\n");

            // 回合後刷新卡牌按鈕
            updateCardButtons();

            // 更新分數
            scoreLabel.setText(String.format("玩家: %d | 電腦: %d",
                    gameController.getPlayerScore(), gameController.getComputerScore()));
            // 只在還有剩餘卡牌時更新回合數
            if (!gameController.getPlayerCards().isEmpty()) {
                int currentRound = 10 - gameController.getPlayerCards().size() + 1; // 正確的回合計算
                roundLabel.setText(String.format("回合: %d/10", currentRound));
            }

            if (gameController.getPlayerCards().isEmpty()) {
                endGame();
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) { // 也捕獲潛在的索引錯誤
            JOptionPane.showMessageDialog(this, "無效的卡牌選擇或卡牌已經被使用。", "錯誤", JOptionPane.ERROR_MESSAGE);
            // 如果發生錯誤，重新啟用特定按鈕
            if (playerCardIndex >= 0 && playerCardIndex < cardPanel.getComponentCount()) {
                 Component button = cardPanel.getComponent(playerCardIndex);
                 if (button instanceof JButton) {
                     ((JButton) button).setEnabled(true);
                 }
            }
        }
    }    private void endGame() {
        String winner = gameController.determineWinner();
        
        // 把英文結果轉為中文
        String winnerText;
        if (winner.contains("Player")) {
            winnerText = "玩家";
            // 播放勝利動畫
            showAnimationEffect("win");
        } else if (winner.contains("Computer")) {
            winnerText = "電腦";
            // 播放失敗動畫
            showAnimationEffect("lose");
        } else {
            winnerText = "平局";
            // 播放平局動畫
            showAnimationEffect("draw");
        }
        
        String finalScore = String.format("最終分數 - 玩家: %d, 電腦: %d",
                gameController.getPlayerScore(), gameController.getComputerScore());

        gameLog.append("\n遊戲結束!\n");
        gameLog.append(finalScore + "\n");
        gameLog.append("勝利者: " + winnerText + "\n");

        // 將記錄保存到資料庫
        if (currentPlayer != null) { // Fix: Check if currentPlayer is not null
            recordService.saveRecord(currentPlayer.getUsername(), "Player", gameController.getPlayerScore(), gameController.getComputerScore()); // Fix: Use currentPlayer.getUsername()
            gameLog.append("遊戲記錄已保存。\n");
        }

        // 根據勝負應用評分變更並保存
        gameController.applyRatingChange();
        recordService.savePlayerData(gameController.getCurrentPlayer());
        updatePlayerStatsDisplay();

        JOptionPane.showMessageDialog(this, "遊戲結束! 勝利者: " + winnerText + "\n" + finalScore, "遊戲結束", JOptionPane.INFORMATION_MESSAGE);

        // 結束時禁用所有卡牌按鈕
        for (Component comp : cardPanel.getComponents()) {
            comp.setEnabled(false);
        }
        // 返回大廳以顯示更新的統計信息
        showLobbyPanel();
    }

    private void addRestartButton() {
        JButton restartButton = createStyledButton("重新開始", e -> { 
            gameController.startGame();
            gameLog.setText("");
            scoreLabel.setText("玩家: 0 | 電腦: 0");
            roundLabel.setText("回合: 1/10");
            updateCardButtons();
        });
        restartButton.setToolTipText("開始新的對戰");
        // add(restartButton, BorderLayout.EAST); // This was causing issues, restart button is part of battle panel or similar context
    }

    private void addHistoryButton() {
        JButton historyButton = createStyledButton("查看歷史", e -> {
            // 使用 GameRecordService 的方法來查詢對戰紀錄
            if (currentPlayer != null) { // Fix: Check if currentPlayer is not null
                List<String> records = recordService.getAllRecords(currentPlayer.getUsername()); // Fix: Use currentPlayer.getUsername()
                if (records.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "用戶 " + currentPlayer.getUsername() + " 沒有找到對戰記錄", // Fix: Use currentPlayer.getUsername()
                        "遊戲歷史", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // 創建更美觀的遊戲歷史記錄對話框
                    JDialog historyDialog = new JDialog(this, "遊戲歷史記錄", true);
                    historyDialog.setLayout(new BorderLayout(10, 10));
                    historyDialog.setSize(500, 400);
                    historyDialog.setLocationRelativeTo(this);
                    
                    // 標題面板
                    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    JLabel titleLabel = new JLabel(currentPlayer.getUsername() + " 的遊戲記錄", SwingConstants.CENTER); // Fix: Use currentPlayer.getUsername()
                    titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
                    if (isDarkTheme) {
                        titleLabel.setForeground(Color.WHITE);
                    }
                    titlePanel.add(titleLabel);
                    historyDialog.add(titlePanel, BorderLayout.NORTH);
                    
                    // 記錄列表
                    JPanel recordsPanel = new JPanel();
                    recordsPanel.setLayout(new BoxLayout(recordsPanel, BoxLayout.Y_AXIS));
                    recordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                    if (isDarkTheme) {
                        recordsPanel.setBackground(new Color(33, 37, 43));
                    }
                    
                    // 統計資訊
                    JLabel statsLabel = new JLabel("總遊戲記錄數: " + records.size());
                    statsLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 14));
                    statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    if (isDarkTheme) {
                        statsLabel.setForeground(Color.WHITE);
                    }
                    recordsPanel.add(statsLabel);
                    
                    // 添加每條記錄
                    int count = 1;
                    for (String record : records) {
                        JPanel recordPanel = new JPanel();
                        recordPanel.setLayout(new BorderLayout(5, 2));
                        recordPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                            BorderFactory.createEmptyBorder(8, 5, 8, 5)
                        ));
                        
                        // 為了使每條記錄更易讀，替換英文成中文並添加更多信息
                        String enhancedRecord = record
                            .replace("Game record for", "對戰記錄 -")
                            .replace("Player", "玩家")
                            .replace("Computer", "電腦");
                        
                        // 添加斑馬紋效果
                        if (count % 2 == 0) {
                            recordPanel.setBackground(new Color(245, 245, 245));
                        }
                        
                        JLabel recordLabel = new JLabel("#" + count + ": " + enhancedRecord);
                        recordLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
                        recordPanel.add(recordLabel, BorderLayout.CENTER);
                        
                        recordsPanel.add(recordPanel);
                        count++;
                    }
                    
                    // 添加記錄到滾動面板
                    JScrollPane scrollPane = new JScrollPane(recordsPanel);
                    historyDialog.add(scrollPane, BorderLayout.CENTER);
                    
                    // 底部按鈕
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    JButton closeButton = createStyledButton("關閉", e2 -> historyDialog.dispose());
                    buttonPanel.add(closeButton);
                    historyDialog.add(buttonPanel, BorderLayout.SOUTH);
                    
                    // 顯示對話框
                    historyDialog.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "請先登入以查看歷史記錄。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        historyButton.setToolTipText("查看過去的對戰記錄");
        // add(historyButton, BorderLayout.WEST); // This was causing issues, history button is part of battle panel or similar context
    }    /**
     * 顯示玩家的當前牌組，包括完整詳細信息和重複卡片的計數。
     */
    private void showDeck() {
        List<Card> deck = gameController.getPlayerDeck();
        // 按卡片名稱聚合，保留抽取順序
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, Card> repMap = new LinkedHashMap<>();
        for (Card c : deck) {
            countMap.put(c.getName(), countMap.getOrDefault(c.getName(), 0) + 1);
            repMap.putIfAbsent(c.getName(), c);
        }
        
        // 創建更視覺化的對話框
        JDialog deckDialog = new JDialog(this, "牌組內容", true);
        deckDialog.setLayout(new BorderLayout(10, 10));
        deckDialog.setSize(600, 400);
        deckDialog.setLocationRelativeTo(this);
        
        // 設置對話框主題顏色
        if (isDarkTheme) {
            deckDialog.getContentPane().setBackground(new Color(33, 37, 43));
        }
        
        // 標題面板
        JPanel titlePanel = new JPanel();
        if (isDarkTheme) {
            titlePanel.setBackground(new Color(33, 37, 43));
        }
        JLabel titleLabel = new JLabel("你的牌組", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
        if (isDarkTheme) {
            titleLabel.setForeground(Color.WHITE);
        }
        titlePanel.add(titleLabel);
        deckDialog.add(titlePanel, BorderLayout.NORTH);
        
        // 卡片列表面板
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        if (isDarkTheme) {
            cardsPanel.setBackground(new Color(33, 37, 43));
        }
        
        // 統計數據
        JLabel statsLabel = new JLabel(String.format("總卡片數: %d, 獨特卡片: %d", 
                                       deck.size(), repMap.size()));
        statsLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 14));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isDarkTheme) {
            statsLabel.setForeground(Color.WHITE);
        }
        cardsPanel.add(statsLabel);
        
        // 添加每張卡片
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Card rep = repMap.get(entry.getKey());
            int cnt = entry.getValue();
            
            JPanel cardPanel = new JPanel();
            cardPanel.setLayout(new BorderLayout(5, 2));
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkTheme ? new Color(80, 80, 80) : Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
            ));
            
            // 左側：卡片名稱和數量
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            if (isDarkTheme) {
                leftPanel.setBackground(new Color(33, 37, 43));
            }
            
            String nameHtml = String.format("<html><b>%s</b> x%d</html>", rep.getName(), cnt);
            JLabel nameLabel = new JLabel(nameHtml);
            nameLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
            if (isDarkTheme) {
                nameLabel.setForeground(Color.WHITE);
            }
            leftPanel.add(nameLabel);
              // 右側：卡片詳細資訊
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            if (isDarkTheme) {
                rightPanel.setBackground(new Color(33, 37, 43));
            }
              // 獲取對應稀有度的顏色
            String rarityColorHex = getRarityColorForTheme(rep.getRarity().toString());
            
            String details = String.format("<html><span style='color:%s'>%s</span> %s, 類型: %s, 力量: %d<br><i>%s</i></html>",
                                          rarityColorHex,
                                          rep.getRarity(), rep.getAttribute(), 
                                          rep.getType(), rep.getBasePower(), 
                                          rep.getDescription());
            JLabel detailsLabel = new JLabel(details);
            detailsLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 13));
            if (isDarkTheme) {
                detailsLabel.setForeground(Color.WHITE);
            }
            rightPanel.add(detailsLabel);
            
            cardPanel.add(leftPanel, BorderLayout.WEST);
            cardPanel.add(rightPanel, BorderLayout.CENTER);
            
            // 根據稀有度和主題設置背景色
            String rarity = rep.getRarity().toString();
            if (isDarkTheme) {
                // 暗色主題下的背景色
                if (rarity.equals("LEGENDARY")) {
                    cardPanel.setBackground(new Color(100, 84, 0, 80)); // 深金色
                } else if (rarity.equals("RARE")) {
                    cardPanel.setBackground(new Color(40, 70, 120, 80)); // 深藍色
                } else if (rarity.equals("UNCOMMON")) {
                    cardPanel.setBackground(new Color(30, 80, 30, 80)); // 深綠色
                } else {
                    cardPanel.setBackground(new Color(50, 50, 50)); // 深灰色
                }
            } else {
                // 明亮主題下的背景色
                if (rarity.equals("LEGENDARY")) {
                    cardPanel.setBackground(new Color(255, 250, 205)); // 淺金色
                } else if (rarity.equals("RARE")) {
                    cardPanel.setBackground(new Color(230, 230, 250)); // 淺藍色
                } else if (rarity.equals("UNCOMMON")) {
                    cardPanel.setBackground(new Color(240, 255, 240)); // 淺綠色
                } else {
                    cardPanel.setBackground(Color.WHITE); // 白色
                }            }
            
            cardsPanel.add(cardPanel);
        }
        
        // 將卡片列表加入滾动面板
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        deckDialog.add(scrollPane, BorderLayout.CENTER);
          // 底部按鈕
        JPanel buttonPanel = new JPanel();
        if (isDarkTheme) {
            buttonPanel.setBackground(new Color(33, 37, 43));
        }
        JButton closeButton = createStyledButton("關閉", e -> deckDialog.dispose());
        buttonPanel.add(closeButton);
        deckDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 顯示對話框
        deckDialog.setVisible(true);
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
    }    private void showDrawCardPanel() {
        // 動態重建抽卡面板以顯示最新卡片
        drawCardPanel.removeAll();
        drawCardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 標題
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("抽卡結果", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        drawCardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 卡片顯示區域
        JPanel cardDisplayPanel = new JPanel(new GridLayout(2, 5, 8, 8));
        cardDisplayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        List<Card> cards = gameController.getPlayerCards();
        
        for (Card card : cards) {
            // 創建卡片面板並添加邊框效果
            JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
            // 根據稀有度和主題設置邊框顏色
            Color borderColor;
            Color bgColor;
            Color textColor = isDarkTheme ? Color.WHITE : Color.BLACK;
            
            String rarity = card.getRarity().toString();
            if (rarity.equals("LEGENDARY")) {
                borderColor = new Color(255, 215, 0); // 金色邊框在兩種主題下都相同
                bgColor = isDarkTheme ? new Color(100, 84, 0, 80) : new Color(255, 250, 205);
            } else if (rarity.equals("RARE")) {
                borderColor = new Color(70, 130, 180); // 藍色邊框
                bgColor = isDarkTheme ? new Color(40, 70, 120, 80) : new Color(230, 230, 250);
            } else if (rarity.equals("UNCOMMON")) {
                borderColor = new Color(173, 255, 47); // 綠色邊框
                bgColor = isDarkTheme ? new Color(30, 80, 30, 80) : new Color(240, 255, 240);
            } else {
                borderColor = new Color(192, 192, 192); // 灰色邊框
                bgColor = isDarkTheme ? new Color(50, 50, 50) : Color.WHITE;
            }
            
            cardPanel.setBackground(bgColor);
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            
            // 卡片文字內容 - 在暗色主題下使用更亮的顏色
            String textStyle = isDarkTheme ? 
                "style='color: white; text-align:center;'" : 
                "style='text-align:center;'";
            String rarityColorHex = getRarityColorForTheme(rarity);
            
            String cardText = String.format(
                "<html><div %s><b>%s</b><br><span style='color:%s'>%s</span> %s<br>類型: %s<br>力量: %d<br><i>%s</i></div></html>",
                textStyle, 
                card.getName(), 
                rarityColorHex,
                card.getRarity(), 
                card.getAttribute(), 
                card.getType(), 
                card.getBasePower(), 
                card.getDescription());
            
            JLabel cardLabel = new JLabel(cardText, SwingConstants.CENTER);
            cardLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 12));
            cardLabel.setForeground(textColor);
            
            cardPanel.add(cardLabel, BorderLayout.CENTER);
            cardDisplayPanel.add(cardPanel);
        }
        
        // 使用JScrollPane以便卡片太多時可以滾動
        JScrollPane scrollPane = new JScrollPane(cardDisplayPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        drawCardPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部控制按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton startGameButton = createStyledButton("開始戰鬥", e -> { showBattlePanel(); });
        JButton backToLobbyButton = createStyledButton("返回大廳", e -> { showLobbyPanel(); });
        
        buttonPanel.add(startGameButton);
        buttonPanel.add(backToLobbyButton);
        drawCardPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 顯示面板
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "DrawCard");
    }
    
    /**
     * 獲取稀有度對應的顏色十六進制代碼，適配不同主題
     */
    private String getRarityColorForTheme(String rarity) {
        if (isDarkTheme) {
            // 暗色主題下的顏色
            switch (rarity) {
                case "LEGENDARY": return "#ffd700"; // 明亮的金色
                case "RARE": return "#87cefa";      // 明亮的藍色 
                case "UNCOMMON": return "#90ee90";  // 明亮的綠色
                default: return "#cccccc";          // 明亮的灰色
            }
        } else {
            // 亮色主題下的顏色
            switch (rarity) {
                case "LEGENDARY": return "#b8860b"; // 深金色
                case "RARE": return "#4169e1";      // 皇家藍
                case "UNCOMMON": return "#228b22";  // 森林綠
                default: return "#696969";          // 深灰色
            }
        }
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
        selectionPanel.removeAll();
        selectionPanel.setLayout(new BorderLayout(10, 10));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 標題
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("選擇10張卡牌進行對戰", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        titlePanel.add(title);
        selectionPanel.add(titlePanel, BorderLayout.NORTH);

        // 卡片選擇區域
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        // 指引文字
        JLabel guidanceLabel = new JLabel("<html>提示: 按住Ctrl鍵可選擇多張卡牌。<br>必須選擇剛好10張卡牌。</html>", 
                                         SwingConstants.CENTER);
        guidanceLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 13));
        guidanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        centerPanel.add(guidanceLabel, BorderLayout.NORTH);
        
        // 卡片列表
        deckListModel = new DefaultListModel<>();
        deckList = new JList<>(deckListModel);
        deckList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deckList.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        // 添加自定義渲染器
        deckList.setCellRenderer(new CardListRenderer());
        
        // 添加選擇計數器標籤
        JLabel selectionCountLabel = new JLabel("已選擇: 0/10", SwingConstants.CENTER);
        selectionCountLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        // 監聽選擇變化
        deckList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedCount = deckList.getSelectedIndices().length;
                selectionCountLabel.setText("已選擇: " + selectedCount + "/10");
                
                // 根據選擇數量更新標籤顏色
                if (selectedCount == 10) {
                    selectionCountLabel.setForeground(new Color(0, 128, 0)); // 綠色
                } else {
                    selectionCountLabel.setForeground(Color.RED);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(deckList);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "可用卡牌"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(selectionCountLabel, BorderLayout.SOUTH);
        selectionPanel.add(centerPanel, BorderLayout.CENTER);

        // 按鈕面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton confirm = createStyledButton("確認選擇", e -> { 
            List<Card> deck = gameController.getPlayerDeck(); 
            int[] sel = deckList.getSelectedIndices();
            if (sel.length != 10) {
                JOptionPane.showMessageDialog(this, "請確切選擇10張卡牌。", "選擇錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Card> selected = new ArrayList<>();
            for (int idx : sel) {
                selected.add(deck.get(idx));
            }
            gameController.setBattleCards(selected);
            showBattlePanel();
        });
        
        JButton back = createStyledButton("返回大廳", e -> { showLobbyPanel(); });
        btnPanel.add(confirm);
        btnPanel.add(back);
        selectionPanel.add(btnPanel, BorderLayout.SOUTH);
        
        selectionPanel.revalidate();
        selectionPanel.repaint();
    }
      /**
     * 卡片列表自定義渲染器，根據卡片稀有度顯示不同顏色背景
     */
    private class CardListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            
            if (value != null) {
                String cardInfo = value.toString();
                // 根據卡片稀有度和主題設置背景顏色
                if (cardInfo.contains("LEGENDARY")) {
                    if (!isSelected) {
                        label.setBackground(isDarkTheme ? 
                            new Color(100, 84, 0, 80) :     // 深色主題下的金色背景
                            new Color(255, 250, 205));      // 淺色主題下的金色背景
                        label.setForeground(isDarkTheme ? new Color(255, 223, 0) : label.getForeground());
                    }
                    label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(255, 215, 0)));
                } else if (cardInfo.contains("RARE")) {
                    if (!isSelected) {
                        label.setBackground(isDarkTheme ? 
                            new Color(40, 70, 120, 80) :    // 深色主題下的藍色背景
                            new Color(230, 230, 250));      // 淺色主題下的藍色背景
                        label.setForeground(isDarkTheme ? new Color(135, 206, 250) : label.getForeground());
                    }
                    label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(70, 130, 180)));
                } else if (cardInfo.contains("UNCOMMON")) {
                    if (!isSelected) {
                        label.setBackground(isDarkTheme ? 
                            new Color(30, 80, 30, 80) :     // 深色主題下的綠色背景
                            new Color(240, 255, 240));      // 淺色主題下的綠色背景
                        label.setForeground(isDarkTheme ? new Color(144, 238, 144) : label.getForeground());
                    }
                    label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(173, 255, 47)));
                } else {
                    if (!isSelected) {
                        label.setBackground(isDarkTheme ? 
                            new Color(50, 50, 50) :         // 深色主題下的灰色背景
                            Color.WHITE);                   // 淺色主題下的白色背景
                        label.setForeground(isDarkTheme ? Color.LIGHT_GRAY : label.getForeground());
                    }
                    label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(192, 192, 192)));
                }
            }
            
            // 設置更好的選擇顏色
            if (isSelected) {
                label.setBackground(isDarkTheme ? 
                    new Color(80, 110, 160) :  // 深色主題下的選中背景色
                    new Color(232, 242, 254)); // 淺色主題下的選中背景色
                label.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
            }
            
            // 增加垂直填充
            label.setBorder(BorderFactory.createCompoundBorder(
                label.getBorder(),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
            
            return label;
        }
    }    /**
     * 自定義卡片清單渲染器，提供詳細資訊懸停顯示功能
     */
    private class EnhancedCardListRenderer extends DefaultListCellRenderer {
        // 淺色主題下的卡片背景顏色
        private final Color lightLegendaryColor = new Color(255, 223, 0, 40);  // 淺金色
        private final Color lightEpicColor = new Color(163, 53, 238, 40);      // 淺紫色
        private final Color lightRareColor = new Color(0, 112, 221, 40);       // 淺藍色
        private final Color lightCommonColor = new Color(200, 200, 200, 40);   // 淺灰色
        
        // 深色主題下的卡片背景顏色
        private final Color darkLegendaryColor = new Color(255, 215, 0, 60);   // 金色
        private final Color darkEpicColor = new Color(190, 110, 255, 60);      // 紫色
        private final Color darkRareColor = new Color(65, 155, 240, 60);       // 藍色
        private final Color darkCommonColor = new Color(150, 150, 150, 60);    // 灰色

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                    int index, boolean isSelected, 
                                                    boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            // 檢查是否超出範圍
            if (index < 0 || index >= gameController.getPlayerDeck().size()) {
                return label;
            }
              // 獲取對應的卡片對象
            Card card = gameController.getPlayerDeck().get(index);
            
            // 使用全局主題變數，而不是嘗試從顏色推斷
            // boolean isDarkTheme = UIManager.getColor("Panel.background") != null && 
            //                     UIManager.getColor("Panel.background").getRed() < 100;
            
            // 設置適當的背景顏色 (如果未被選中)
            if (!isSelected) {
                String rarity = card.getRarity().toString();
                switch (rarity) {
                    case "LEGENDARY":
                    case "SSR":
                        label.setBackground(isDarkTheme ? darkLegendaryColor : lightLegendaryColor);
                        break;
                    case "EPIC":
                    case "SR":
                        label.setBackground(isDarkTheme ? darkEpicColor : lightEpicColor);
                        break;
                    case "RARE":
                    case "R":
                        label.setBackground(isDarkTheme ? darkRareColor : lightRareColor);
                        break;
                    case "COMMON":
                    default:
                        label.setBackground(isDarkTheme ? darkCommonColor : lightCommonColor);
                        break;
                }
                
                // 根據主題調整文字顏色
                if (isDarkTheme) {
                    label.setForeground(new Color(230, 230, 230));
                } else {
                    label.setForeground(new Color(33, 33, 33));
                }            
            }
            
            // 設置懸停文字提示 (詳細卡片資訊)
            // 根據主題設置工具提示顏色
            String bgColor = isDarkTheme ? "#2d3748" : "#f8f9fa";
            String textColor = isDarkTheme ? "#e2e8f0" : "#1a202c";
            String borderColor = isDarkTheme ? "#4a5568" : "#cbd5e0";
            String headerColor = isDarkTheme ? "#63b3ed" : "#3182ce";
            String descBgColor = isDarkTheme ? "#1a202c" : "#edf2f7";
            
            // 根據稀有度設置特殊顏色
            String rarityColor = "#6b7280"; // 默認灰色
            String rarityText = card.getRarity().toString();
            if (rarityText.equals("LEGENDARY") || rarityText.equals("SSR")) {
                rarityColor = isDarkTheme ? "#ffd700" : "#d97706"; // 金色
            } else if (rarityText.equals("EPIC") || rarityText.equals("SR")) {
                rarityColor = isDarkTheme ? "#9f7aea" : "#7e22ce"; // 紫色
            } else if (rarityText.equals("RARE") || rarityText.equals("R")) {
                rarityColor = isDarkTheme ? "#60a5fa" : "#2563eb"; // 藍色
            }
            
            String tooltipText = String.format(
                "<html><div style='background-color:%s; color:%s; padding:12px; border:1px solid %s; border-radius:6px; width:320px;'>" +
                "<h3 style='margin:0 0 8px 0; padding-bottom:6px; border-bottom:1px solid %s; color:%s;'>%s</h3>" +
                "<table style='width:100%%; border-collapse:collapse;'>" +
                "<tr><td style='padding:4px;'><b>稀有度:</b></td><td style='padding:4px; color:%s; font-weight:bold;'>%s</td></tr>" +
                "<tr><td style='padding:4px;'><b>屬性:</b></td><td style='padding:4px;'>%s</td></tr>" +
                "<tr><td style='padding:4px;'><b>類型:</b></td><td style='padding:4px;'>%s</td></tr>" +
                "<tr><td style='padding:4px;'><b>力量:</b></td><td style='padding:4px;'>%d</td></tr>" +
                "</table>" +
                "<div style='margin-top:10px; padding:8px; background-color:%s; border-left:3px solid %s; font-style:italic;'>%s</div>" +
                "</div></html>",
                bgColor, textColor, borderColor,
                borderColor, headerColor, card.getName(),
                rarityColor, card.getRarity(),
                card.getAttribute(),
                card.getType(),
                card.getBasePower(),
                descBgColor, headerColor, card.getDescription()
            );
            
            label.setToolTipText(tooltipText);
            
            // 設置內部邊距
            label.setBorder(BorderFactory.createCompoundBorder(
                label.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // 調整字體
            label.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
            
            return label;
        }
    }

    private void updatePlayerStatsDisplay() {
        if (currentPlayer != null) {
            Player player = currentPlayer; // 可直接使用currentPlayer或gameController.getCurrentPlayer()
            playerLevelLabel.setText("等級: " + player.getLevel());
            playerXpLabel.setText(String.format("經驗值: %d/%d", player.getXp(), player.getXpToNextLevel()));
            xpBar.setMaximum(player.getXpToNextLevel());
            xpBar.setValue(player.getXp());
            xpBar.setString(player.getXp() + " / " + player.getXpToNextLevel());
            playerCurrencyLabel.setText("貨幣: " + player.getCurrency());
            playerRatingLabel.setText("排名分數: " + player.getRating());
        } else {
            playerLevelLabel.setText("等級: -");
            playerXpLabel.setText("經驗值: -/-");
            xpBar.setValue(0);
            xpBar.setString("0 / 0");
            playerCurrencyLabel.setText("貨幣: -");
            playerRatingLabel.setText("排名分數: -");
        }
    }

    /**
     * 初始化排行榜面板，設置控制項和列表。
     */
    private void initializeRankingPanel() {
        rankingPanel.removeAll(); // 清除之前的組件（如果重新初始化）
        rankingPanel.setLayout(new BorderLayout(10, 10)); // 添加BorderLayout區域之間的間隙
        rankingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加面板周圍的填充

        // 標題
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("排行榜", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); // 在標題下方添加填充
        titlePanel.add(title);
        rankingPanel.add(titlePanel, BorderLayout.NORTH);

        // 中央排行榜列表
        rankingListModel = new DefaultListModel<>();
        rankingList = new JList<>(rankingListModel);
        rankingList.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        // 應用自訂單元格渲染器以獲得更好的列表項外觀
        rankingList.setCellRenderer(new CustomRankingRenderer());
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "玩家排名"));
        rankingPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部控制面板（下拉選單、排序按鈕、返回按鈕）
        JPanel controlPanel = new JPanel(new GridBagLayout()); // 使用GridBagLayout獲得更多控制
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // 控制面板上方的填充
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 組件之間的間距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sortByLabel = new JLabel("排序依據：");
        sortByLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // 標籤不需要擴展
        gbc.anchor = GridBagConstraints.LINE_START;
        controlPanel.add(sortByLabel, gbc);

        String[] options = {"等級", "貨幣", "牌位積分"};
        rankingCombo = new JComboBox<>(options);
        rankingCombo.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        rankingCombo.setToolTipText("選擇排序依據");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // 下拉選單佔用可用空間
        controlPanel.add(rankingCombo, gbc);

        JButton sortButton = createStyledButton("排序", e -> {
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
        });
        sortButton.setToolTipText("依照選擇的依據對玩家排名排序");
        sortButton.setMnemonic(KeyEvent.VK_S);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.25; // 按鈕佔用一些空間
        controlPanel.add(sortButton, gbc);

        JButton backButton = createStyledButton("返回大廳", e -> {
            showLobbyPanel();
        });
        backButton.setToolTipText("返回主遊戲大廳");
        backButton.setMnemonic(KeyEvent.VK_B);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.25; // 按鈕佔用一些空間
        controlPanel.add(backButton, gbc);

        rankingPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // 默認顯示時自動加載排名
        SwingUtilities.invokeLater(() -> {
            sortButton.doClick();
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

    /**
     * 自定義渲染器用於排行榜項目
     */
    private class CustomRankingRenderer extends JPanel implements ListCellRenderer<Player> {
        private final JLabel rankLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel levelLabel = new JLabel();
        private final JLabel currencyLabel = new JLabel();
        private final JLabel ratingLabel = new JLabel();
        
        public CustomRankingRenderer() {
            setLayout(new BorderLayout(10, 5));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            
            JPanel leftPanel = new JPanel(new BorderLayout());
            rankLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 16));
            rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rankLabel.setPreferredSize(new Dimension(30, 30));
            leftPanel.add(rankLabel, BorderLayout.CENTER);
            
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 2));
            nameLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 14));
            
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            levelLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 13));
            currencyLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 13));
            ratingLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 13));
            
            statsPanel.add(levelLabel);
            statsPanel.add(currencyLabel);
            statsPanel.add(ratingLabel);
            
            infoPanel.add(nameLabel);
            infoPanel.add(statsPanel);
            
            add(leftPanel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Player> list, 
                                                     Player player, 
                                                     int index, 
                                                     boolean isSelected, 
                                                     boolean cellHasFocus) {
            // 設置排名編號 (從1開始)
            rankLabel.setText("#" + (index + 1));
            
            nameLabel.setText(player.getUsername());
            levelLabel.setText("等級: " + player.getLevel());
            currencyLabel.setText("貨幣: " + player.getCurrency());
            ratingLabel.setText("牌位積分: " + player.getRating());
              // 斑馬紋效果 - 根據主題設置不同顏色
            if (isDarkTheme) {
                if (index % 2 == 0) {
                    setBackground(new Color(50, 55, 65));
                } else {
                    setBackground(new Color(40, 45, 55));
                }
                
                nameLabel.setForeground(Color.WHITE);
                levelLabel.setForeground(Color.LIGHT_GRAY);
                currencyLabel.setForeground(Color.LIGHT_GRAY);
                ratingLabel.setForeground(Color.LIGHT_GRAY);
            } else {
                if (index % 2 == 0) {
                    setBackground(new Color(245, 245, 245));
                } else {
                    setBackground(Color.WHITE);
                }
                
                nameLabel.setForeground(Color.BLACK);
                levelLabel.setForeground(Color.DARK_GRAY);
                currencyLabel.setForeground(Color.DARK_GRAY);
                ratingLabel.setForeground(Color.DARK_GRAY);
            }
              // 設置選擇狀態顏色
            if (isSelected) {
                if (isDarkTheme) {
                                       setBackground(new Color(60, 80, 120));  // 深藍色選擇背景
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 80)),
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(100, 130, 180), 1),
                            BorderFactory.createEmptyBorder(7, 9, 7, 9)
                        )
                    ));
                } else {
                    setBackground(new Color(232, 242, 254));  // 淺藍色選擇背景
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(120, 170, 220), 1),
                            BorderFactory.createEmptyBorder(7, 9, 7, 9)
                        )
                    ));
                }
            } else {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkTheme ? new Color(80, 80, 80) : Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
              // 對前三名使用特殊顏色
            if (index < 3) {
                switch (index) {
                    case 0: // 金色 - 第一名
                        rankLabel.setForeground(new Color(255, 215, 0));
                        rankLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
                        break;
                    case 1: // 銀色 - 第二名
                        rankLabel.setForeground(new Color(192, 192, 192));
                        rankLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
                        break;
                    case 2: // 銅色 - 第三名
                        rankLabel.setForeground(new Color(205, 127, 50));
                        rankLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
                        break;
                }
            } else {
                // 普通排名 - 根據主題選擇顏色
                rankLabel.setForeground(isDarkTheme ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                rankLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 16));
            }
            
            return this;
        }
    }    /**
     * 帶有過濾功能的卡片選擇面板
     */
    private void showSelectionPanel() {
        // 先設置卡片列表模型
        deckListModel = new DefaultListModel<>();
        List<Card> deck = gameController.getPlayerDeck();
        
        // 原始卡牌列表 (用於過濾)
        final List<Card> originalDeck = new ArrayList<>(deck);
        
        // 用於追踪顯示列表中每個項目對應的卡片索引
        final List<Integer> cardIndices = new ArrayList<>();
        
        // 過濾前先顯示所有卡片
        for (int i = 0; i < deck.size(); i++) {
            Card c = deck.get(i);
            deckListModel.addElement(String.format("%s (%s %s, 類型:%s, 力量:%d)",
                c.getName(), c.getRarity(), c.getAttribute(), c.getType(), c.getBasePower()));
            cardIndices.add(i);
        }
        
        // 先清空面板，再初始化
        selectionPanel.removeAll();
        selectionPanel.setLayout(new BorderLayout(10, 10));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 標題
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("選擇10張卡牌進行對戰", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        titlePanel.add(title);
        selectionPanel.add(titlePanel, BorderLayout.NORTH);

        // 卡片選擇區域
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // 指引文字
        JLabel guidanceLabel = new JLabel("<html>提示: 按住Ctrl鍵可選擇多張卡牌。<br>必須選擇剛好10張卡牌。</html>", 
                                         SwingConstants.CENTER);
        guidanceLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 13));
        guidanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        centerPanel.add(guidanceLabel, BorderLayout.NORTH);
          // 使用已設置好的列表模型創建JList
        deckList = new JList<>(deckListModel);
        deckList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deckList.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        // 添加增強版自定義渲染器，支持卡片詳細信息懸停顯示
        deckList.setCellRenderer(new EnhancedCardListRenderer());
        
        // 添加選擇計數器標籤
        JLabel selectionCountLabel = new JLabel("已選擇: 0/10", SwingConstants.CENTER);
        selectionCountLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        // 監聽選擇變化
        deckList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedCount = deckList.getSelectedIndices().length;
                selectionCountLabel.setText("已選擇: " + selectedCount + "/10");
                
                // 根據選擇數量更新標籤顏色
                if (selectedCount == 10) {
                    selectionCountLabel.setForeground(new Color(0, 128, 0)); // 綠色
                } else {
                    selectionCountLabel.setForeground(Color.RED);
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(deckList);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "可用卡牌"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(selectionCountLabel, BorderLayout.SOUTH);
        selectionPanel.add(centerPanel, BorderLayout.CENTER);

        // 添加卡片過濾功能
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 0, 8, 0)
        ));
        
        // 稀有度過濾
        JLabel rarityLabel = new JLabel("稀有度:");
        rarityLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        JComboBox<String> rarityFilter = new JComboBox<>(new String[]{"全部", "傳說 (SSR)", "史詩 (SR)", "罕見 (R)"});
        rarityFilter.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        // 屬性過濾
        JLabel attributeLabel = new JLabel("屬性:");
        attributeLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        JComboBox<String> attributeFilter = new JComboBox<>(new String[]{"全部", "火", "水", "草"});
        attributeFilter.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
          // 重置按鈕
        JButton resetButton = createStyledButton("重置過濾", e -> {
            rarityFilter.setSelectedIndex(0);
            attributeFilter.setSelectedIndex(0);
            
            // 重置列表顯示所有卡片
            deckListModel.clear();
            cardIndices.clear();
            for (int i = 0; i < originalDeck.size(); i++) {
                Card c = originalDeck.get(i);
                deckListModel.addElement(String.format("%s (%s %s, 類型:%s, 力量:%d)",
                    c.getName(), c.getRarity(), c.getAttribute(), c.getType(), c.getBasePower()));
                cardIndices.add(i);
            }
        });
          // 過濾監聽器
        ActionListener filterListener = e -> {
            // 先清空當前列表
            deckListModel.clear();
            cardIndices.clear();
            
            // 應用過濾條件
            int rarityIdx = rarityFilter.getSelectedIndex();
            int attributeIdx = attributeFilter.getSelectedIndex();
            
            for (int i = 0; i < originalDeck.size(); i++) {
                Card c = originalDeck.get(i);
                // 稀有度過濾
                boolean rarityMatch = rarityIdx == 0 || 
                    (rarityIdx == 1 && c.getRarity().toString().equals("SSR")) ||
                    (rarityIdx == 2 && c.getRarity().toString().equals("SR")) ||
                    (rarityIdx == 3 && c.getRarity().toString().equals("R"));
                
                // 屬性過濾
                boolean attributeMatch = attributeIdx == 0 || 
                    (attributeIdx == 1 && c.getAttribute().toString().equals("FIRE")) ||
                    (attributeIdx == 2 && c.getAttribute().toString().equals("WATER")) ||
                    (attributeIdx == 3 && c.getAttribute().toString().equals("GRASS"));
                
                // 如果同時符合兩種過濾條件，則添加到列表
                if (rarityMatch && attributeMatch) {
                    deckListModel.addElement(String.format("%s (%s %s, 類型:%s, 力量:%d)",
                        c.getName(), c.getRarity(), c.getAttribute(), c.getType(), c.getBasePower()));
                    cardIndices.add(i);
                }
            }
        };
        
        // 設置過濾器動作監聽器
        rarityFilter.addActionListener(filterListener);
        attributeFilter.addActionListener(filterListener);
        
        // 添加所有過濾器控件到面板
        filterPanel.add(rarityLabel);
        filterPanel.add(rarityFilter);
        filterPanel.add(attributeLabel);
        filterPanel.add(attributeFilter);
        filterPanel.add(resetButton);
        
        // 修改布局以容納過濾面板
        JPanel topCenterPanel = new JPanel(new BorderLayout());
        topCenterPanel.add(filterPanel, BorderLayout.NORTH);
        
        // 將指引文字移至過濾面板下方
        JPanel guidancePanel = new JPanel();
        guidanceLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        guidancePanel.add(guidanceLabel);
        topCenterPanel.add(guidancePanel, BorderLayout.SOUTH);
        
        // 替換原有北側組件
        centerPanel.remove(guidanceLabel);
        centerPanel.add(topCenterPanel, BorderLayout.NORTH);
        
        // 按鈕面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton confirm = createStyledButton("確認選擇", e -> { 
            int[] sel = deckList.getSelectedIndices();
            if (sel.length != 10) {
                JOptionPane.showMessageDialog(this, "請確切選擇10張卡牌。", "選擇錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
              // 從已經存在的卡牌列表中根據映射的索引取得選擇的卡片
            List<Card> selected = new ArrayList<>();
            for (int idx : sel) {
                // 使用cardIndices來映射回原始的卡牌
                int originalIdx = cardIndices.get(idx);
                selected.add(deck.get(originalIdx));
            }
              // 設置這些卡片用於對戰
            gameController.setBattleCards(selected);
            showBattlePanel();
        });
        
        JButton back = createStyledButton("返回大廳", e -> { showLobbyPanel(); });
        btnPanel.add(confirm);
        btnPanel.add(back);
        selectionPanel.add(btnPanel, BorderLayout.SOUTH);
        
        // 重新繪製面板
        selectionPanel.revalidate();
        selectionPanel.repaint();
        
        // 最後才切換到選擇面板
        CardLayout layout = (CardLayout) mainPanel.getLayout();
        layout.show(mainPanel, "SelectBattleCards");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }
    
    /**
     * 初始化菜單欄，提供更方便的導航和功能訪問
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 遊戲選單
        JMenu gameMenu = new JMenu("遊戲");
        gameMenu.setMnemonic(KeyEvent.VK_G);
        
        JMenuItem homeItem = new JMenuItem("返回大廳", KeyEvent.VK_H);
        homeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        homeItem.addActionListener(e -> showLobbyPanel());
        
        JMenuItem logoutItem = new JMenuItem("登出", KeyEvent.VK_L);
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "確定要登出嗎？", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentPlayer = null; // Fix: Use currentPlayer
                showLoginPanel();
            }
        });
        
        JMenuItem exitItem = new JMenuItem("退出", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "確定要退出遊戲嗎？", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        gameMenu.add(homeItem);
        gameMenu.addSeparator();
        gameMenu.add(logoutItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        // 卡牌選單
        JMenu cardMenu = new JMenu("卡牌");
        cardMenu.setMnemonic(KeyEvent.VK_C);
        
        JMenuItem drawItem = new JMenuItem("抽卡", KeyEvent.VK_D);
        drawItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        drawItem.addActionListener(e -> showDrawOptionsPanel());
        
        JMenuItem deckItem = new JMenuItem("我的牌組", KeyEvent.VK_M);
        deckItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
        deckItem.addActionListener(e -> showDeck());
        
        JMenuItem battleItem = new JMenuItem("開始對戰", KeyEvent.VK_B);
        battleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
        battleItem.addActionListener(e -> {
            if (gameController.getPlayerDeck().size() < 10) {
                JOptionPane.showMessageDialog(this, 
                    "你的牌組中至少需要10張卡牌才能進行對戰。\n請先抽卡獲取更多卡片。", 
                    "卡片不足", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            showSelectionPanel();
        });
        
        cardMenu.add(drawItem);
        cardMenu.add(deckItem);
        cardMenu.add(battleItem);
        
        // 統計選單
        JMenu statsMenu = new JMenu("統計");
        statsMenu.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem rankingItem = new JMenuItem("排行榜", KeyEvent.VK_R);
        rankingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        rankingItem.addActionListener(e -> showRankingPanel());
        
        JMenuItem historyItem = new JMenuItem("對戰歷史", KeyEvent.VK_H);
        historyItem.addActionListener(e -> {
            // 使用之前定義的方法來查詢對戰紀錄
            if (currentPlayer != null) { // Fix: Check currentPlayer
                List<String> records = recordService.getAllRecords(currentPlayer.getUsername()); // Fix: Use currentPlayer.getUsername()
                if (records.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "用戶 " + currentPlayer.getUsername() + " 沒有找到對戰記錄", // Fix: Use currentPlayer.getUsername()
                        "遊戲歷史", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showBattleHistory(records);
                }
            } else {
                JOptionPane.showMessageDialog(this, "請先登入以查看歷史記錄。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        statsMenu.add(rankingItem);
        statsMenu.add(historyItem);
        
        // 設定選單
        JMenu settingsMenu = new JMenu("設定");
        settingsMenu.setMnemonic(KeyEvent.VK_T);
          // 主題選擇
        JMenu themeMenu = new JMenu("主題");
        
        // 默認使用明亮主題，但不在這裡設置selected狀態，而是在applyTheme中設置
        JRadioButtonMenuItem lightTheme = new JRadioButtonMenuItem("明亮主題");
        JRadioButtonMenuItem darkTheme = new JRadioButtonMenuItem("暗黑主題");
        
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightTheme);
        themeGroup.add(darkTheme);
        
        // 只設置事件監聽器，不重複調用showThemeChangeNotification
        lightTheme.addActionListener(e -> {
            if (lightTheme.isSelected()) { // 避免重複觸發
                System.out.println("明亮主題按鈕點擊");
                applyTheme("light");
            }
        });
        
        darkTheme.addActionListener(e -> {
            if (darkTheme.isSelected()) { // 避免重複觸發
                System.out.println("暗黑主題按鈕點擊");
                applyTheme("dark");
            }
        });
        
        themeMenu.add(lightTheme);
        themeMenu.add(darkTheme);
        
        // 音效選項
        JCheckBoxMenuItem soundEffects = new JCheckBoxMenuItem("音效", true);
        soundEffects.addActionListener(e -> toggleSoundEffects());
        
        settingsMenu.add(themeMenu);
        settingsMenu.add(soundEffects);
        
        // 幫助選單
        JMenu helpMenu = new JMenu("幫助");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem howToPlayItem = new JMenuItem("遊戲說明", KeyEvent.VK_P);
        howToPlayItem.addActionListener(e -> showHowToPlay());
        
        JMenuItem aboutItem = new JMenuItem("關於", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(howToPlayItem);
        helpMenu.add(aboutItem);
        
        // 添加所有選單到菜單欄
        menuBar.add(gameMenu);
        menuBar.add(cardMenu);
        menuBar.add(statsMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * 顯示遊戲說明對話框
     */
    private void showHowToPlay() {
        JDialog helpDialog = new JDialog(this, "遊戲說明", true);
        helpDialog.setLayout(new BorderLayout(10, 10));
        helpDialog.setSize(600, 450);
        helpDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 標題
        JLabel titleLabel = new JLabel("如何遊玩 - 卡牌對決：元素抽卡競技場");
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 遊戲說明文本
        String instructions = "<html><body style='width: 450px'>" +
            "<h2>基本規則</h2>" +
            "<p>這是一個卡牌對戰遊戲，玩家通過收集卡牌並與電腦對戰來獲得勝利。</p>" +
            "<h2>遊戲流程</h2>" +
            "<ol>" +
            "<li><b>抽卡：</b> 透過單抽或十連抽來獲取新的卡片。</li>" +
            "<li><b>組建牌組：</b> 在對戰前，從你的卡牌池中選擇10張卡牌。</li>" +
            "<li><b>對戰：</b> 與電腦進行10回合的對戰，每回合出一張卡牌，卡牌力量高者獲勝。</li>" +
            "</ol>" +
            "<h2>卡牌屬性</h2>" +
            "<ul>" +
            "<li><b>稀有度：</b> 分為普通、罕見、史詩和傳說，稀有度越高，卡牌基礎能力越強。</li>" +
            "<li><b>屬性：</b> 包括火、水、風、土等元素屬性，不同屬性之間有相互剋制關係。</li>" +
            "<li><b>力量：</b> 決定卡牌在對戰中的基礎強度。</li>" +
            "</ul>" +
            "<h2>勝利條件</h2>" +
            "<p>在10回合對戰結束後，贏得較多回合的一方獲勝。獲勝可以提升評分和經驗值。</p>" +
            "</body></html>";
        
        JLabel instructionsLabel = new JLabel(instructions);
        instructionsLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        
        // 將說明文本放入滾動面板
        JScrollPane scrollPane = new JScrollPane(instructionsLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = createStyledButton("關閉", e -> helpDialog.dispose());
        buttonPanel.add(closeButton);
        
        contentPanel.add(titleLabel);
        contentPanel.add(scrollPane);
        
        helpDialog.add(contentPanel, BorderLayout.CENTER);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        helpDialog.setVisible(true);
    }
    
    /**
     * 顯示關於對話框
     */
    private void showAbout() {
        JDialog aboutDialog = new JDialog(this, "關於", true);
        aboutDialog.setLayout(new BorderLayout(10, 10));
        aboutDialog.setSize(400, 300);
        aboutDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 標題和版本
        JLabel titleLabel = new JLabel("卡牌對決：元素抽卡競技場");
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("版本 1.2.0");
        versionLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        
        // 關於內容
        String aboutText = "<html><body style='width: 300px; text-align: center;'>" +
            "<p>一個簡單而有趣的卡牌對戰遊戲。</p>" +
            "<p>使用 Java Swing 開發的桌面應用程式。</p>" +
            "<p>© 2025 卡牌遊戲公司。保留所有權利。</p>" +
            "</body></html>";
        
        JLabel aboutLabel = new JLabel(aboutText);
        aboutLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
        aboutLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = createStyledButton("關閉", e -> aboutDialog.dispose());
        buttonPanel.add(closeButton);
        
        contentPanel.add(titleLabel);
        contentPanel.add(versionLabel);
        contentPanel.add(aboutLabel);
        
        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        aboutDialog.setVisible(true);
    }
    
    /**
     * 顯示對戰歷史記錄
     */
    private void showBattleHistory(List<String> records) {
        JDialog historyDialog = new JDialog(this, "遊戲歷史記錄", true);
        historyDialog.setLayout(new BorderLayout(10, 10));
        historyDialog.setSize(500, 400);
        historyDialog.setLocationRelativeTo(this);
        
        // 設置對話框主題顏色
        if (isDarkTheme) {
            historyDialog.getContentPane().setBackground(new Color(33, 37, 43));
        }
        
        // 標題面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (isDarkTheme) {
            titlePanel.setBackground(new Color(33, 37, 43));
        }
        JLabel titleLabel = new JLabel((currentPlayer != null ? currentPlayer.getUsername() : "") + " 的遊戲記錄", SwingConstants.CENTER); // Fix: Use currentPlayer.getUsername()
        titleLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 18));
        if (isDarkTheme) {
            titleLabel.setForeground(Color.WHITE);
        }
        titlePanel.add(titleLabel);
        historyDialog.add(titlePanel, BorderLayout.NORTH);
        
        // 記錄列表
        JPanel recordsPanel = new JPanel();
        recordsPanel.setLayout(new BoxLayout(recordsPanel, BoxLayout.Y_AXIS));
        recordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        if (isDarkTheme) {
            recordsPanel.setBackground(new Color(33, 37, 43));
        }
        
        // 統計資訊
        JLabel statsLabel = new JLabel("總遊戲記錄數: " + records.size());
        statsLabel.setFont(new Font("Microsoft JhengHei UI", Font.ITALIC, 14));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isDarkTheme) {
            statsLabel.setForeground(Color.WHITE);
        }
        recordsPanel.add(statsLabel);
        
        // 添加每條記錄
        int count = 1;
        for (String record : records) {
            JPanel recordPanel = new JPanel();
            recordPanel.setLayout(new BorderLayout(5, 2));
            recordPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
            ));
            
            // 為了使每條記錄更易讀，替換英文成中文並添加更多信息
            String enhancedRecord = record
                .replace("Game record for", "對戰記錄 -")
                .replace("Player", "玩家")
                .replace("Computer", "電腦");
            
            // 添加斑馬紋效果
            if (count % 2 == 0) {
                recordPanel.setBackground(new Color(245, 245, 245));
            }
            
            JLabel recordLabel = new JLabel("#" + count + ": " + enhancedRecord);
            recordLabel.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 14));
            recordPanel.add(recordLabel, BorderLayout.CENTER);
            
            recordsPanel.add(recordPanel);
            count++;
        }
        
        // 添加記錄到滾动面板
        JScrollPane scrollPane = new JScrollPane(recordsPanel);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        
        // 底部按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = createStyledButton("關閉", e -> historyDialog.dispose());
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 顯示對話框
        historyDialog.setVisible(true);
    }
    
    /**
     * 應用主題設置
     */    private void applyTheme(String themeName) {
        System.out.println("切換主題到: " + themeName);
        
        // 更新主題狀態變數
        isDarkTheme = "dark".equals(themeName);
        System.out.println("isDarkTheme設置為: " + isDarkTheme);
        
        if ("dark".equals(themeName)) {
            // 設定暗黑主題顏色 - 使用更柔和的深色調，提高對比度
            Color bgColor = new Color(33, 37, 43);          // 較柔和的深藍灰色背景
            Color textColor = new Color(238, 238, 238);     // 近白色文字，提高可讀性
            Color buttonBgColor = new Color(59, 66, 82);    // 按鈕背景色，與主背景區分
            Color accentColor = new Color(106, 127, 219);   // 強調色（用於高亮、邊框）
            Color fieldBgColor = new Color(45, 49, 58);     // 文本輸入區背景色
            
            // 設置主要UI屬性
            UIManager.put("Panel.background", bgColor);
            UIManager.put("OptionPane.background", bgColor);
            UIManager.put("OptionPane.messageForeground", textColor);
            
            // 文字和標籤
            UIManager.put("Label.foreground", textColor);
            UIManager.put("Label.background", bgColor);
            
            // 按鈕相關
            UIManager.put("Button.background", buttonBgColor);
            UIManager.put("Button.foreground", textColor);
            UIManager.put("Button.select", accentColor);
            UIManager.put("Button.focus", accentColor);
            UIManager.put("Button.border", BorderFactory.createLineBorder(accentColor.darker(), 1));
            
            // 文字輸入區域
            UIManager.put("TextField.background", fieldBgColor);
            UIManager.put("TextField.foreground", textColor);
            UIManager.put("TextField.caretForeground", textColor);
            UIManager.put("TextField.selectionBackground", accentColor);
            
            UIManager.put("TextArea.background", fieldBgColor);
            UIManager.put("TextArea.foreground", textColor);
            UIManager.put("TextArea.caretForeground", textColor);
            UIManager.put("TextArea.selectionBackground", accentColor);
            
            // 列表相關
            UIManager.put("List.background", fieldBgColor);
            UIManager.put("List.foreground", textColor);
            UIManager.put("List.selectionBackground", accentColor);
            UIManager.put("List.selectionForeground", Color.WHITE);
            
            // 設置菜單顏色
            UIManager.put("MenuBar.background", buttonBgColor);
            UIManager.put("MenuBar.foreground", textColor);
            UIManager.put("Menu.background", buttonBgColor);
            UIManager.put("Menu.foreground", textColor);
            UIManager.put("Menu.selectionBackground", accentColor);
            UIManager.put("Menu.selectionForeground", Color.WHITE);
            
            UIManager.put("MenuItem.background", buttonBgColor);
            UIManager.put("MenuItem.foreground", textColor);
            UIManager.put("MenuItem.selectionBackground", accentColor);
            UIManager.put("MenuItem.selectionForeground", Color.WHITE);
            UIManager.put("MenuItem.acceleratorForeground", textColor);
            
            // RadioButtonMenuItem相關
            UIManager.put("RadioButtonMenuItem.background", buttonBgColor);
            UIManager.put("RadioButtonMenuItem.foreground", textColor);
            UIManager.put("RadioButtonMenuItem.selectionBackground", accentColor);
            UIManager.put("RadioButtonMenuItem.selectionForeground", Color.WHITE);
            UIManager.put("RadioButtonMenuItem.acceleratorForeground", textColor);
            
            // 捲動條
            UIManager.put("ScrollBar.background", bgColor);
            UIManager.put("ScrollBar.thumb", buttonBgColor);
            UIManager.put("ScrollBar.thumbDarkShadow", bgColor.darker());
            UIManager.put("ScrollBar.thumbHighlight", buttonBgColor.brighter());
            UIManager.put("ScrollBar.thumbShadow", buttonBgColor.darker());
            UIManager.put("ScrollBar.track", bgColor);
            
            // 下拉選單
            UIManager.put("ComboBox.background", fieldBgColor);
            UIManager.put("ComboBox.foreground", textColor);
            UIManager.put("ComboBox.selectionBackground", accentColor);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
              
            // 表格
            UIManager.put("Table.background", fieldBgColor);
            UIManager.put("Table.foreground", textColor);
            UIManager.put("Table.selectionBackground", accentColor);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", bgColor.brighter());
            
            // 子標題
            UIManager.put("TitledBorder.titleColor", textColor);
            
        } else {
            // 設定明亮主題 - 溫暖柔和的配色
            Color bgColor = new Color(248, 248, 252);       // 淡藍灰色背景，不刺眼
            Color textColor = new Color(33, 33, 33);        // 深灰近黑色文字，提高可讀性
            Color buttonBgColor = new Color(210, 230, 255); // 淡藍色按鈕背景
            Color accentColor = new Color(70, 105, 210);    // 藍色強調色
            Color fieldBgColor = new Color(255, 255, 255);  // 純白色輸入區背景
            
            // 設置主要UI屬性
            UIManager.put("Panel.background", bgColor);
            UIManager.put("OptionPane.background", bgColor);
            UIManager.put("OptionPane.messageForeground", textColor);
            
            // 文字和標籤
            UIManager.put("Label.foreground", textColor);
            UIManager.put("Label.background", bgColor);
            
            // 按鈕相關
            UIManager.put("Button.background", buttonBgColor);
            UIManager.put("Button.foreground", textColor.darker());
            UIManager.put("Button.select", accentColor);
            UIManager.put("Button.focus", accentColor);
            UIManager.put("Button.border", BorderFactory.createLineBorder(accentColor, 1));
            
            // 文字輸入區域
            UIManager.put("TextField.background", fieldBgColor);
            UIManager.put("TextField.foreground", textColor);
            UIManager.put("TextField.caretForeground", textColor);
            UIManager.put("TextField.selectionBackground", accentColor.brighter());
            
            UIManager.put("TextArea.background", fieldBgColor);
            UIManager.put("TextArea.foreground", textColor);
            UIManager.put("TextArea.caretForeground", textColor);
            UIManager.put("TextArea.selectionBackground", accentColor.brighter());
            
            // 列表相關
            UIManager.put("List.background", fieldBgColor);
            UIManager.put("List.foreground", textColor);
            UIManager.put("List.selectionBackground", accentColor.brighter());
            UIManager.put("List.selectionForeground", Color.BLACK);
            
            // 設置菜單顏色
            UIManager.put("MenuBar.background", buttonBgColor);
            UIManager.put("MenuBar.foreground", textColor);
            UIManager.put("Menu.background", buttonBgColor);
            UIManager.put("Menu.foreground", textColor);
            UIManager.put("Menu.selectionBackground", accentColor.brighter());
            UIManager.put("Menu.selectionForeground", Color.BLACK);
            
            UIManager.put("MenuItem.background", buttonBgColor);
            UIManager.put("MenuItem.foreground", textColor);
            UIManager.put("MenuItem.selectionBackground", accentColor.brighter());
            UIManager.put("MenuItem.selectionForeground", Color.BLACK);
            UIManager.put("MenuItem.acceleratorForeground", textColor);
            
            // RadioButtonMenuItem相關
            UIManager.put("RadioButtonMenuItem.background", buttonBgColor);
            UIManager.put("RadioButtonMenuItem.foreground", textColor);
            UIManager.put("RadioButtonMenuItem.selectionBackground", accentColor.brighter());
            UIManager.put("RadioButtonMenuItem.selectionForeground", Color.BLACK);
            UIManager.put("RadioButtonMenuItem.acceleratorForeground", textColor);
            
            // 捲動條
            UIManager.put("ScrollBar.background", bgColor);
            UIManager.put("ScrollBar.thumb", buttonBgColor);
            UIManager.put("ScrollBar.thumbDarkShadow", bgColor.darker());
            UIManager.put("ScrollBar.thumbHighlight", buttonBgColor.brighter());
            UIManager.put("ScrollBar.thumbShadow", buttonBgColor.darker());
            UIManager.put("ScrollBar.track", bgColor);
            
            // 下拉選單
            UIManager.put("ComboBox.background", fieldBgColor);
            UIManager.put("ComboBox.foreground", textColor);
            UIManager.put("ComboBox.selectionBackground", accentColor.brighter());
            UIManager.put("ComboBox.selectionForeground", Color.BLACK);
            
            // 表格
            UIManager.put("Table.background", fieldBgColor);
            UIManager.put("Table.foreground", textColor);
            UIManager.put("Table.selectionBackground", accentColor.brighter());
            UIManager.put("Table.selectionForeground", Color.BLACK);
            UIManager.put("Table.gridColor", bgColor.darker());
            
            // 子標題
            UIManager.put("TitledBorder.titleColor", textColor);
        }
        
        // 設置自訂字體 - 在兩個主題下統一設置
        setUIFont(new FontUIResource("Microsoft JhengHei UI", Font.PLAIN, 12));
          
        // 更新所有組件
        SwingUtilities.updateComponentTreeUI(this);
        
        // 更新菜單中的主題選擇狀態 - 在更新UI之後執行
        updateThemeMenuSelection(themeName);
        
        // 最後顯示通知
        showThemeChangeNotification(themeName);
    }
      /**
     * 更新主題菜單中的選擇狀態
     */
    private void updateThemeMenuSelection(String themeName) {
        // 找到菜單欄
        JMenuBar menuBar = getJMenuBar();
        if (menuBar == null) return;
        
        // 輸出調試信息
        System.out.println("更新主題菜單選擇: " + themeName);
        System.out.println("菜單欄項目數量: " + menuBar.getMenuCount());
        
        // 尋找設定選單
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                System.out.println("菜單名稱: " + menu.getText());
                if ("設定".equals(menu.getText())) {
                    System.out.println("找到設定選單, 項目數量: " + menu.getItemCount());
                    
                    // 尋找主題子選單
                    for (int j = 0; j < menu.getItemCount(); j++) {
                        if (menu.getItem(j) instanceof JMenu) {
                            JMenu subMenu = (JMenu)menu.getItem(j);
                            System.out.println("子選單名稱: " + subMenu.getText());
                            
                            if ("主題".equals(subMenu.getText())) {
                                System.out.println("找到主題選單, 項目數量: " + subMenu.getItemCount());
                                
                                JMenu themeMenu = subMenu;
                                // 更新單選按鈕狀態
                                for (int k = 0; k < themeMenu.getItemCount(); k++) {
                                    if (themeMenu.getItem(k) instanceof JRadioButtonMenuItem) {
                                        JRadioButtonMenuItem item = (JRadioButtonMenuItem) themeMenu.getItem(k);
                                        System.out.println("選單項: " + item.getText() + ", 當前選中狀態: " + item.isSelected());
                                        
                                        if ("明亮主題".equals(item.getText())) {
                                            item.setSelected("light".equals(themeName));
                                            System.out.println("設置明亮主題選中狀態為: " + "light".equals(themeName));
                                        } else if ("暗黑主題".equals(item.getText())) {
                                            item.setSelected("dark".equals(themeName));
                                            System.out.println("設置暗黑主題選中狀態為: " + "dark".equals(themeName));
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * 切換音效開關
     */
    private boolean soundEnabled = true;
    
    private void toggleSoundEffects() {
        soundEnabled = !soundEnabled;
    }
    
    /**
     * 播放音效
     */
    private void playSound(String soundType) {
        if (!soundEnabled) return;
        
        // 這裡可以實現音效播放功能
        // 需要添加相應的音效檔案和播放程式碼
    }
    
    /**
     * 顯示動畫效果
     */
    private void showAnimationEffect(String type) {
        if (!soundEnabled) return; // 如果音效關閉，則動畫也不顯示
        
        JWindow animWindow = new JWindow(this);
        JPanel animPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if ("win".equals(type)) {
                    // 勝利動畫
                    g2d.setColor(new Color(0, 128, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 36));
                    
                    String text = "恭喜勝利！";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
                } else if ("lose".equals(type)) {
                    // 失敗動畫
                    g2d.setColor(new Color(139, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 36));
                    
                    String text = "戰敗了...";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
                } else if ("draw".equals(type)) {
                    // 平局動畫
                    g2d.setColor(new Color(128, 128, 128, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 36));
                    
                    String text = "平局！";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
                } else if ("card_draw".equals(type)) {
                    // 抽卡動畫
                    g2d.setColor(new Color(30, 144, 255, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 36));
                    
                    String text = "抽取卡片！";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
                }
            }
        };
        
        animPanel.setPreferredSize(new Dimension(400, 200));
        animWindow.setContentPane(animPanel);
        animWindow.pack();
        
        // 讓動畫視窗居中顯示在主視窗上方
        animWindow.setLocationRelativeTo(this);
        animWindow.setVisible(true);
        
        // 播放相應音效
        playSound(type);
        
        // 顯示一段時間後自動消失
        Timer timer = new Timer(1500, e -> animWindow.dispose());
        timer.setRepeats(false);
        timer.start();
    }
      /**
     * 顯示主題切換通知
     */
    private void showThemeChangeNotification(String themeName) {
        System.out.println("顯示主題切換通知: " + themeName);
        
        // 創建自定義通知面板
        JPanel notificationPanel = new JPanel(new BorderLayout(10, 10));
        notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        String message = "light".equals(themeName) ? 
                "已切換至明亮主題" : "已切換至暗黑主題";
        
        // 調整圖標標籤：不使用圖片資源，改為文字圖標
        JLabel iconLabel = new JLabel("light".equals(themeName) ? "☀" : "☾");
        iconLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Microsoft JhengHei UI", Font.BOLD, 14));
        
        notificationPanel.add(iconLabel, BorderLayout.WEST);
        notificationPanel.add(messageLabel, BorderLayout.CENTER);
        
        // 創建對話框但不使用模態，這樣它會自動消失
        JDialog notification = new JDialog(this);
        notification.setUndecorated(true); // 移除標題欄和邊框
        notification.setContentPane(notificationPanel);
        notification.pack();
        
        // 設置位置在視窗右下角
        Point location = getLocation();
        Dimension size = getSize();
        notification.setLocation(
            location.x + size.width - notification.getWidth() - 20,
            location.y + size.height - notification.getHeight() - 20
        );
        
        // 顯示通知
        notification.setVisible(true);
        
        // 設置計時器在幾秒後自動關閉通知
        Timer timer = new Timer(2000, e -> {
            notification.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Inner class for handling login asynchronously
    private class LoginWorker extends SwingWorker<Player, Void> {
        private final String username;
        private final String password;
        private Player loggedInPlayer = null;
        private String errorMessage = null;

        public LoginWorker(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Player doInBackground() throws Exception {
            try {
                // Simulate network latency or long-running task
                // Thread.sleep(2000);
                loggedInPlayer = recordService.loginUser(username, password);
                if (loggedInPlayer != null) {
                    // currentPlayer = loggedInPlayer; // This was moved from here
                    // gameController.setCurrentPlayer(loggedInPlayer); // Ensure GameController has this method or similar logic
                    // It's better to set currentPlayer in done() after get() to ensure it's the result of the background task.
                }
                return loggedInPlayer;
            } catch (Exception ex) {
                // It's generally better to catch specific exceptions
                errorMessage = "登入時發生錯誤：" + ex.getMessage();
                ex.printStackTrace(); // For debugging, consider logging this instead
                return null;
            }
        }

        @Override
        protected void done() {
            loginButton.setEnabled(true); // Re-enable button
            // Optionally, hide loading indicator here
            try {
                Player resultPlayer = get(); // Get the result from doInBackground
                if (resultPlayer != null) {
                    currentPlayer = resultPlayer; // Set currentPlayer with the successfully logged-in player
                    gameController.setCurrentPlayer(currentPlayer); // Pass player to controller
                    
                    // 載入玩家的卡片收藏 - 從資料庫載入玩家的卡片庫
                    gameController.loadPlayerDeck(currentPlayer.getUsername(), recordService);
                    System.out.println("[LOGIN] 已從資料庫載入玩家 " + currentPlayer.getUsername() + " 的卡片，共 " + gameController.getPlayerDeck().size() + " 張");

                    statusLabel.setText("登入成功！歡迎 " + currentPlayer.getUsername());
                    usernameField.setText(""); // Clear fields
                    passwordField.setText("");
                    
                    updateLobbyInfo(); 
                    updatePlayerStatsDisplay(); // This will use the new currentPlayer
                    // updatePlayerCurrencyLabel(); // updatePlayerStatsDisplay should cover this
                    // updatePlayerRatingLabel(); // updatePlayerStatsDisplay should cover this
                    
                    cardLayout.show(mainPanel, "Lobby");
                } else {
                    if (errorMessage != null) {
                        JOptionPane.showMessageDialog(GameGUI.this, errorMessage, "登入失敗", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText(errorMessage);
                    } else {
                        JOptionPane.showMessageDialog(GameGUI.this, "無效的使用者名稱或密碼。", "登入失敗", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("登入失敗：無效的使用者名稱或密碼。");
                    }
                    passwordField.setText(""); // Clear password field only
                }
            } catch (Exception ex) {
                // This catch block handles exceptions from get() or during UI updates
                JOptionPane.showMessageDialog(GameGUI.this, "登入處理完成後發生錯誤：" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("登入處理完成後發生錯誤。");
                ex.printStackTrace(); // For debugging, consider logging this instead
            }
        }
    }

    // Placeholder for methods that might be called in LoginWorker.done()
    // Ensure these methods exist and are correctly implemented in your GameGUI class.
    private void updateLobbyInfo() {
        // Example: lobbyWelcomeLabel.setText("Welcome, " + currentPlayer.getUsername());
        // This method should refresh any information displayed on the lobby panel
        // that depends on the currentPlayer.
        if (currentPlayer != null) {
            // Assuming you have labels like lobbyUsernameLabel, lobbyCurrencyLabel, lobbyRatingLabel
            // Make sure these labels are declared as fields in GameGUI
            // For example:
            // lobbyUsernameLabel.setText("玩家: " + currentPlayer.getUsername());
            // lobbyCurrencyLabel.setText("貨幣: " + currentPlayer.getCurrency());
            // lobbyRatingLabel.setText("評分: " + currentPlayer.getRating());
            System.out.println("Lobby info updated for " + currentPlayer.getUsername());
        }
    }

    // Ensure updatePlayerCurrencyLabel and updatePlayerRatingLabel are implemented
    private void updatePlayerCurrencyLabel() {
        if (currentPlayer != null && playerCurrencyLabel != null) {
            playerCurrencyLabel.setText("貨幣: " + currentPlayer.getCurrency());
        }
    }

    private void updatePlayerRatingLabel() {
        if (currentPlayer != null && playerRatingLabel != null) {
            playerRatingLabel.setText("評分: " + currentPlayer.getRating());
        }
    }


} // End of GameGUI class
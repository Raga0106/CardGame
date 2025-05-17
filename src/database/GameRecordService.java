package database;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import model.Player;

/**
 * Service for managing game records in an SQLite database.
 */
public class GameRecordService {
    private static final String DB_FILENAME = "game_records.db";
    // 動態計算應用程式所在的資料夾，並定位 data 子目錄
    public static final String DB_URL;
    static {
        try {
            // 取得程式碼（jar 或 exe）位置
            Path codePath = Paths.get(GameRecordService.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            // jar 在 app 資料夾下，data 與 jar 同層
            Path appDir = codePath.getParent();
            Path dataDir = appDir.resolve("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("[DB] Created data directory at " + dataDir.toAbsolutePath());
            }
            Path dbFile = dataDir.resolve(DB_FILENAME);
            DB_URL = "jdbc:sqlite:" + dbFile.toAbsolutePath();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize database URL: " + e.getMessage());
        }
    }

    /**
     * Initializes the database by creating the necessary tables if they don't exist.
     * Also ensures the default admin account exists.
     */
    public GameRecordService() {
        System.out.println("[DB] Using DB URL: " + DB_URL);
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {

            // 檢查所有必要的資料表是否已存在
            boolean recordTableExists = false;
            ResultSet rs = connection.getMetaData().getTables(null, null, "record", null);
            if (rs.next()) {
                recordTableExists = true;
            }

            if (!recordTableExists) {
                statement.execute("CREATE TABLE IF NOT EXISTS record (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL," +
                        "player_name TEXT NOT NULL," +
                        "wins INTEGER NOT NULL," +
                        "losses INTEGER NOT NULL," +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ");");
                System.out.println("[DB] 'record' table created.");
            } else {
                System.out.println("[DB] 'record' table already exists.");
            }

            // 檢查 players 資料表是否已存在
            boolean playersTableExists = false;
            rs = connection.getMetaData().getTables(null, null, "players", null);
            if (rs.next()) {
                playersTableExists = true;
            }

            if (!playersTableExists) {
                // Create players table with password, level, xp, currency, rating
                statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "username TEXT PRIMARY KEY NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "level INTEGER DEFAULT 1, " +
                        "xp INTEGER DEFAULT 0, " +
                        "currency INTEGER DEFAULT 1000, " +
                        "rating INTEGER DEFAULT 1000" +
                        ");");
                System.out.println("[DB] 'players' table created with password column.");
            } else {
                System.out.println("[DB] 'players' table already exists.");
                // Ensure all necessary columns exist in players table for backward compatibility
                try { statement.execute("ALTER TABLE players ADD COLUMN password TEXT"); System.out.println("[DB] Added missing column 'password' to players"); } catch (SQLException ignored) {}
                try { statement.execute("ALTER TABLE players ADD COLUMN level INTEGER DEFAULT 1"); System.out.println("[DB] Added missing column 'level' to players"); } catch (SQLException ignored) {}
                try { statement.execute("ALTER TABLE players ADD COLUMN xp INTEGER DEFAULT 0"); System.out.println("[DB] Added missing column 'xp' to players"); } catch (SQLException ignored) {}
                try { statement.execute("ALTER TABLE players ADD COLUMN currency INTEGER DEFAULT 1000"); System.out.println("[DB] Added missing column 'currency' to players"); } catch (SQLException ignored) {}
                try { statement.execute("ALTER TABLE players ADD COLUMN rating INTEGER DEFAULT 1000"); System.out.println("[DB] Added missing column 'rating' to players"); } catch (SQLException ignored) {}
            }

            // 檢查 deck 資料表是否已存在
            boolean deckTableExists = false;
            rs = connection.getMetaData().getTables(null, null, "deck", null);
            if (rs.next()) {
                deckTableExists = true;
            }

            if (!deckTableExists) {
                statement.execute("CREATE TABLE IF NOT EXISTS deck (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL," +
                        "card_name TEXT NOT NULL," +
                        "attribute TEXT NOT NULL," +
                        "rarity TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "description TEXT," +
                        "base_power INTEGER NOT NULL" +
                        ");");
                System.out.println("[DB] 'deck' table created.");
            } else {
                System.out.println("[DB] 'deck' table already exists.");
            }

            // 確保管理員帳號存在
            statement.execute("INSERT OR IGNORE INTO players (username, password) VALUES ('admin', 'admin');");
            System.out.println("[DB] Admin account ensured.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves a game record to the database, binding it to a specific username.
     * @param username The username associated with the record.
     * @param playerName The name of the player.
     * @param wins The number of wins.
     * @param losses The number of losses.
     */
    public void saveRecord(String username, String playerName, int wins, int losses) {
        String insertSQL = "INSERT INTO record (username, player_name, wins, losses) VALUES (?, ?, ?, ?);";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, playerName);
            preparedStatement.setInt(3, wins);
            preparedStatement.setInt(4, losses);
            preparedStatement.executeUpdate();
            System.out.println("[DB] Record saved: Username=" + username + ", PlayerName=" + playerName + ", Wins=" + wins + ", Losses=" + losses);
        } catch (SQLException e) {
            System.err.println("[DB] Error saving record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all game records for a specific username from the database.
     * @param username The username whose records are to be retrieved.
     */
    public void printAllRecords(String username) {
        System.out.println("[DB] Checking records for username: " + username);
        String querySQL = "SELECT * FROM record WHERE username = ? ORDER BY timestamp DESC;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean hasRecords = false;
                while (resultSet.next()) {
                    hasRecords = true;
                    System.out.printf("ID: %d, Username: %s, Player: %s, Wins: %d, Losses: %d, Timestamp: %s\n",
                            resultSet.getInt("id"),
                            username,
                            resultSet.getString("player_name"),
                            resultSet.getInt("wins"),
                            resultSet.getInt("losses"),
                            resultSet.getString("timestamp"));
                }
                if (!hasRecords) {
                    System.out.println("[DB] No records found for username: " + username);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registers a new user with username and password.
     * @param username The username.
     * @param password The password.
     * @return true if registration successful, false if username exists or other error occurs.
     */
    public boolean registerUser(String username, String password) {
        String checkUserSQL = "SELECT username FROM players WHERE username = ?";
        String insertUserSQL = "INSERT INTO players (username, password) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = connection.prepareStatement(checkUserSQL);
             PreparedStatement insertStmt = connection.prepareStatement(insertUserSQL)) {

            System.out.println("[DB] registerUser SQL: " + checkUserSQL + ", then " + insertUserSQL);

            // Check if the username already exists
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.err.println("Registration failed: Username already exists (" + username + ").");
                    return false;
                }
            }

            // Insert the new user
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            // Diagnostic: print table content after registration
            System.out.println("[DB] After registration, players table content:");
            checkDatabaseContent();
            System.out.println("User registered successfully: " + username);
            return true;

        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates a user's login credentials.
     * @param username The username.
     * @param password The password.
     * @return true if credentials are valid.
     */
    public boolean loginUser(String username, String password) {
        String queryUserSQL = "SELECT password FROM players WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = connection.prepareStatement(queryUserSQL)) {

            System.out.println("[DB] loginUser SQL: " + queryUserSQL);

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password").trim(); // Trim whitespace
                    boolean isValid = password.trim().equals(storedPassword);
                    if (isValid) {
                        System.out.println("Login successful for user: " + username);
                    } else {
                        System.err.println("Login failed: Incorrect password for user: " + username);
                        System.err.println("Expected: " + storedPassword + ", Provided: " + password.trim());
                    }
                    return isValid;
                } else {
                    System.err.println("Login failed: Username not found (" + username + ").");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks the content of the players table in the database.
     */
    public void checkDatabaseContent() {
        String queryPlayersSQL = "SELECT username, password, level, xp, currency, rating FROM players;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(queryPlayersSQL)) {
            System.out.println("[DB Debug] Players table content:");
            while (rs.next()) {
                System.out.printf("Username: %s, Password: %s, Level: %d, XP: %d, Currency: %d, Rating: %d\n",
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getInt("level"),
                    rs.getInt("xp"),
                    rs.getInt("currency"),
                    rs.getInt("rating"));
            }
        } catch (SQLException e) {
            System.err.println("[DB Debug] Error checking database content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all records from the database. Only accessible by admin users.
     * @param username The username attempting to clear the database.
     * @return true if the operation is successful, false otherwise.
     */
    public boolean clearDatabase(String username) {
        if (!"admin".equals(username)) {
            System.err.println("Permission denied: Only admin can clear the database.");
            return false;
        }

        String deleteRecordsSQL = "DELETE FROM record;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteRecordsSQL);
            System.out.println("All records have been cleared by admin.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clears all records or cards from the database based on the specified type. Only accessible by admin users.
     * @param username The username attempting to clear the database.
     * @param type The type of data to clear: "records" or "cards".
     * @return true if the operation is successful, false otherwise.
     */
    public boolean clearDatabaseByType(String username, String type) {
        if (!"admin".equals(username)) {
            System.err.println("Permission denied: Only admin can clear the database.");
            return false;
        }

        String deleteSQL;
        if ("records".equalsIgnoreCase(type)) {
            deleteSQL = "DELETE FROM record;";
        } else if ("cards".equalsIgnoreCase(type)) {
            deleteSQL = "DELETE FROM cards;"; // Assuming a 'cards' table exists
        } else {
            System.err.println("Invalid type specified. Use 'records' or 'cards'.");
            return false;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSQL);
            System.out.println("All " + type + " have been cleared by admin.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 新增保存玩家卡片到資料庫的方法
    public void saveCardToDeck(String username, model.Card card) {
        String insertSQL = "INSERT INTO deck (username, card_name, attribute, rarity, type, description, base_power) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = connection.prepareStatement(insertSQL)) {
            ps.setString(1, username);
            ps.setString(2, card.getName());
            ps.setString(3, card.getAttribute().name());
            ps.setString(4, card.getRarity().name());
            ps.setString(5, card.getType().name());
            ps.setString(6, card.getDescription());
            ps.setInt(7, card.getBasePower());
            ps.executeUpdate();
            System.out.println("[DB] Card saved to deck: " + card.getName());
        } catch (SQLException e) {
            System.err.println("[DB] Error saving card to deck: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 新增從資料庫載入玩家卡片的方法
    public java.util.List<model.Card> loadDeck(String username) {
        java.util.List<model.Card> deck = new java.util.ArrayList<>();
        String querySQL = "SELECT * FROM deck WHERE username = ?;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = connection.prepareStatement(querySQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Card card = new model.Card(
                        rs.getString("card_name"),
                        model.Attribute.valueOf(rs.getString("attribute")),
                        model.Rarity.valueOf(rs.getString("rarity")),
                        model.CardType.valueOf(rs.getString("type")),
                        rs.getString("description"),
                        rs.getInt("base_power")
                    );
                    deck.add(card);
                    System.out.println("[DB] Card loaded from deck: " + card.getName());
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading deck: " + e.getMessage());
            e.printStackTrace();
        }
        return deck;
    }

    public List<String> getAllRecords(String username) {
        List<String> records = new ArrayList<>();
        String querySQL = "SELECT * FROM record WHERE username = ? ORDER BY timestamp DESC;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String record = String.format("Player: %s, Wins: %d, Losses: %d, Time: %s",
                            resultSet.getString("player_name"),
                            resultSet.getInt("wins"),
                            resultSet.getInt("losses"),
                            resultSet.getString("timestamp"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving records: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    public Player loadPlayerData(String username) {
        String sql = "SELECT level, xp, currency, rating FROM players WHERE username = ?";
        System.out.println("[DB] loadPlayerData SQL: " + sql + ", user=" + username);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("level");
                int xp = rs.getInt("xp");
                int currency = rs.getInt("currency");
                int rating = rs.getInt("rating");
                System.out.println(String.format("[DB] Loaded player %s: level=%d, xp=%d, currency=%d, rating=%d", username, level, xp, currency, rating));
                return new Player(username, level, xp, currency, rating);
            }
        } catch (SQLException e) {
            System.err.println("Error loading player data: " + e.getMessage());
        }
        // If player data not found, create default
        Player newPlayer = new Player(username, 1, 0, 1000, 1000);
        if (savePlayerData(newPlayer)) {
            System.out.println("Created new player data entry for: " + username);
            return newPlayer;
        }
        return null;
    }

    public boolean savePlayerData(Player player) {
        if (player == null) return false;
        String sql = "UPDATE players SET level = ?, xp = ?, currency = ?, rating = ? WHERE username = ?";
        System.out.println(String.format("[DB] savePlayerData SQL: %s, player=%s level=%d xp=%d currency=%d rating=%d", sql,
                player.getUsername(), player.getLevel(), player.getXp(), player.getCurrency(), player.getRating()));
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, player.getLevel());
            pstmt.setInt(2, player.getXp());
            pstmt.setInt(3, player.getCurrency());
            pstmt.setInt(4, player.getRating());
            pstmt.setString(5, player.getUsername());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                // No existing row updated, insert new one preserving password
                String insert = "INSERT INTO players (username, password, level, xp, currency, rating) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setString(1, player.getUsername());
                    ins.setString(2, player.getUsername()); // fallback password to username if missing
                    ins.setInt(3, player.getLevel());
                    ins.setInt(4, player.getXp());
                    ins.setInt(5, player.getCurrency());
                    ins.setInt(6, player.getRating());
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error saving player data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all players from the database with their stats.
     * @return List of Player objects.
     */
    public List<Player> loadAllPlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT username, level, xp, currency, rating FROM players";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String user = rs.getString("username");
                int level = rs.getInt("level");
                int xp = rs.getInt("xp");
                int currency = rs.getInt("currency");
                int rating = rs.getInt("rating");
                players.add(new Player(user, level, xp, currency, rating));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading all players: " + e.getMessage());
            e.printStackTrace();
        }
        return players;
    }
}
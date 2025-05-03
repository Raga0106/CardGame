package database;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service for managing game records in an SQLite database.
 */
public class GameRecordService {
    private static final String DB_FILENAME = "game_records.db";
    private static final Path DATA_DIR = Paths.get("data");
    public static final String DB_URL;
    static {
        try {
            // 確保 data 資料夾存在於專案根目錄
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
                System.out.println("[DB] Created data directory at " + DATA_DIR.toAbsolutePath());
            }
            // 遷移舊的 root 資料庫到 data 資料夾
            Path rootDb = Paths.get(DB_FILENAME);
            Path newDb = DATA_DIR.resolve(DB_FILENAME);
            if (Files.exists(rootDb) && !Files.exists(newDb)) {
                Files.move(rootDb, newDb, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[DB] Migrated database from root to " + newDb.toAbsolutePath());
            }
            // 設定絕對路徑的 SQLite URL
            DB_URL = "jdbc:sqlite:" + newDb.toAbsolutePath();
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
            boolean playersTableExists = false;
            boolean deckTableExists = false;

            ResultSet rs = connection.getMetaData().getTables(null, null, "record", null);
            if (rs.next()) {
                recordTableExists = true;
            }

            rs = connection.getMetaData().getTables(null, null, "players", null);
            if (rs.next()) {
                playersTableExists = true;
            }

            rs = connection.getMetaData().getTables(null, null, "deck", null);
            if (rs.next()) {
                deckTableExists = true;
            }

            // 如果所有表都已存在，跳過初始化
            if (recordTableExists && playersTableExists && deckTableExists) {
                System.out.println("[DB] All tables already exist. Skipping initialization.");
                return;
            }

            // 創建必要的表
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
            }

            if (!playersTableExists) {
                statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "username TEXT PRIMARY KEY, password TEXT NOT NULL" +
                        ");");
                System.out.println("[DB] 'players' table created.");
            }

            if (!deckTableExists) {
                statement.execute("CREATE TABLE IF NOT EXISTS deck (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "card_name TEXT NOT NULL, " +
                        "attribute TEXT NOT NULL, " +
                        "rarity TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "description TEXT, " +
                        "base_power INTEGER NOT NULL, " +
                        "FOREIGN KEY(username) REFERENCES players(username)" +
                        ");");
                System.out.println("[DB] 'deck' table created.");
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all game records for a specific username from the database.
     * @param username The username whose records are to be retrieved.
     */
    public void printAllRecords(String username) {
        String querySQL = "SELECT * FROM record WHERE username = ? ORDER BY timestamp DESC;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.printf("ID: %d, Player: %s, Wins: %d, Losses: %d, Timestamp: %s\n",
                            resultSet.getInt("id"),
                            resultSet.getString("player_name"),
                            resultSet.getInt("wins"),
                            resultSet.getInt("losses"),
                            resultSet.getString("timestamp"));
                }
            }
        } catch (SQLException e) {
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
        String queryPlayersSQL = "SELECT * FROM players;";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(queryPlayersSQL)) {
            System.out.println("Players table content:");
            while (rs.next()) {
                System.out.printf("Username: %s, Password: %s\n", rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("Error checking database content: " + e.getMessage());
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
}
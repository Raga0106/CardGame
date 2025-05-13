package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String username;
    private int level;
    private int xp;
    private int currency;
    private List<Card> deck;
    private int xpToNextLevel; // Example: XP needed for next level
    private int rating;

    public Player(String username, int level, int xp, int currency) {
        this(username, level, xp, currency, 1000); // default rating
    }

    // Overloaded constructor to include rating
    public Player(String username, int level, int xp, int currency, int rating) {
        this.username = username;
        this.level = level;
        this.xp = xp;
        this.currency = currency;
        this.rating = rating;
        this.deck = new ArrayList<>();
        this.xpToNextLevel = calculateXpToNextLevel(level);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public int getRating() {
        return rating;
    }

    public void addRating(int delta) {
        this.rating += delta;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getCurrency() {
        return currency;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    // Setters
    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    // Public methods to modify player stats
    public void addXp(int amount) {
        this.xp += amount;
        // Check for level up
        while (this.xp >= this.xpToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        this.xp -= this.xpToNextLevel; // Subtract XP used for level up
        this.level++;
        this.xpToNextLevel = calculateXpToNextLevel(this.level);
        System.out.println(username + " leveled up to level " + this.level + "!");
        // Potentially add rewards for leveling up (e.g., currency, cards)
        addCurrency(50 * this.level); // Example: reward currency based on new level
    }

    public void addCurrency(int amount) {
        this.currency += amount;
    }

    public boolean spendCurrency(int amount) {
        if (this.currency >= amount) {
            this.currency -= amount;
            return true;
        }
        return false;
    }

    public void addCardToDeck(Card card) {
        if (card != null) {
            this.deck.add(card);
        }
    }

    // Example method to calculate XP needed for next level
    private int calculateXpToNextLevel(int currentLevel) {
        return 100 * currentLevel; // Simple example: 100 XP per level
    }
}

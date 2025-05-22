package controller;

import model.Card;
import model.Player; // Add import for Player
import service.GachaService;
import service.BattleService;
import service.BattleService.BattleResult;
import database.GameRecordService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing the game flow, including card drawing, battles, and result tracking.
 */
public class GameController {
    private final GachaService gachaService;
    private final BattleService battleService;
    private final List<Card> playerDeck = new ArrayList<>(); // total collected cards
    private List<Card> playerCards;
    private List<Card> computerCards;
    private int playerScore;
    private int computerScore;
    private Player currentPlayer; // Track current player for stats

    /**
     * Constructor for GameController.
     */
    public GameController() {
        this.gachaService = new GachaService();
        this.battleService = new BattleService();
    }

    /**
     * Starts the game by initializing player and computer cards.
     */
    public void startGame() {
        playerCards = new ArrayList<>();
        computerCards = new ArrayList<>();
        playerScore = 0;
        computerScore = 0;
    }

    /**
     * Draws a single card from gacha and adds to deck.
     * @return the drawn Card
     */
    public Card drawCard() {
        Card card = gachaService.drawCards(1).get(0);
        playerDeck.add(card);
        return card;
    }

    /**
     * Draws multiple cards from the gacha, sets them as the player's hand and draws computer cards.
     * @param count Number of cards to draw.
     * @return List of drawn cards.
     */
    public List<Card> drawMultiple(int count) {
        List<Card> cards = gachaService.drawCards(count);
        // Add to persistent deck
        playerDeck.addAll(cards);
        // Set current hand
        this.playerCards = new ArrayList<>(cards);
        // Reset battle state
        this.computerCards = gachaService.drawCards(count);
        this.playerScore = 0;
        this.computerScore = 0;
        return cards;
    }

    /**
     * Returns the list of all collected cards (deck).
     */
    public List<Card> getPlayerDeck() {
        return playerDeck;
    }

    /**
     * Sets the player's battle hand and draws same number of computer cards.
     * @param selectedCards The list of cards selected for battle.
     */
    public void setBattleCards(List<Card> selectedCards) {
        this.playerCards = new ArrayList<>(selectedCards);
        this.computerCards = gachaService.drawCards(selectedCards.size());
        this.playerScore = 0;
        this.computerScore = 0;
    }

    /**
     * Conducts a single round of battle.
     * @param playerCardIndex The index of the card chosen by the player.
     * @return The result of the battle.
     */
    public BattleResult playRound(int playerCardIndex) {
        if (playerCardIndex < 0 || playerCardIndex >= playerCards.size()) {
            throw new IllegalArgumentException("Invalid card index.");
        }

        Card playerCard = playerCards.remove(playerCardIndex);
        Card computerCard = computerCards.remove(0); // Computer always picks the first card

        BattleResult result = battleService.fight(playerCard, computerCard);        if (result.getWinner() == playerCard) {
            playerScore++;
            addXP(10);      // award XP for win
            addCurrency(5); // award currency for win
        } else if (result.getWinner() == computerCard) {
            computerScore++;
        } else {
            // draw - both winner and loser are null
            addXP(2);
            addCurrency(1);
        }

        return result;
    }

    /**
     * Award experience points to current player.
     */
    public void addXP(int amount) {
        if (currentPlayer != null) {
            currentPlayer.addXp(amount);
        }
    }

    /**
     * Award currency to current player.
     */
    public void addCurrency(int amount) {
        if (currentPlayer != null) {
            currentPlayer.addCurrency(amount);
        }
    }

    /**
     * Determines the final winner of the game.
     * @return "Player" if the player wins, "Computer" if the computer wins, or "Draw" if tied.
     */
    public String determineWinner() {
        if (playerScore > computerScore) {
            return "Player";
        } else if (computerScore > playerScore) {
            return "Computer";
        } else {
            return "Draw";
        }
    }

    public List<Card> getPlayerCards() {
        return playerCards;
    }

    public List<Card> getComputerCards() {
        return computerCards;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getComputerScore() {
        return computerScore;
    }

    /**
     * Loads the player's deck from the database using the provided record service.
     * @param username The username whose deck is to be loaded.
     * @param recordService The service to interact with the database.
     */
    public void loadPlayerDeck(String username, GameRecordService recordService) {
        playerDeck.clear();
        playerDeck.addAll(recordService.loadDeck(username));
    }

    public void addRating(int amount) {
        if (currentPlayer != null) {
            currentPlayer.addRating(amount);
        }
    }

    /**
     * Calculate rating delta: difference between wins and losses.
     * @return positive if player had more wins, negative if more losses.
     */
    public int calculateRatingDelta() {
        return playerScore - computerScore;
    }

    /**
     * Apply rating change after a full match.
     */
    public void applyRatingChange() {
        if (currentPlayer != null) {
            int delta = calculateRatingDelta();
            currentPlayer.addRating(delta);
        }
    }

    /**
     * Get current player's rating.
     */
    public int getPlayerRating() {
        return currentPlayer != null ? currentPlayer.getRating() : 0;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }
}
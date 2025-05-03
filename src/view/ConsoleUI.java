package view;

import controller.GameController;
import model.Card;
import service.BattleService.BattleResult;

import java.util.Scanner;

/**
 * Console-based UI for interacting with the game.
 */
public class ConsoleUI {
    private final GameController gameController;
    private final Scanner scanner;

    /**
     * Constructor for ConsoleUI.
     */
    public ConsoleUI() {
        this.gameController = new GameController();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the game and handles user interaction.
     */
    public void start() {
        System.out.println("Welcome to Card Clash: Elemental Gacha Arena!");
        gameController.startGame();

        System.out.println("Your cards:");
        displayCards(gameController.getPlayerCards());

        for (int round = 1; round <= 5; round++) {
            System.out.printf("\nRound %d: Choose a card to play (1-%d): ", round, gameController.getPlayerCards().size());
            int choice = scanner.nextInt() - 1;

            try {
                BattleResult result = gameController.playRound(choice);
                System.out.println("\nBattle Result:");
                System.out.println(result);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid choice. Please try again.");
                round--; // Retry the round
            }
        }

        System.out.println("\nGame Over!");
        System.out.printf("Final Score - Player: %d, Computer: %d\n", gameController.getPlayerScore(), gameController.getComputerScore());
        System.out.println("Winner: " + gameController.determineWinner());
    }

    private void displayCards(Iterable<Card> cards) {
        int index = 1;
        for (Card card : cards) {
            System.out.printf("%d. %s\n", index++, card);
        }
    }

    public static void main(String[] args) {
        new ConsoleUI().start();
    }
}
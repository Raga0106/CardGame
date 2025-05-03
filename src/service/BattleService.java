package service;

import model.Card;

/**
 * Service for handling battles between two cards.
 */
public class BattleService {

    /**
     * Represents the result of a battle between two cards.
     */
    public static class BattleResult {
        private final Card winner;
        private final Card loser;
        private final int winnerFinalPower;
        private final int loserFinalPower;

        public BattleResult(Card winner, Card loser, int winnerFinalPower, int loserFinalPower) {
            this.winner = winner;
            this.loser = loser;
            this.winnerFinalPower = winnerFinalPower;
            this.loserFinalPower = loserFinalPower;
        }

        public Card getWinner() {
            return winner;
        }

        public Card getLoser() {
            return loser;
        }

        public int getWinnerFinalPower() {
            return winnerFinalPower;
        }

        public int getLoserFinalPower() {
            return loserFinalPower;
        }

        @Override
        public String toString() {
            return String.format("Winner: %s (Power: %d), Loser: %s (Power: %d)",
                    winner.getName(), winnerFinalPower, loser.getName(), loserFinalPower);
        }
    }

    /**
     * Conducts a battle between two cards and determines the winner.
     * @param c1 The first card.
     * @param c2 The second card.
     * @return The result of the battle, including the winner and final powers.
     */
    public BattleResult fight(Card c1, Card c2) {
        int c1FinalPower = c1.getBasePower();
        int c2FinalPower = c2.getBasePower();

        // Apply attribute advantage
        if (c1.getAttribute().isStrongAgainst(c2.getAttribute())) {
            c1FinalPower += 4;
        } else if (c2.getAttribute().isStrongAgainst(c1.getAttribute())) {
            c2FinalPower += 4;
        }

        // Determine winner
        if (c1FinalPower > c2FinalPower) {
            return new BattleResult(c1, c2, c1FinalPower, c2FinalPower);
        } else {
            return new BattleResult(c2, c1, c2FinalPower, c1FinalPower);
        }
    }
}
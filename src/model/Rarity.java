package model;

/**
 * Enum representing the rarity of a card.
 * Each rarity has a specific draw probability and power range.
 */
public enum Rarity {
    SSR(10, 9, 10),
    SR(30, 6, 8),
    R(60, 3, 5);

    private final int probability;
    private final int minPower;
    private final int maxPower;

    /**
     * Constructor for Rarity enum.
     * @param probability The draw probability of the rarity.
     * @param minPower The minimum power for this rarity.
     * @param maxPower The maximum power for this rarity.
     */
    Rarity(int probability, int minPower, int maxPower) {
        this.probability = probability;
        this.minPower = minPower;
        this.maxPower = maxPower;
    }

    public int getProbability() {
        return probability;
    }

    public int getMinPower() {
        return minPower;
    }

    public int getMaxPower() {
        return maxPower;
    }
}
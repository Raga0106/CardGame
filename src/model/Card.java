package model;

import model.CardType;

/**
 * Represents a card in the game with a name, attribute, rarity, and base power.
 */
public class Card {
    private final String name;
    private final Attribute attribute;
    private final Rarity rarity;
    private final int basePower;
    private final CardType type;
    private final String description;

    /**
     * Existing constructor for backward compatibility, defaults type and description.
     * @param name The name of the card.
     * @param attribute The attribute of the card (FIRE, WATER, GRASS).
     * @param rarity The rarity of the card (SSR, SR, R).
     * @param basePower The base power of the card.
     */
    public Card(String name, Attribute attribute, Rarity rarity, int basePower) {
        this(name, attribute, rarity, CardType.BEAST, "", basePower);
    }

    /**
     * New constructor including type and description.
     * @param name The name of the card.
     * @param attribute The attribute of the card (FIRE, WATER, GRASS).
     * @param rarity The rarity of the card (SSR, SR, R).
     * @param type The type of the card.
     * @param description The description of the card.
     * @param basePower The base power of the card.
     */
    public Card(String name, Attribute attribute, Rarity rarity, CardType type, String description, int basePower) {
        this.name = name;
        this.attribute = attribute;
        this.rarity = rarity;
        this.basePower = basePower;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getBasePower() {
        return basePower;
    }

    public CardType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Card{name='%s', attribute=%s, rarity=%s, type=%s, basePower=%d}",
                name, attribute, rarity, type, basePower);
    }
}
package model;

/**
 * Template class for card definitions, holding metadata loaded from game data.
 */
public class CardTemplate {
    private final String name;
    private final Attribute attribute;
    private final Rarity rarity;
    private final CardType type;
    private final String description;
    private final String imagePath; // Path to the card's image

    public CardTemplate(String name, Attribute attribute, Rarity rarity, CardType type, String description, String imagePath) {
        this.name = name;
        this.attribute = attribute;
        this.rarity = rarity;
        this.type = type;
        this.description = description;
        this.imagePath = imagePath;
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

    public CardType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }
}
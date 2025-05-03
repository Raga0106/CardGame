package model;

/**
 * Enum representing the attributes of a card.
 * FIRE is strong against GRASS, GRASS is strong against WATER, and WATER is strong against FIRE.
 */
public enum Attribute {
    FIRE, WATER, GRASS;

    /**
     * Determines if this attribute is strong against another attribute.
     * @param other The other attribute to compare against.
     * @return True if this attribute is strong against the other, false otherwise.
     */
    public boolean isStrongAgainst(Attribute other) {
        return (this == FIRE && other == GRASS) ||
               (this == GRASS && other == WATER) ||
               (this == WATER && other == FIRE);
    }
}
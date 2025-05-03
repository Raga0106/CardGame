package service;

import model.Card;
import model.Attribute;
import model.Rarity;
import model.CardTemplate;
import model.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service for handling the gacha (card drawing) logic.
 */
public class GachaService {
    private final Random random = new Random();

    // Static list of all card templates
    private static final List<CardTemplate> TEMPLATES = new ArrayList<>();
    static {
        // Fire attribute cards
        TEMPLATES.add(new CardTemplate("Blaze Hound", Attribute.FIRE, Rarity.R, CardType.BEAST, "A fast-burning canine, agile but fragile.", "resources/images/blaze_hound.png"));
        TEMPLATES.add(new CardTemplate("Flame Hedgehog", Attribute.FIRE, Rarity.R, CardType.BEAST, "Defensive spiker that retaliates when hit.", "resources/images/flame_hedgehog.png"));
        TEMPLATES.add(new CardTemplate("Ember Archer", Attribute.FIRE, Rarity.SR, CardType.WARRIOR, "Fires burning arrows from long range.", "resources/images/ember_archer.png"));
        TEMPLATES.add(new CardTemplate("Lava Beetle", Attribute.FIRE, Rarity.SR, CardType.NATURE, "Molten body grants high resistance.", "resources/images/lava_beetle.png"));
        TEMPLATES.add(new CardTemplate("Flame Dancer", Attribute.FIRE, Rarity.SR, CardType.MAGE, "Twirls through the battlefield, evasive.", "resources/images/flame_dancer.png"));
        TEMPLATES.add(new CardTemplate("Inferno Dragon", Attribute.FIRE, Rarity.SSR, CardType.BEAST, "Dominant fire-breather, area burn skill.", "resources/images/inferno_dragon.png"));
        TEMPLATES.add(new CardTemplate("Hellfire Knight", Attribute.FIRE, Rarity.SSR, CardType.WARRIOR, "Rides a fire beast, blends strength & magic.", "resources/images/hellfire_knight.png"));
        TEMPLATES.add(new CardTemplate("Solar Fox", Attribute.FIRE, Rarity.SR, CardType.BEAST, "Quick-strike card with bonus crit chance.", "resources/images/solar_fox.png"));
        TEMPLATES.add(new CardTemplate("Magma Golem", Attribute.FIRE, Rarity.R, CardType.GOLEM, "Slow but incredibly hard to destroy.", "resources/images/magma_golem.png"));
        TEMPLATES.add(new CardTemplate("Ash Phoenix", Attribute.FIRE, Rarity.SSR, CardType.ELEMENTAL, "Mythical rebirth card, powerful late-game.", "resources/images/ash_phoenix.png"));
        // Grass attribute cards
        TEMPLATES.add(new CardTemplate("Mossback Turtle", Attribute.GRASS, Rarity.R, CardType.BEAST, "Tanky turtle with regeneration abilities.", "resources/images/mossback_turtle.png"));
        TEMPLATES.add(new CardTemplate("Leaf Pixie", Attribute.GRASS, Rarity.R, CardType.MAGE, "Disruptive support unit, specializes in CC.", "resources/images/leaf_pixie.png"));
        TEMPLATES.add(new CardTemplate("Vine Hunter", Attribute.GRASS, Rarity.SR, CardType.WARRIOR, "Archer who tracks with entangling vines.", "resources/images/vine_hunter.png"));
        TEMPLATES.add(new CardTemplate("Boomshroom", Attribute.GRASS, Rarity.SR, CardType.NATURE, "Explodes on attack, high-risk card.", "resources/images/boomshroom.png"));
        TEMPLATES.add(new CardTemplate("Thorn Witch", Attribute.GRASS, Rarity.SR, CardType.MAGE, "Specializes in poison and control.", "resources/images/thorn_witch.png"));
        TEMPLATES.add(new CardTemplate("Shadow Leopard", Attribute.GRASS, Rarity.SSR, CardType.BEAST, "Stealthy predator, double strike ability.", "resources/images/shadow_leopard.png"));
        TEMPLATES.add(new CardTemplate("Glimmerhorn King", Attribute.GRASS, Rarity.SSR, CardType.BEAST, "King of the field, inspires other cards.", "resources/images/glimmerhorn_king.png"));
        TEMPLATES.add(new CardTemplate("Spirit of Forest", Attribute.GRASS, Rarity.SSR, CardType.ELEMENTAL, "Legendary support card, heals over time.", "resources/images/spirit_of_forest.png"));
        TEMPLATES.add(new CardTemplate("Petal Guardian", Attribute.GRASS, Rarity.R, CardType.WARRIOR, "Defensive shield unit, ideal for stalling.", "resources/images/petal_guardian.png"));
        TEMPLATES.add(new CardTemplate("Prairie Windwolf", Attribute.GRASS, Rarity.SR, CardType.BEAST, "Breaks through defense with speed.", "resources/images/prairie_windwolf.png"));
        // Water attribute cards
        TEMPLATES.add(new CardTemplate("Bubble Tardigrade", Attribute.WATER, Rarity.R, CardType.BEAST, "Cute yet resilient, restores minor HP.", "resources/images/bubble_tardigrade.png"));
        TEMPLATES.add(new CardTemplate("Tide Ninja", Attribute.WATER, Rarity.R, CardType.WARRIOR, "High dodge rate, fast assassin.", "resources/images/tide_ninja.png"));
        TEMPLATES.add(new CardTemplate("Ice-scaled Murloc", Attribute.WATER, Rarity.SR, CardType.BEAST, "Blocks incoming attacks, counter-ready.", "resources/images/ice_scaled_murloc.png"));
        TEMPLATES.add(new CardTemplate("Aqua Sorcerer", Attribute.WATER, Rarity.SR, CardType.MAGE, "Area caster, slows enemy cards.", "resources/images/aqua_sorcerer.png"));
        TEMPLATES.add(new CardTemplate("Abyssal Tentacle", Attribute.WATER, Rarity.SR, CardType.NATURE, "Disrupts and binds opponents in place.", "resources/images/abyssal_tentacle.png"));
        TEMPLATES.add(new CardTemplate("Frost Giant", Attribute.WATER, Rarity.SSR, CardType.ELEMENTAL, "Slows enemies and freezes the battlefield.", "resources/images/frost_giant.png"));
        TEMPLATES.add(new CardTemplate("Sea King Knight", Attribute.WATER, Rarity.SSR, CardType.WARRIOR, "Leads aquatic troops, aggressive leader.", "resources/images/sea_king_knight.png"));
        TEMPLATES.add(new CardTemplate("Snowfang Lynx", Attribute.WATER, Rarity.SR, CardType.BEAST, "Fast striker with high crit potential.", "resources/images/snowfang_lynx.png"));
        TEMPLATES.add(new CardTemplate("Mystic Codex", Attribute.WATER, Rarity.R, CardType.MAGE, "Autonomous water spellcaster.", "resources/images/mystic_codex.png"));
        TEMPLATES.add(new CardTemplate("Tidal Leviathan", Attribute.WATER, Rarity.SSR, CardType.BEAST, "Devastating waterquake attack, hard to beat.", "resources/images/tidal_leviathan.png"));
    }

    /**
     * Draws a specified number of cards randomly based on rarity probabilities.
     * @param count The number of cards to draw.
     * @return A list of randomly generated cards.
     */
    public List<Card> drawCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Rarity rarity = getRandomRarity();
            Attribute attribute = getRandomAttribute();
            List<CardTemplate> pool = TEMPLATES.stream()
                    .filter(t -> t.getAttribute() == attribute && t.getRarity() == rarity)
                    .collect(Collectors.toList());
            CardTemplate template = pool.get(random.nextInt(pool.size()));
            int basePower = getRandomPower(rarity);
            Card card = new Card(template.getName(), attribute, rarity, template.getType(), template.getDescription(), basePower);
            cards.add(card);

            // Print card details immediately after drawing
            System.out.printf("[Gacha] Drawn Card: Name=%s, Attribute=%s, Rarity=%s, Type=%s, Power=%d\n",
                    card.getName(), card.getAttribute(), card.getRarity(), card.getType(), card.getBasePower());
        }
        return cards;
    }

    private Rarity getRandomRarity() {
        int roll = random.nextInt(100) + 1;
        if (roll <= Rarity.SSR.getProbability()) {
            return Rarity.SSR;
        } else if (roll <= Rarity.SSR.getProbability() + Rarity.SR.getProbability()) {
            return Rarity.SR;
        } else {
            return Rarity.R;
        }
    }

    private Attribute getRandomAttribute() {
        Attribute[] attributes = Attribute.values();
        return attributes[random.nextInt(attributes.length)];
    }

    private int getRandomPower(Rarity rarity) {
        return random.nextInt(rarity.getMaxPower() - rarity.getMinPower() + 1) + rarity.getMinPower();
    }
}
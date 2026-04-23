package sts.advisor;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import sts.advisor.SynergyEngine.CharData;

import java.util.*;

/**
 * Advises on shop purchases — cards, relics, and card removal.
 */
public class ShopAdvisor {

    // Cached relic list from PostCreateShopRelicSubscriber
    private static List<StoreRelic> cachedRelics = new ArrayList<>();

    public static void cacheRelics(ArrayList<StoreRelic> relics) {
        cachedRelics = new ArrayList<>(relics);
    }

    public static void clearCache() {
        cachedRelics.clear();
    }

    // ── Relic scoring ─────────────────────────────────────────────────────────

    public static RelicAdvice scoreRelic(StoreRelic storeRelic) {
        AbstractRelic relic = storeRelic.relic;
        int price           = storeRelic.price;
        int gold            = AbstractDungeon.player.gold;

        // Get archetype for context-aware scoring
        CharData data = SynergyEngine.getCharData();
        String archetype = "";
        if (data != null) {
            Map<String, Integer> deckTags = SynergyEngine.buildDeckTags(data.cardTags);
            archetype = data.archetype(deckTags);
        }

        int baseScore = getRelicBaseScore(relic.relicId, archetype);
        if (baseScore == 45) logUnknownRelic(relic.relicId, relic.name);

        boolean canAfford = gold >= price;
        boolean goodValue = price <= 150 && baseScore >= 70;
        boolean poorValue = price >= 250 && baseScore < 60;

        String rec;
        if (!canAfford)           rec = "Can't afford";
        else if (baseScore >= 70) rec = "Buy";
        else if (baseScore >= 50) rec = "Consider";
        else                      rec = "Skip";

        List<String> reasons = new ArrayList<>();
        if (poorValue)  reasons.add("Expensive for the value");
        if (goodValue)  reasons.add("Good price");
        if (!canAfford) reasons.add("Need " + (price - gold) + "g more");

        return new RelicAdvice(relic, baseScore, price, canAfford, rec, reasons);
    }

    private static final Set<String> loggedRelics = new java.util.HashSet<>();
    private static void logUnknownRelic(String relicId, String name) {
        if (loggedRelics.contains(relicId)) return;
        loggedRelics.add(relicId);
        try (java.io.FileWriter fw = new java.io.FileWriter(
                System.getenv("HOME") + "/.steam/steam/steamapps/common/SlayTheSpire/sts_advisor_unknown_relics.log", true)) {
            fw.write(relicId + " -> " + name + "\n");
        } catch (Exception ignored) {}
    }

    private static int getRelicBaseScore(String relicId, String archetype) {
        // Universal high-value relics — IDs use spaces, matching AbstractRelic.relicId
        switch (relicId) {
            case "Snecko Eye":          return 92;
            case "Toolbox":             return 85;
            case "Orange Pellets":      return 80;
            case "Mango":               return 78;
            case "Paper Frog":          return 75;
            case "Paper Krane":         return 75;
            case "Gremlin Horn":        return 72;
            case "Pocketwatch":         return 65;
            case "Sundial":             return 68;
            case "Mummified Hand":      return 68;
            case "Sling of Courage":    return 62;
            case "Meat on the Bone":    return 60;
            case "Medical Kit":         return 55;
            case "Lantern":             return 48;
            case "Boot":                return 42;
            case "Ectoplasm":           return 35;
            case "Cursed Key":          return 32;
            case "Busted Crown":        return 20;
            case "Coffee Dripper":      return 22;
            case "Sozu":                return 18;
            case "Philosopher's Stone": return 70;
            case "Velvet Choker":       return 68;
            case "Nuclear Battery":     return 80;
            case "Cauldron":            return 55;
            case "Bottled Flame":       return 58;
            case "Bottled Lightning":   return 55;
            case "Bottled Tornado":     return 55;
            case "Calipers":            return 62;
            case "Self Forming Clay":   return 60;
            case "Oddly Smooth Stone":  return 60;
            case "Dead Branch":         return 80;
            case "Runic Cube":          return 82;
            case "Du-Vu Doll":          return 65;
            case "Juzu Bracelet":       return 55;
            case "Kunai":               return 65;
            case "Shuriken":            return 63;
            case "Letter Opener":       return 65;
            case "Ninja Scroll":        return 70;
            case "Pen Nib":             return 62;
            case "Preserved Insect":    return 68;
            case "Frozen Eye":          return 65;
            case "Inserter":            return 70;
            case "Frozen Core":         return 72;
            case "Data Disk":           return 70;
            case "Symbiotic Virus":     return 68;
            case "Runic Capacitor":     return 85;
            case "Twisted Funnel":      return 82;
            case "Tingsha":             return 72;
            case "Toy Ornithopter":     return 58;
            case "Brimstone":           return 78;
            case "Red Skull":           return 70;
            case "Orichalcum":          return 65;
            case "Anchor":              return 62;
            case "Horn Cleat":          return 60;
            case "Thread and Needle":   return 58;
            case "Torii":               return 65;
            case "Turnip":              return 55;
            case "Bag of Marbles":      return 58;
            case "Bag of Preparation":  return 72;
            case "Blood Vial":          return 48;
            case "Bronze Scales":       return 45;
            case "Centennial Puzzle":   return 62;
            case "Ceramic Fish":        return 55;
            case "Dream Catcher":       return 65;
            case "Happy Flower":        return 58;
            case "Tiny Chest":          return 60;
            case "Omamori":             return 62;
            case "Regal Pillow":        return 48;
            case "Smiling Mask":        return 60;
            case "Strawberry":          return 52;
            case "The Boot":            return 42;
            case "Tiny House":          return 75;
            case "Vajra":               return 62;
            case "War Paint":           return 65;
            case "Whetstone":           return 65;
            case "Winged Boots":        return 70;
            case "Lizard Tail":         return 68;
            case "Old Coin":            return 65;
            case "Chemical X":          return 72;
            case "Cloak Clasp":         return 62;
            case "Golden Eye":          return 60;
            case "Pantograph":          return 65;
            case "Matryoshka":          return 65;
            case "Mercury Hourglass":   return 55;
            case "Molten Egg 2":        return 65;
            case "Frozen Egg 2":        return 65;
            case "Toxic Egg 2":         return 65;
            case "White Beast Statue":  return 62;
            case "Prayer Wheel":        return 65;
            case "Shovel":              return 68;
            case "Stone Calendar":      return 55;
            case "Strange Spoon":       return 60;
            case "The Courier":         return 68;
            case "Orrery":              return 70;
        }

        // Archetype-specific relics
        switch (archetype) {
            case "Exhaust Engine":
                if (relicId.equals("Dead Branch"))      return 95;
                if (relicId.equals("Runic Cube"))       return 88;
                break;
            case "Strength Scaling":
                if (relicId.equals("Brimstone"))        return 88;
                if (relicId.equals("Paper Frog"))       return 82;
                break;
            case "Poison":
                if (relicId.equals("Twisted Funnel"))   return 90;
                if (relicId.equals("Tingsha"))          return 85;
                break;
            case "Shiv Engine":
                if (relicId.equals("Ninja Scroll"))     return 88;
                if (relicId.equals("Letter Opener"))    return 82;
                break;
            case "Frost Fortress":
                if (relicId.equals("Frozen Core"))      return 88;
                if (relicId.equals("Symbiotic Virus"))  return 78;
                break;
            case "Nova/Plasma":
                if (relicId.equals("Nuclear Battery"))  return 92;
                if (relicId.equals("Runic Capacitor"))  return 88;
                break;
            case "Stance Cycling":
            case "Mantra/Divinity":
                if (relicId.equals("Ninja Scroll"))     return 80;
                if (relicId.equals("Pocketwatch"))      return 72;
                break;
            case "Block Fortress":
            case "Calm Block":
            case "Block/Dex":
                if (relicId.equals("Orichalcum"))       return 78;
                if (relicId.equals("Calipers"))         return 72;
                break;
        }

        return 45; // unknown
    }

    // ── Card removal advice ───────────────────────────────────────────────────

    public static RemovalAdvice scoreRemoval() {
        if (AbstractDungeon.player == null) return new RemovalAdvice(false, "", 0);

        ShopScreen shop = AbstractDungeon.shopScreen;
        if (shop == null || !shop.purgeAvailable) return new RemovalAdvice(false, "", 0);

        int cost = ShopScreen.purgeCost;
        int gold = AbstractDungeon.player.gold;
        int deckSize = AbstractDungeon.player.masterDeck.group.size();

        // Find the worst card in the deck — best candidate for removal
        AbstractCard worst = findWorstCard();
        if (worst == null) return new RemovalAdvice(false, "Deck looks clean", cost);

        // Should we recommend removal?
        boolean shouldRemove = false;
        String reason = "";

        // Always remove curses
        if (worst.type == AbstractCard.CardType.CURSE) {
            shouldRemove = gold >= cost;
            reason = "Remove " + worst.name + " (curse)";
        }
        // Remove status cards
        else if (worst.type == AbstractCard.CardType.STATUS) {
            shouldRemove = gold >= cost;
            reason = "Remove " + worst.name + " (status)";
        }
        // Remove excess strikes/defends if deck is large enough
        else if (isStarterCard(worst) && deckSize >= 12) {
            shouldRemove = gold >= cost;
            reason = "Remove " + worst.name + " (starter)";
        }
        // Remove weak cards from a developed deck
        else if (deckSize >= 18) {
            AdvisorResult score = CardScorer.score(worst);
            if (score.totalScore < 35) {
                shouldRemove = gold >= cost;
                reason = "Remove " + worst.name + " (D tier, thins deck)";
            } else if (score.totalScore < 50 && gold >= cost) {
                // Suggest but don't strongly recommend
                reason = "Consider removing " + worst.name;
            }
        }

        if (!shouldRemove && reason.isEmpty()) {
            reason = deckSize < 15 ? "Deck too small to remove" : "No obvious removal target";
        }

        return new RemovalAdvice(shouldRemove && gold >= cost, reason, cost);
    }

    private static AbstractCard findWorstCard() {
        AbstractCard worst = null;
        int worstScore = Integer.MAX_VALUE;

        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            // Curses and statuses first
            if (card.type == AbstractCard.CardType.CURSE
                    || card.type == AbstractCard.CardType.STATUS) {
                return card;
            }

            AdvisorResult result = CardScorer.score(card);
            if (result.totalScore < worstScore) {
                worstScore = result.totalScore;
                worst = card;
            }
        }
        return worst;
    }

    private static boolean isStarterCard(AbstractCard card) {
        String id = card.cardID;
        return id.equals("Strike_R") || id.equals("Strike_G")
            || id.equals("Strike_B") || id.equals("Strike_P")
            || id.equals("Defend_R") || id.equals("Defend_G")
            || id.equals("Defend_B") || id.equals("Defend_P");
    }

    public static List<StoreRelic> getCachedRelics() {
        return cachedRelics;
    }

    // ── Data classes ──────────────────────────────────────────────────────────

    public static class RelicAdvice {
        public final AbstractRelic relic;
        public final int score;
        public final int price;
        public final boolean canAfford;
        public final String recommendation;
        public final List<String> reasons;

        public RelicAdvice(AbstractRelic relic, int score, int price,
                           boolean canAfford, String recommendation, List<String> reasons) {
            this.relic          = relic;
            this.score          = score;
            this.price          = price;
            this.canAfford      = canAfford;
            this.recommendation = recommendation;
            this.reasons        = reasons;
        }

        public String tier() {
            if (score >= 85) return "S";
            if (score >= 70) return "A";
            if (score >= 55) return "B";
            if (score >= 35) return "C";
            return "D";
        }
    }

    public static class RemovalAdvice {
        public final boolean shouldRemove;
        public final String reason;
        public final int cost;

        public RemovalAdvice(boolean shouldRemove, String reason, int cost) {
            this.shouldRemove = shouldRemove;
            this.reason       = reason;
            this.cost         = cost;
        }
    }
}

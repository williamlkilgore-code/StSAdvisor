package sts.advisor;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import sts.advisor.data.*;

import java.util.*;

/**
 * Scans the player's current deck and computes synergy bonuses for candidate cards.
 */
public class SynergyEngine {

    /** Detect the current character and return a CharData wrapper */
    public static CharData getCharData() {
        if (AbstractDungeon.player == null) return null;
        String cls = AbstractDungeon.player.getClass().getSimpleName();
        switch (cls) {
            case "Ironclad":   return new CharData(IroncladData.SCORES, IroncladData.UPGRADE_BONUS,
                                                   IroncladData.CARD_TAGS, IroncladData.SYNERGY_GROUPS) {
                @Override public String archetype(Map<String,Integer> t) { return IroncladData.detectArchetype(t); }
            };
            case "TheSilent":  return new CharData(SilentData.SCORES, SilentData.UPGRADE_BONUS,
                                                   SilentData.CARD_TAGS, SilentData.SYNERGY_GROUPS) {
                @Override public String archetype(Map<String,Integer> t) { return SilentData.detectArchetype(t); }
            };
            case "Defect":     return new CharData(DefectData.SCORES, DefectData.UPGRADE_BONUS,
                                                   DefectData.CARD_TAGS, DefectData.SYNERGY_GROUPS) {
                @Override public String archetype(Map<String,Integer> t) { return DefectData.detectArchetype(t); }
            };
            default:           return new CharData(WatcherData.SCORES, WatcherData.UPGRADE_BONUS,
                                                   WatcherData.CARD_TAGS, WatcherData.SYNERGY_GROUPS) {
                @Override public String archetype(Map<String,Integer> t) { return WatcherData.detectArchetype(t); }
            };
        }
    }

    /** Build a tag frequency map from the player's current deck */
    public static Map<String, Integer> buildDeckTags(Map<String, List<String>> cardTags) {
        Map<String, Integer> counts = new HashMap<>();
        if (AbstractDungeon.player == null) return counts;

        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            List<String> tags = cardTags.get(card.cardID);
            if (tags == null) continue;
            for (String tag : tags) {
                counts.put(tag, counts.getOrDefault(tag, 0) + 1);
            }
        }
        return counts;
    }

    /**
     * Calculate the synergy bonus and reasons for a candidate card
     * given the current deck tag counts.
     */
    public static SynergyResult calcSynergy(
            String cardName,
            Map<String, List<String>> cardTags,
            Map<String, List<String>> synergyGroups,
            Map<String, Integer> deckTags) {

        List<String> candidateTags = cardTags.getOrDefault(cardName, Collections.emptyList());
        int bonus = 0;
        List<String> reasons = new ArrayList<>();

        // Tag-level synergy: card shares tags with cards already in deck
        for (String tag : candidateTags) {
            int count = deckTags.getOrDefault(tag, 0);
            if (count >= 2) {
                bonus += 10;
                reasons.add("Strong " + humanTag(tag) + " synergy (" + count + " cards)");
            } else if (count == 1) {
                bonus += 5;
                reasons.add("Builds " + humanTag(tag) + " package");
            }
        }

        // Group-level synergy: card fits an emerging archetype
        for (Map.Entry<String, List<String>> entry : synergyGroups.entrySet()) {
            String groupName = entry.getKey();
            List<String> groupTags = entry.getValue();
            boolean cardFitsGroup = false;
            for (String t : candidateTags) {
                if (groupTags.contains(t)) { cardFitsGroup = true; break; }
            }
            if (!cardFitsGroup) continue;
            int deckSupport = 0;
            for (String t : groupTags) {
                if (deckTags.getOrDefault(t, 0) > 0) deckSupport++;
            }
            if (deckSupport >= 2) {
                bonus += 8;
                reasons.add("Fits " + groupName + " archetype");
            }
        }

        // Duplicate penalty: already have 2+ copies of this card
        int copies = countCopies(cardName);
        if (copies >= 2) {
            bonus -= 15;
            reasons.add("Already have " + copies + " copies");
        } else if (copies == 1) {
            bonus -= 5;
            reasons.add("Already have 1 copy");
        }

        // Missing role bonuses
        if (candidateTags.contains("aoe") && deckTags.getOrDefault("aoe", 0) == 0) {
            bonus += 10;
            reasons.add("Fills missing AoE role");
        }
        if (candidateTags.contains("draw") && deckTags.getOrDefault("draw", 0) == 0) {
            bonus += 8;
            reasons.add("Fills missing draw role");
        }
        if (candidateTags.contains("block") && deckTags.getOrDefault("block", 0) == 0) {
            bonus += 8;
            reasons.add("Fills missing block role");
        }

        return new SynergyResult(Math.max(-20, Math.min(bonus, 25)), reasons);
    }

    private static int countCopies(String cardName) {
        if (AbstractDungeon.player == null) return 0;
        int count = 0;
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.cardID.equals(cardName)) count++;
        }
        return count;
    }

    private static String humanTag(String tag) {
        return tag.replace("_", " ");
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    public static abstract class CharData {
        public final Map<String, Integer> scores;
        public final Map<String, Integer> upgradeBonuses;
        public final Map<String, List<String>> cardTags;
        public final Map<String, List<String>> synergyGroups;

        public CharData(Map<String, Integer> scores,
                        Map<String, Integer> upgradeBonuses,
                        Map<String, List<String>> cardTags,
                        Map<String, List<String>> synergyGroups) {
            this.scores = scores;
            this.upgradeBonuses = upgradeBonuses;
            this.cardTags = cardTags;
            this.synergyGroups = synergyGroups;
        }

        public abstract String archetype(Map<String, Integer> deckTags);
    }

    public static class SynergyResult {
        public final int bonus;
        public final List<String> reasons;
        public SynergyResult(int bonus, List<String> reasons) {
            this.bonus = bonus;
            this.reasons = reasons;
        }
    }
}

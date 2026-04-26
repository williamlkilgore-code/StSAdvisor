package sts.advisor;

import com.megacrit.cardcrawl.cards.AbstractCard;
import sts.advisor.SynergyEngine.CharData;
import sts.advisor.SynergyEngine.SynergyResult;

import java.io.*;
import java.util.*;

public class CardScorer {

    private static final Map<String, Integer> COLORLESS_SCORES = new HashMap<>();
    private static final Set<String> loggedUnknown = new HashSet<>();
    private static final String LOG_PATH =
        System.getenv("HOME") + "/.steam/steam/steamapps/common/SlayTheSpire/sts_advisor_unknown_cards.log";

    static {
        COLORLESS_SCORES.put("Apparition", 92);
        COLORLESS_SCORES.put("Metamorphosis", 85);
        COLORLESS_SCORES.put("Violence", 80);
        COLORLESS_SCORES.put("Transmutation", 72);
        COLORLESS_SCORES.put("Apotheosis", 88);
        COLORLESS_SCORES.put("Chrysalis", 82);
        COLORLESS_SCORES.put("Hand of Greed", 70);
        COLORLESS_SCORES.put("Enlightenment", 68);
        COLORLESS_SCORES.put("Forethought", 65);
        COLORLESS_SCORES.put("Magnetism", 62);
        COLORLESS_SCORES.put("Discovery", 65);
        COLORLESS_SCORES.put("Madness", 60);
        COLORLESS_SCORES.put("Panacea", 58);
        COLORLESS_SCORES.put("Finesse", 55);
        COLORLESS_SCORES.put("Good Instincts", 52);
        COLORLESS_SCORES.put("Impatience", 48);
        COLORLESS_SCORES.put("Panic Button", 55);
        COLORLESS_SCORES.put("Purity", 58);
        COLORLESS_SCORES.put("Deep Breath", 45);
        COLORLESS_SCORES.put("Dramatic Entrance", 50);
        COLORLESS_SCORES.put("Flash of Steel", 40);
        COLORLESS_SCORES.put("Swift Strike", 35);
        COLORLESS_SCORES.put("Trip", 38);
        COLORLESS_SCORES.put("Blind", 42);
        COLORLESS_SCORES.put("Bandage Up", 45);
        COLORLESS_SCORES.put("Dark Shackles", 50);
        COLORLESS_SCORES.put("Mind Blast", 45);
        COLORLESS_SCORES.put("Jack Of All Trades", 48);
        COLORLESS_SCORES.put("Ritual Dagger", 55);
        COLORLESS_SCORES.put("Bite", 42);
        COLORLESS_SCORES.put("Safety", 48);
        COLORLESS_SCORES.put("Mayhem", 62);
        COLORLESS_SCORES.put("Panache", 58);
        COLORLESS_SCORES.put("Master of Strategy", 60);
        COLORLESS_SCORES.put("Secret Technique", 62);
        COLORLESS_SCORES.put("Secret Weapon", 62);
        COLORLESS_SCORES.put("Thinking Ahead", 52);
        COLORLESS_SCORES.put("Sadistic Nature", 55);
        COLORLESS_SCORES.put("Just Lucky", 45);
        COLORLESS_SCORES.put("The Bomb", 55);
        COLORLESS_SCORES.put("Conjure Blade", 65);
        COLORLESS_SCORES.put("Flying Sleeves", 48);
        COLORLESS_SCORES.put("Shiv", 30);
        COLORLESS_SCORES.put("Smite", 30);
        COLORLESS_SCORES.put("Miracle", 35);
        COLORLESS_SCORES.put("Insight", 38);
        COLORLESS_SCORES.put("Void", 5);
        COLORLESS_SCORES.put("Clumsy", 5);
        COLORLESS_SCORES.put("Normality", 5);
        COLORLESS_SCORES.put("Pain", 8);
        COLORLESS_SCORES.put("Doubt", 8);
        COLORLESS_SCORES.put("Shame", 8);
        COLORLESS_SCORES.put("Pride", 10);
        COLORLESS_SCORES.put("Regret", 8);
        COLORLESS_SCORES.put("Parasite", 10);
        COLORLESS_SCORES.put("Writhe", 8);
        COLORLESS_SCORES.put("Injury", 8);
        COLORLESS_SCORES.put("Decay", 8);
        COLORLESS_SCORES.put("Burn", 8);
        COLORLESS_SCORES.put("Dazed", 8);
        COLORLESS_SCORES.put("Slimed", 8);
        COLORLESS_SCORES.put("Wound", 8);
        COLORLESS_SCORES.put("Curse of the Bell", 5);
        COLORLESS_SCORES.put("Necronomicurse", 5);
    }

    public static AdvisorResult score(AbstractCard card) {
        CharData data = SynergyEngine.getCharData();

        // Check colorless cards first
        if (COLORLESS_SCORES.containsKey(card.cardID)) {
            int base = COLORLESS_SCORES.get(card.cardID);
            if (card.upgraded) base = Math.min(base + 5, 100);
            return new AdvisorResult(card.cardID, base, 0, Collections.emptyList());
        }

        if (data == null) {
            return new AdvisorResult(card.cardID, 40, 0, Collections.emptyList());
        }

        int base = data.scores.getOrDefault(card.cardID, -1);

        if (base == -1) {
            base = 40;
            if (!loggedUnknown.contains(card.cardID)) {
                loggedUnknown.add(card.cardID);
                logUnknown(card.cardID, card.name);
            }
        }

        if (card.upgraded) {
            base += data.upgradeBonuses.getOrDefault(card.cardID, 3);
        }
        base = Math.min(base, 100);

        Map<String, Integer> deckTags = SynergyEngine.buildDeckTags(data.cardTags);
        SynergyResult syn = SynergyEngine.calcSynergy(
            card.cardID, data.cardTags, data.synergyGroups, deckTags
        );

        return new AdvisorResult(card.cardID, base, syn.bonus, syn.reasons);
    }

    public static List<AdvisorResult> scoreAll(List<AbstractCard> cards) {
        List<AdvisorResult> results = new ArrayList<>();
        for (AbstractCard card : cards) {
            results.add(score(card));
        }
        return results;
    }

    private static void logUnknown(String cardId, String cardName) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write(cardId + " -> " + cardName + "\n");
        } catch (IOException e) {
            // ignore
        }
    }
}

package sts.advisor;

import com.megacrit.cardcrawl.cards.AbstractCard;
import sts.advisor.SynergyEngine.CharData;
import sts.advisor.SynergyEngine.SynergyResult;

import java.io.*;
import java.util.*;

public class CardScorer {

    // Log unknown card IDs so we can add them to data files
    private static final Set<String> loggedUnknown = new HashSet<>();
    private static final String LOG_PATH =
        System.getenv("HOME") + "/.steam/steam/steamapps/common/SlayTheSpire/sts_advisor_unknown_cards.log";

    public static AdvisorResult score(AbstractCard card) {
        CharData data = SynergyEngine.getCharData();
        if (data == null) {
            return new AdvisorResult(card.cardID, 40, 0, Collections.emptyList());
        }

        int base = data.scores.getOrDefault(card.cardID, -1);

        // Log unknown card IDs (score -1 means not found)
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

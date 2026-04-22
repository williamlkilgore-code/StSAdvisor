package sts.advisor;

import java.util.List;
import java.util.ArrayList;

/**
 * Holds the full scoring result for a single card recommendation.
 */
public class AdvisorResult {
    public final String cardName;
    public final int baseScore;
    public final int synergyBonus;
    public final int totalScore;
    public final String tier;
    public final String recommendation;  // "TAKE", "CONSIDER", "SKIP"
    public final List<String> reasons;   // synergy reasons, truncated for display

    public AdvisorResult(String cardName, int baseScore, int synergyBonus,
                         List<String> reasons) {
        this.cardName    = cardName;
        this.baseScore   = baseScore;
        this.synergyBonus = synergyBonus;
        this.totalScore  = Math.min(baseScore + synergyBonus, 100);
        this.tier        = calcTier(this.totalScore);
        this.recommendation = calcRec(this.totalScore);
        this.reasons     = reasons != null ? reasons : new ArrayList<>();
    }

    private static String calcTier(int score) {
        if (score >= 85) return "S";
        if (score >= 70) return "A";
        if (score >= 55) return "B";
        if (score >= 35) return "C";
        return "D";
    }

    private static String calcRec(int score) {
        if (score >= 70) return "TAKE";
        if (score >= 50) return "CONSIDER";
        return "SKIP";
    }

    /** True if all cards in a reward should be skipped */
    public static boolean allSkip(List<AdvisorResult> results) {
        if (results == null || results.isEmpty()) return false;
        for (AdvisorResult r : results) {
            if (!r.recommendation.equals("SKIP")) return false;
        }
        return true;
    }
}

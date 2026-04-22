package sts.advisor.data;

import java.util.*;

public class SilentData {

    public static final Map<String, Integer> SCORES = new HashMap<>();
    public static final Map<String, Integer> UPGRADE_BONUS = new HashMap<>();
    public static final Map<String, List<String>> CARD_TAGS = new HashMap<>();
    public static final Map<String, List<String>> SYNERGY_GROUPS = new LinkedHashMap<>();

    static {
        SCORES.put("Nightmare", 95);
        SCORES.put("WraithForm", 92);
        SCORES.put("Envenom", 90);
        SCORES.put("NoxiousFumes", 89);
        SCORES.put("Malaise", 88);
        SCORES.put("InfiniteBlades", 87);
        SCORES.put("GlassKnife", 85);
        SCORES.put("AThousandCuts", 84);
        SCORES.put("Catalyst", 83);
        SCORES.put("DieDieDie", 81);
        SCORES.put("AfterImage", 82);
        SCORES.put("Footwork", 80);
        SCORES.put("DaggerSpray", 79);
        SCORES.put("StormOfSteel", 78);
        SCORES.put("WellLaidPlans", 77);
        SCORES.put("Burst", 75);
        SCORES.put("Expertise", 74);
        SCORES.put("Backflip", 76);
        SCORES.put("CorpseExplosion", 71);
        SCORES.put("Predator", 70);
        SCORES.put("Eviscerate", 68);
        SCORES.put("Flechettes", 66);
        SCORES.put("BladeDance", 65);
        SCORES.put("Caltrops", 64);
        SCORES.put("BouncingFlask", 63);
        SCORES.put("CripplingCloud", 62);
        SCORES.put("Setup", 61);
        SCORES.put("Choke", 59);
        SCORES.put("RiddleWithHoles", 58);
        SCORES.put("Distraction", 57);
        SCORES.put("Outmaneuver", 56);
        SCORES.put("EscapePlan", 55);
        SCORES.put("Blur", 54);
        SCORES.put("DodgeAndRoll", 52);
        SCORES.put("Acrobatics", 50);
        SCORES.put("SneakyStrike", 48);
        SCORES.put("Terror", 46);
        SCORES.put("LegSweep", 44);
        SCORES.put("QuickSlash", 45);
        SCORES.put("FlyingKnee", 36);
        SCORES.put("Bane", 25);
        SCORES.put("Backstab", 22);
        SCORES.put("Strike_G", 10);
        SCORES.put("Defend_G", 10);
        SCORES.put("Neutralize", 18);
        SCORES.put("Survivor", 18);

        UPGRADE_BONUS.put("Catalyst", 15);
        UPGRADE_BONUS.put("Nightmare", 8);
        UPGRADE_BONUS.put("NoxiousFumes", 6);
        UPGRADE_BONUS.put("WellLaidPlans", 8);
        UPGRADE_BONUS.put("Flechettes", 10);
        UPGRADE_BONUS.put("Burst", 8);
        UPGRADE_BONUS.put("Backflip", 6);
        UPGRADE_BONUS.put("AfterImage", 5);

        tag("NoxiousFumes",   "poison_source", "power");
        tag("Envenom",        "poison_source", "power");
        tag("Catalyst",       "poison_payoff");
        tag("BouncingFlask",  "poison_source");
        tag("CripplingCloud", "poison_source");
        tag("CorpseExplosion","poison_payoff", "aoe");
        tag("Bane",           "poison_payoff", "attack");
        tag("BladeDance",     "shiv_gen");
        tag("CloakAndDagger", "shiv_gen", "block");
        tag("InfiniteBlades", "shiv_gen", "power");
        tag("StormOfSteel",   "shiv_gen", "exhaust");
        tag("Setup",          "shiv_gen", "zero_cost");
        tag("AfterImage",     "shiv_payoff", "block_on_play");
        tag("Flechettes",     "shiv_payoff", "attack");
        tag("AThousandCuts",  "shiv_payoff", "aoe", "power");
        tag("GlassKnife",     "shiv_payoff", "big_attack");
        tag("Expertise",      "discard_payoff", "draw");
        tag("Acrobatics",     "discard_payoff", "draw");
        tag("Concentrate",    "discard_trigger");
        tag("WellLaidPlans",  "retain", "discard_trigger");
        tag("Blur",           "block_retain", "block");
        tag("DodgeAndRoll",   "block", "retain");
        tag("Backflip",       "block", "draw");
        tag("Footwork",       "dexterity", "power");
        tag("EscapePlan",     "block", "draw");
        tag("Caltrops",       "thorns");
        tag("Predator",       "draw", "attack");
        tag("Burst",          "skill_double");
        tag("Nightmare",      "skill_double", "exhaust");
        tag("LegSweep",       "dexterity");

        group("Poison",        "poison_source", "poison_payoff");
        group("Shiv Engine",   "shiv_gen", "shiv_payoff");
        group("Discard Cycle", "discard_trigger", "discard_payoff");
        group("Block/Dex",     "block_retain", "dexterity");
    }

    public static String detectArchetype(Map<String, Integer> t) {
        int poison  = get(t,"poison_source") + get(t,"poison_payoff");
        int shiv    = get(t,"shiv_gen") + get(t,"shiv_payoff");
        int discard = get(t,"discard_trigger") + get(t,"discard_payoff");
        int block   = get(t,"block_retain") + get(t,"dexterity");
        if (poison >= 3)  return "Poison";
        if (shiv >= 3)    return "Shiv Engine";
        if (discard >= 3) return "Discard Cycle";
        if (block >= 3)   return "Block/Dex";
        return "Balanced";
    }

    private static void tag(String id, String... tags) { CARD_TAGS.put(id, Arrays.asList(tags)); }
    private static void group(String name, String... tags) { SYNERGY_GROUPS.put(name, Arrays.asList(tags)); }
    private static int get(Map<String,Integer> m, String k) { return m.getOrDefault(k, 0); }
}

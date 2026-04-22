package sts.advisor.data;

import java.util.*;

public class IroncladData {

    public static final Map<String, Integer> SCORES = new HashMap<>();
    public static final Map<String, Integer> UPGRADE_BONUS = new HashMap<>();
    public static final Map<String, List<String>> CARD_TAGS = new HashMap<>();
    public static final Map<String, List<String>> SYNERGY_GROUPS = new LinkedHashMap<>();

    static {
        SCORES.put("Reaper", 95);
        SCORES.put("DemonForm", 93);
        SCORES.put("Barricade", 92);
        SCORES.put("Bludgeon", 88);
        SCORES.put("FiendFire", 87);
        SCORES.put("Corruption", 86);
        SCORES.put("Impervious", 85);
        SCORES.put("Feed", 84);
        SCORES.put("Whirlwind", 83);
        SCORES.put("Offering", 82);
        SCORES.put("Combust", 81);
        SCORES.put("LimitBreak", 80);
        SCORES.put("Inflame", 79);
        SCORES.put("TrueGrit", 78);
        SCORES.put("Exhume", 77);
        SCORES.put("Brutality", 76);
        SCORES.put("SpotWeakness", 75);
        SCORES.put("SeverSoul", 74);
        SCORES.put("DarkEmbrace", 73);
        SCORES.put("FeelNoPain", 72);
        SCORES.put("SecondWind", 71);
        SCORES.put("BattleTrance", 70);
        SCORES.put("Carnage", 68);
        SCORES.put("Immolate", 67);
        SCORES.put("Uppercut", 66);
        SCORES.put("Pummel", 65);
        SCORES.put("ShrugItOff", 64);
        SCORES.put("PowerThrough", 63);
        SCORES.put("Entrench", 62);
        SCORES.put("HeavyBlade", 61);
        SCORES.put("Headbutt", 60);
        SCORES.put("BurningPact", 59);
        SCORES.put("FlameBarrier", 58);
        SCORES.put("GhostlyArmor", 57);
        SCORES.put("Bloodletting", 56);
        SCORES.put("Sentinel", 55);
        SCORES.put("BodySlam", 50);
        SCORES.put("Dropkick", 44);
        SCORES.put("WildStrike", 30);
        SCORES.put("Anger", 25);
        SCORES.put("Clash", 28);
        SCORES.put("Flex", 30);
        SCORES.put("Havoc", 22);
        SCORES.put("RecklessCharge", 20);
        SCORES.put("Strike_R", 10);
        SCORES.put("Defend_R", 10);

        UPGRADE_BONUS.put("Corruption", 10);
        UPGRADE_BONUS.put("Barricade", 5);
        UPGRADE_BONUS.put("LimitBreak", 8);
        UPGRADE_BONUS.put("Feed", 12);
        UPGRADE_BONUS.put("Whirlwind", 6);
        UPGRADE_BONUS.put("BodySlam", 8);
        UPGRADE_BONUS.put("Entrench", 8);
        UPGRADE_BONUS.put("Sentinel", 10);
        UPGRADE_BONUS.put("DarkEmbrace", 6);

        tag("Corruption",   "exhaust_enabler", "skill_exhaust");
        tag("DarkEmbrace",  "exhaust_payoff", "draw_on_exhaust");
        tag("FeelNoPain",   "exhaust_payoff", "block_on_exhaust");
        tag("SecondWind",   "exhaust_payoff", "block_on_exhaust");
        tag("Exhume",       "exhaust_payoff");
        tag("TrueGrit",     "exhaust", "block");
        tag("FiendFire",    "exhaust", "big_attack");
        tag("SeverSoul",    "exhaust", "attack");
        tag("BurningPact",  "exhaust", "draw");
        tag("Offering",     "exhaust", "draw");
        tag("Carnage",      "exhaust", "big_attack");
        tag("DemonForm",    "strength_scaling", "power");
        tag("LimitBreak",   "strength_scaling", "exhaust");
        tag("Inflame",      "strength_scaling", "power");
        tag("SpotWeakness", "strength_scaling", "attack");
        tag("Reaper",       "aoe", "lifesteal");
        tag("Whirlwind",    "aoe", "strength_scaling", "attack");
        tag("Immolate",     "aoe", "attack");
        tag("Combust",      "aoe", "power");
        tag("Barricade",    "block_retain", "power");
        tag("Entrench",     "block_retain", "block");
        tag("Impervious",   "block_retain", "block", "exhaust");
        tag("BodySlam",     "block_retain", "attack");
        tag("GhostlyArmor", "block_retain", "block", "exhaust");
        tag("Brutality",    "draw", "aggro");
        tag("BattleTrance", "draw");
        tag("Headbutt",     "draw");
        tag("FlameBarrier", "block_on_attack", "block");
        tag("Bludgeon",     "big_attack");

        group("Exhaust Engine",   "exhaust_enabler", "exhaust_payoff", "draw_on_exhaust", "block_on_exhaust");
        group("Strength Scaling", "strength_scaling");
        group("Block Fortress",   "block_retain");
        group("AoE Package",      "aoe");
    }

    public static String detectArchetype(Map<String, Integer> t) {
        int exhaust  = get(t,"exhaust_enabler") + get(t,"exhaust_payoff");
        int strength = get(t,"strength_scaling");
        int block    = get(t,"block_retain");
        int aggro    = get(t,"aggro") + get(t,"big_attack");
        if (exhaust >= 3)  return "Exhaust Engine";
        if (strength >= 3) return "Strength Scaling";
        if (block >= 3)    return "Block Fortress";
        if (aggro >= 3)    return "Aggro";
        return "Balanced";
    }

    private static void tag(String id, String... tags) { CARD_TAGS.put(id, Arrays.asList(tags)); }
    private static void group(String name, String... tags) { SYNERGY_GROUPS.put(name, Arrays.asList(tags)); }
    private static int get(Map<String,Integer> m, String k) { return m.getOrDefault(k, 0); }
}

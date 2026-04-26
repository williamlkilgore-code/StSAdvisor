package sts.advisor.data;

import java.util.*;

public class SilentData {

    public static final Map<String, Integer> SCORES = new HashMap<>();
    public static final Map<String, Integer> UPGRADE_BONUS = new HashMap<>();
    public static final Map<String, List<String>> CARD_TAGS = new HashMap<>();
    public static final Map<String, List<String>> SYNERGY_GROUPS = new LinkedHashMap<>();

    static {
        // S tier
        SCORES.put("Nightmare", 95);
        SCORES.put("Wraith Form", 92);
        SCORES.put("Envenom", 90);
        SCORES.put("Noxious Fumes", 89);
        SCORES.put("Malaise", 88);
        SCORES.put("Infinite Blades", 87);
        SCORES.put("Glass Knife", 85);
        SCORES.put("A Thousand Cuts", 84);
        SCORES.put("Catalyst", 83);
        // A tier
        SCORES.put("Die Die Die", 81);
        SCORES.put("After Image", 82);
        SCORES.put("Footwork", 80);
        SCORES.put("Dagger Spray", 79);
        SCORES.put("Storm of Steel", 78);
        SCORES.put("Well-Laid Plans", 77);
        SCORES.put("Well Laid Plans", 77);   // alternate ID
        SCORES.put("Burst", 75);
        SCORES.put("Expertise", 74);
        SCORES.put("Backflip", 76);
        SCORES.put("Corpse Explosion", 71);
        SCORES.put("Predator", 70);
        SCORES.put("Phantasmal Killer", 72);
        SCORES.put("Tools of the Trade", 70);
        // B tier
        SCORES.put("Eviscerate", 68);
        SCORES.put("Flechettes", 66);
        SCORES.put("Blade Dance", 65);
        SCORES.put("Caltrops", 64);
        SCORES.put("Bouncing Flask", 63);
        SCORES.put("Crippling Cloud", 62);
        SCORES.put("Setup", 61);
        SCORES.put("Choke", 59);
        SCORES.put("Riddle with Holes", 58);
        SCORES.put("Riddle With Holes", 58);
        SCORES.put("Distraction", 57);
        SCORES.put("Outmaneuver", 56);
        SCORES.put("Escape Plan", 55);
        SCORES.put("Blur", 54);
        SCORES.put("Dodge and Roll", 52);
        SCORES.put("Dodge And Roll", 52);
        SCORES.put("Acrobatics", 50);
        SCORES.put("Calculated Gamble", 55);
        SCORES.put("Endless Agony", 58);
        SCORES.put("Finisher", 60);
        SCORES.put("Reflex", 62);
        SCORES.put("Tactician", 62);
        SCORES.put("Skewer", 58);
        SCORES.put("All-Out Attack", 55);
        SCORES.put("All Out Attack", 55);
        SCORES.put("Unload", 50);
        SCORES.put("Deadly Poison", 60);
        // C tier
        SCORES.put("Sneaky Strike", 48);
        SCORES.put("Terror", 46);
        SCORES.put("Leg Sweep", 44);
        SCORES.put("Quick Slash", 45);
        SCORES.put("Flying Knee", 36);
        SCORES.put("Sucker Punch", 43);
        SCORES.put("Piercing Wail", 35);
        SCORES.put("PiercingWail", 35);
        SCORES.put("Slice", 32);
        SCORES.put("Dagger Throw", 48);
        SCORES.put("Deflect", 38);
        SCORES.put("Prepared", 42);
        SCORES.put("Poisoned Stab", 45);
        // D tier
        SCORES.put("Bane", 25);
        SCORES.put("Backstab", 22);
        // Starter
        SCORES.put("Strike_G", 10);
        SCORES.put("Defend_G", 10);
        SCORES.put("Neutralize", 18);
        SCORES.put("Survivor", 18);

        // Upgrade bonuses
        UPGRADE_BONUS.put("Catalyst", 15);
        UPGRADE_BONUS.put("Nightmare", 8);
        UPGRADE_BONUS.put("Noxious Fumes", 6);
        UPGRADE_BONUS.put("Well-Laid Plans", 8);
        UPGRADE_BONUS.put("Well Laid Plans", 8);
        UPGRADE_BONUS.put("Flechettes", 10);
        UPGRADE_BONUS.put("Burst", 8);
        UPGRADE_BONUS.put("Backflip", 6);
        UPGRADE_BONUS.put("After Image", 5);
        UPGRADE_BONUS.put("Reflex", 8);
        UPGRADE_BONUS.put("Tactician", 8);

        // Card tags — using space-separated IDs
        tag("Noxious Fumes",   "poison_source", "power");
        tag("Envenom",         "poison_source", "power");
        tag("Catalyst",        "poison_payoff");
        tag("Bouncing Flask",  "poison_source");
        tag("Crippling Cloud", "poison_source");
        tag("Corpse Explosion","poison_payoff", "aoe");
        tag("Bane",            "poison_payoff", "attack");
        tag("Deadly Poison",   "poison_source");
        tag("Blade Dance",     "shiv_gen");
        tag("Cloak and Dagger","shiv_gen", "block");
        tag("Cloak And Dagger","shiv_gen", "block");
        tag("Infinite Blades", "shiv_gen", "power");
        tag("Storm of Steel",  "shiv_gen", "exhaust");
        tag("Setup",           "shiv_gen", "zero_cost");
        tag("After Image",     "shiv_payoff", "block_on_play");
        tag("Flechettes",      "shiv_payoff", "attack");
        tag("A Thousand Cuts", "shiv_payoff", "aoe", "power");
        tag("Glass Knife",     "shiv_payoff", "big_attack");
        tag("Expertise",       "discard_payoff", "draw");
        tag("Acrobatics",      "discard_payoff", "draw");
        tag("Calculated Gamble","discard_payoff");
        tag("Reflex",          "discard_payoff");
        tag("Tactician",       "discard_payoff");
        tag("Concentrate",     "discard_trigger");
        tag("Well-Laid Plans", "retain", "discard_trigger");
        tag("Well Laid Plans", "retain", "discard_trigger");
        tag("Blur",            "block_retain", "block");
        tag("Dodge and Roll",  "block", "retain");
        tag("Dodge And Roll",  "block", "retain");
        tag("Backflip",        "block", "draw");
        tag("Footwork",        "dexterity", "power");
        tag("Escape Plan",     "block", "draw");
        tag("Caltrops",        "thorns");
        tag("Predator",        "draw", "attack");
        tag("Burst",           "skill_double");
        tag("Nightmare",       "skill_double", "exhaust");
        tag("Leg Sweep",       "dexterity");
        tag("Dagger Throw",    "draw", "attack");
        tag("Die Die Die",     "aoe", "exhaust");
        tag("Finisher",        "attack");
        tag("Endless Agony",   "exhaust", "attack");
        tag("All-Out Attack",  "aoe");
        tag("All Out Attack",  "aoe");
        tag("Tools of the Trade", "draw", "power");

        // Synergy groups
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

    private static void tag(String card, String... tags) {
        CARD_TAGS.put(card, Arrays.asList(tags));
    }
    private static void group(String name, String... tags) {
        SYNERGY_GROUPS.put(name, Arrays.asList(tags));
    }
    private static int get(Map<String, Integer> map, String key) {
        return map.getOrDefault(key, 0);
    }
}

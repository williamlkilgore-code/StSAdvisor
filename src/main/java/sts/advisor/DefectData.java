package sts.advisor.data;

import java.util.*;

public class DefectData {

    public static final Map<String, Integer> SCORES = new HashMap<>();
    public static final Map<String, Integer> UPGRADE_BONUS = new HashMap<>();
    public static final Map<String, List<String>> CARD_TAGS = new HashMap<>();
    public static final Map<String, List<String>> SYNERGY_GROUPS = new LinkedHashMap<>();

    static {
        SCORES.put("Glacier", 94);
        SCORES.put("Electrodynamics", 93);
        SCORES.put("MeteorStrike", 90);
        SCORES.put("AllForOne", 92);
        SCORES.put("Seek", 88);
        SCORES.put("Fission", 87);
        SCORES.put("Amplify", 86);
        SCORES.put("Defragment", 85);
        SCORES.put("CreativeAI", 84);
        SCORES.put("EchoForm", 83);
        SCORES.put("Hyperbeam", 82);
        SCORES.put("Blizzard", 81);
        SCORES.put("Storm", 80);
        SCORES.put("Rainbow", 79);
        SCORES.put("Turbo", 78);
        SCORES.put("Reprogram", 77);
        SCORES.put("Reboot", 76);
        SCORES.put("Consume", 75);
        SCORES.put("Darkness", 74);
        SCORES.put("Capacitor", 73);
        SCORES.put("Fusion", 72);
        SCORES.put("Equilibrium", 71);
        SCORES.put("Loop", 70);
        SCORES.put("Chill", 68);
        SCORES.put("Sunder", 67);
        SCORES.put("Heatsinks", 66);
        SCORES.put("SelfRepair", 65);
        SCORES.put("Skim", 64);
        SCORES.put("Stack", 63);
        SCORES.put("Recycle", 62);
        SCORES.put("ColdSnap", 61);
        SCORES.put("Aggregate", 60);
        SCORES.put("StaticDischarge", 58);
        SCORES.put("WhiteNoise", 57);
        SCORES.put("ChargeBattery", 56);
        SCORES.put("ForceField", 55);
        SCORES.put("Coolheaded", 50);
        SCORES.put("CompileDriver", 48);
        SCORES.put("GoForTheEyes", 44);
        SCORES.put("Hologram", 46);
        SCORES.put("BeamCell", 36);
        SCORES.put("Rebound", 40);
        SCORES.put("BallLightning", 32);
        SCORES.put("Chaos", 28);
        SCORES.put("Strike_B", 10);
        SCORES.put("Defend_B", 10);
        SCORES.put("Dualcast", 18);
        SCORES.put("Zap", 18);

        UPGRADE_BONUS.put("Electrodynamics", 8);
        UPGRADE_BONUS.put("AllForOne", 6);
        UPGRADE_BONUS.put("EchoForm", 5);
        UPGRADE_BONUS.put("Defragment", 7);
        UPGRADE_BONUS.put("Glacier", 6);
        UPGRADE_BONUS.put("Hyperbeam", 5);
        UPGRADE_BONUS.put("Reboot", 8);
        UPGRADE_BONUS.put("ForceField", 10);
        UPGRADE_BONUS.put("Loop", 8);
        UPGRADE_BONUS.put("Reprogram", 7);

        tag("ColdSnap",        "frost_gen", "attack");
        tag("Glacier",         "frost_gen", "block");
        tag("Chill",           "frost_gen", "exhaust");
        tag("Coolheaded",      "frost_gen", "draw");
        tag("Blizzard",        "frost_payoff", "frost_gen", "aoe");
        tag("Electrodynamics", "lightning_payoff", "frost_gen", "power");
        tag("Storm",           "lightning_gen", "power");
        tag("Capacitor",       "lightning_gen", "power");
        tag("StaticDischarge", "lightning_gen");
        tag("BallLightning",   "lightning_gen", "attack");
        tag("ChargeBattery",   "lightning_gen", "block");
        tag("Fusion",          "plasma_gen");
        tag("MeteorStrike",    "plasma_gen", "big_attack");
        tag("Fission",         "plasma_gen", "multi_orb");
        tag("Rainbow",         "plasma_gen", "frost_gen", "lightning_gen");
        tag("Seek",            "orb_any", "exhaust");
        tag("AllForOne",       "zero_cost_cycle", "orb_any");
        tag("Defragment",      "focus", "power");
        tag("Reprogram",       "focus", "power");
        tag("Amplify",         "evoke_payoff", "power");
        tag("EchoForm",        "evoke_payoff", "power");
        tag("Darkness",        "evoke_payoff");
        tag("Loop",            "evoke_payoff");
        tag("Consume",         "focus", "exhaust");
        tag("Equilibrium",     "block", "retain");
        tag("ForceField",      "block", "orb_any");
        tag("Stack",           "block");
        tag("Skim",            "draw");
        tag("Turbo",           "energy", "exhaust");
        tag("Aggregate",       "energy");
        tag("Reboot",          "draw", "exhaust");
        tag("CreativeAI",      "power", "draw");
        tag("Heatsinks",       "draw", "power");
        tag("Hologram",        "retain", "exhaust");
        tag("CompileDriver",   "orb_any", "attack");

        group("Frost Fortress",  "frost_gen", "frost_payoff");
        group("Lightning Storm", "lightning_gen", "lightning_payoff");
        group("Nova/Plasma",     "plasma_gen", "evoke_payoff");
        group("High Focus",      "focus", "evoke_payoff");
        group("Zero-Cost Loop",  "zero_cost_cycle", "orb_any");
    }

    public static String detectArchetype(Map<String, Integer> t) {
        int frost     = get(t,"frost_gen") + get(t,"frost_payoff");
        int lightning = get(t,"lightning_gen") + get(t,"lightning_payoff");
        int plasma    = get(t,"plasma_gen") + get(t,"evoke_payoff");
        int focus     = get(t,"focus");
        int zeroCost  = get(t,"zero_cost_cycle");
        if (plasma >= 3 || focus >= 3) return "Nova/Plasma";
        if (zeroCost >= 2)             return "Zero-Cost Loop";
        if (lightning >= 3)            return "Lightning Storm";
        if (frost >= 3)                return "Frost Fortress";
        return "Balanced";
    }

    private static void tag(String id, String... tags) { CARD_TAGS.put(id, Arrays.asList(tags)); }
    private static void group(String name, String... tags) { SYNERGY_GROUPS.put(name, Arrays.asList(tags)); }
    private static int get(Map<String,Integer> m, String k) { return m.getOrDefault(k, 0); }
}

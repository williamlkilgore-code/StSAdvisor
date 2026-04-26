package sts.advisor.data;

import java.util.*;

/**
 * Watcher card data — keyed by cardID (matches AbstractCard.cardID).
 * IDs sourced from CommunicationMod state output.
 */
public class WatcherData {

    public static final Map<String, Integer> SCORES = new HashMap<>();
    public static final Map<String, Integer> UPGRADE_BONUS = new HashMap<>();
    public static final Map<String, List<String>> CARD_TAGS = new HashMap<>();
    public static final Map<String, List<String>> SYNERGY_GROUPS = new LinkedHashMap<>();

    static {
        // S tier
        SCORES.put("Scrawl", 95);
        SCORES.put("Nirvana", 93);
        SCORES.put("Judgement", 90);
        SCORES.put("Blasphemy", 88);
        SCORES.put("Brilliance", 87);
        SCORES.put("TalkToTheHand", 86);
        SCORES.put("Collect", 85);
        // A tier
        SCORES.put("LessonLearned", 84);
        SCORES.put("MentalFortress", 83);
        SCORES.put("InnerPeace", 82);
        SCORES.put("Ragnarok", 81);
        SCORES.put("Establishment", 80);
        SCORES.put("SpiritShield", 79);
        SCORES.put("Weave", 78);
        SCORES.put("Devotion", 77);
        SCORES.put("Worship", 76);
        SCORES.put("Consecrate", 75);
        SCORES.put("SandsOfTime", 74);
        SCORES.put("Swivel", 73);
        SCORES.put("Pray", 72);
        SCORES.put("Meditate", 71);
        SCORES.put("FlurryOfBlows", 72);   // stance cycling, returns on change
        SCORES.put("SignatureMove", 70);
        // B tier
        SCORES.put("FearNoEvil", 68);
        SCORES.put("Fasting", 67);
        SCORES.put("Adaptation", 66);      // Rushdown
        SCORES.put("Wallop", 65);
        SCORES.put("Conclude", 64);
        SCORES.put("EmptyFist", 63);
        SCORES.put("EmptyBody", 62);
        SCORES.put("Halt", 61);
        SCORES.put("CarveReality", 60);
        SCORES.put("ThirdEye", 59);
        SCORES.put("Perseverance", 58);
        SCORES.put("LikeWater", 57);
        SCORES.put("Vault", 56);
        SCORES.put("Prostrate", 55);
        // C tier
        SCORES.put("Crescendo", 52);
        SCORES.put("ClearTheMind", 50);    // Tranquility
        SCORES.put("Indignation", 48);
        SCORES.put("Vengeance", 46);
        SCORES.put("FollowUp", 44);
        SCORES.put("Sanctity", 42);
        SCORES.put("CutThroughFate", 40);
        SCORES.put("Evaluate", 38);
        SCORES.put("WheelKick", 36);
        SCORES.put("BattleHymn", 50);      // generates smites each turn — ok in wrath
        SCORES.put("PathToVictory", 42);   // Pressure Points — mark stacking
        SCORES.put("WreathOfFlame", 55);   // applies burn in wrath
        SCORES.put("FlyingSleeves", 48);   // attacks twice
        SCORES.put("EmptyMind", 55);       // draw 2, exit stance
        SCORES.put("CrushJoints", 50);     // vulnerable if last was skill
        SCORES.put("BowlingBash", 45);
        SCORES.put("SashWhip", 38);
        SCORES.put("WaveOfTheHand", 52);   // weak on stance enter
        // D tier
        SCORES.put("DeceiveReality", 30);
        SCORES.put("ReachHeaven", 28);
        SCORES.put("Protect", 25);
        SCORES.put("Smite", 22);
        SCORES.put("WindmillStrike", 20);
        SCORES.put("Study", 18);
        SCORES.put("Alpha", 15);
        // Starter / tokens
        SCORES.put("Strike_P", 10);
        SCORES.put("Defend_P", 10);
        SCORES.put("Eruption", 15);
        SCORES.put("Vigilance", 15);
        SCORES.put("Panacea", 40);
        SCORES.put("Transmutation", 35);
        SCORES.put("JustLucky", 30);
        // Missing Watcher cards
        SCORES.put("Tantrum", 68);           // attacks 3x, enters Wrath, shuffles back
        SCORES.put("EmptyMind", 55);         // draw 2, exit stance
        SCORES.put("Omniscience", 85);       // play next card twice — S tier
        SCORES.put("DevaForm", 90);          // S tier — energy gain each turn
        SCORES.put("Wireheading", 62);       // Foresight — scry 3 each turn
        SCORES.put("Fasting2", 67);          // Fasting upgraded form
        // Wish cards — obtained from Wish spell
        SCORES.put("BecomeAlmighty", 75);    // gain 3 strength permanently
        SCORES.put("FameAndFortune", 65);    // gain 30 gold
        SCORES.put("LiveForever", 72);       // gain 5 max HP
        // Other missing
        SCORES.put("ConjureBlade", 65);

        // Upgrade bonuses
        UPGRADE_BONUS.put("LessonLearned", 10);
        UPGRADE_BONUS.put("InnerPeace", 12);
        UPGRADE_BONUS.put("SandsOfTime", 15);
        UPGRADE_BONUS.put("TalkToTheHand", 8);
        UPGRADE_BONUS.put("Devotion", 8);
        UPGRADE_BONUS.put("Nirvana", 5);
        UPGRADE_BONUS.put("Adaptation", 5);    // Rushdown
        UPGRADE_BONUS.put("FlurryOfBlows", 4);
        UPGRADE_BONUS.put("ClearTheMind", 6);  // Tranquility — exhaust on upgrade

        // Card tags — keyed by cardID
        tag("Rushdown",       "stance_enter", "draw_on_wrath");
        tag("Nirvana",        "stance_enter", "block_on_calm");
        tag("MentalFortress", "stance_change", "block_on_change");
        tag("Establishment",  "stance_enter", "cost_reduction");
        tag("Weave",          "exhaust", "stance_enter");
        tag("ClearTheMind",  "calm_enter");  // Tranquility
        tag("Crescendo",      "wrath_enter");
        tag("Meditate",       "calm_enter", "retain");
        tag("Swivel",         "stance_enter");
        tag("Scrawl",         "draw", "stance_change");
        tag("SandsOfTime",    "retain", "big_attack");
        tag("InnerPeace",     "calm_user", "draw");
        tag("ThirdEye",       "calm_user", "retain", "scry");
        tag("Perseverance",   "retain", "block");
        tag("FollowUp",       "retain");
        tag("EmptyBody",      "calm_enter", "block");
        tag("Halt",           "wrath_block", "block");
        tag("Prostrate",      "mantra", "block");
        tag("LikeWater",      "calm_passive", "block");
        tag("SpiritShield",   "hand_size", "block");
        tag("Devotion",       "mantra");
        tag("Worship",        "mantra");
        tag("Collect",        "mantra", "wrath_user");
        tag("Judgement",      "divinity_payoff");
        tag("Brilliance",     "divinity_payoff", "mantra");
        tag("Blasphemy",      "divinity_payoff");
        tag("Ragnarok",       "wrath_user", "big_attack");
        tag("LessonLearned",  "exhaust", "upgrade");
        tag("TalkToTheHand",  "wrath_user", "block_on_attack");
        tag("SignatureMove",  "big_attack");
        tag("Wallop",         "attack", "block");
        tag("Conclude",       "attack", "end_turn");
        tag("Consecrate",     "aoe");
        tag("WheelKick",      "attack", "draw");
        tag("CarveReality",   "attack");
        tag("Sanctity",       "skill", "draw");
        tag("Evaluate",       "exhaust");
        tag("Vault",          "exhaust");
        tag("Fasting",        "energy");
        tag("Pray",           "mantra");
        tag("FearNoEvil",     "attack", "stance_based");
        tag("Adaptation",     "stance_enter", "draw_on_wrath");  // Rushdown
        tag("FlurryOfBlows",  "stance_enter", "stance_change", "attack");
        tag("WaveOfTheHand",  "stance_enter", "weak");
        tag("CrushJoints",    "attack", "stance_based");
        tag("SashWhip",       "attack");
        tag("EmptyMind",      "calm_enter", "draw");
        tag("WreathOfFlame",  "wrath_user", "attack");
        tag("BattleHymn",     "wrath_user");
        tag("PathToVictory",  "attack");   // Pressure Points
        tag("Tantrum",        "wrath_enter", "stance_enter", "attack");
        tag("Wireheading",    "scry");     // Foresight

        // Synergy groups
        group("Stance Cycling",  "stance_enter", "stance_change", "block_on_change",
                                 "draw_on_wrath", "block_on_calm", "cost_reduction");
        group("Mantra/Divinity", "mantra", "divinity_payoff");
        group("Retain Control",  "retain", "big_attack", "scry", "hand_size");
        group("Calm Block",      "calm_user", "calm_enter", "calm_passive", "block");
        group("Wrath Aggro",     "wrath_user", "wrath_enter", "wrath_block");
        group("Exhaust Package", "exhaust", "upgrade");
    }

    public static String detectArchetype(Map<String, Integer> t) {
        int mantra = get(t,"mantra") + get(t,"divinity_payoff");
        int stance = get(t,"stance_enter") + get(t,"stance_change");
        int retain = get(t,"retain");
        int calm   = get(t,"calm_user") + get(t,"calm_passive");
        int wrath  = get(t,"wrath_user") + get(t,"wrath_enter");
        if (mantra >= 3)              return "Mantra/Divinity";
        if (stance >= 4)              return "Stance Cycling";
        if (retain >= 3)              return "Retain Control";
        if (calm >= 3 && wrath <= 1)  return "Calm Block";
        if (wrath >= 3)               return "Wrath Aggro";
        return "Balanced";
    }

    private static void tag(String id, String... tags) { CARD_TAGS.put(id, Arrays.asList(tags)); }
    private static void group(String name, String... tags) { SYNERGY_GROUPS.put(name, Arrays.asList(tags)); }
    private static int get(Map<String,Integer> m, String k) { return m.getOrDefault(k, 0); }
}

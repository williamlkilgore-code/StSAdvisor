package sts.advisor;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.*;

import java.util.*;

/**
 * Path-based map advisor.
 * Finds and scores the top 2 full paths from current position to boss.
 */
public class MapAdvisor {

    public enum Strategy { SAFE, BALANCED, AGGRESSIVE }
    public static Strategy strategy = Strategy.BALANCED;

    private static final String WINGED_BOOTS_ID = "WingedBoots";

    // ── Public API ────────────────────────────────────────────────────────────

    public static List<ScoredPath> advise() {
        if (AbstractDungeon.map == null || AbstractDungeon.player == null)
            return Collections.emptyList();

        boolean wingedBoots = hasRelic(WINGED_BOOTS_ID);
        List<MapRoomNode> choices = getChoiceNodes(wingedBoots);
        if (choices.size() < 2) return Collections.emptyList();

        Map<String, MapRoomNode> lookup = buildLookup();
        int act     = AbstractDungeon.actNum;
        float hpPct = (float) AbstractDungeon.player.currentHealth
                    / AbstractDungeon.player.maxHealth;
        int gold    = AbstractDungeon.player.gold;

        // Find best full path per choice node
        List<ScoredPath> candidates = new ArrayList<>();
        for (MapRoomNode start : choices) {
            List<List<MapRoomNode>> allPaths = findPaths(start, lookup);
            ScoredPath best = null;
            for (List<MapRoomNode> p : allPaths) {
                ScoredPath sp = scorePath(p, act, hpPct, gold);
                if (best == null || sp.score > best.score) best = sp;
            }
            if (best != null) candidates.add(best);
        }

        candidates.sort((a, b) -> b.score - a.score);

        // Return top 2 distinct paths (different start node x positions)
        List<ScoredPath> results = new ArrayList<>();
        Set<Integer> usedX = new HashSet<>();
        for (ScoredPath sp : candidates) {
            MapRoomNode sn = sp.startNode();
            if (sn == null) continue;
            if (!usedX.contains(sn.x)) {
                usedX.add(sn.x);
                results.add(sp);
            }
            if (results.size() >= 2) break;
        }
        return results;
    }

    public static boolean hasChoice() {
        if (AbstractDungeon.map == null || AbstractDungeon.player == null) return false;
        return getChoiceNodes(hasRelic(WINGED_BOOTS_ID)).size() >= 2;
    }

    // ── Path finding ──────────────────────────────────────────────────────────

    private static List<MapRoomNode> getChoiceNodes(boolean wingedBoots) {
        List<MapRoomNode> choices = new ArrayList<>();
        MapRoomNode curr = AbstractDungeon.currMapNode;
        int nextFloor = (curr == null || curr.y == -1) ? 0 : curr.y + 1;
        if (nextFloor >= AbstractDungeon.map.size()) return choices;

        for (MapRoomNode node : AbstractDungeon.map.get(nextFloor)) {
            if (node == null || node.room == null) continue;
            if (wingedBoots || curr == null || curr.y == -1 || curr.isConnectedTo(node))
                choices.add(node);
        }
        return choices;
    }

    private static List<List<MapRoomNode>> findPaths(MapRoomNode start,
                                                      Map<String, MapRoomNode> lookup) {
        List<List<MapRoomNode>> results = new ArrayList<>();
        dfs(start, new ArrayList<>(), lookup, results, 0);
        return results;
    }

    private static void dfs(MapRoomNode node, List<MapRoomNode> current,
                              Map<String, MapRoomNode> lookup,
                              List<List<MapRoomNode>> results, int depth) {
        if (depth > 20) return;
        List<MapRoomNode> path = new ArrayList<>(current);
        path.add(node);
        List<MapRoomNode> children = getChildren(node, lookup);
        if (children.isEmpty()) { results.add(path); return; }
        for (MapRoomNode child : children) dfs(child, path, lookup, results, depth + 1);
    }

    private static List<MapRoomNode> getChildren(MapRoomNode node,
                                                   Map<String, MapRoomNode> lookup) {
        List<MapRoomNode> children = new ArrayList<>();
        for (MapEdge edge : node.getEdges()) {
            if (edge == null) continue;
            MapRoomNode child = lookup.get(edge.dstX + "," + edge.dstY);
            if (child != null && child.room != null) children.add(child);
        }
        return children;
    }

    // ── Deck profile ──────────────────────────────────────────────────────────

    /**
     * Returns modifiers based on current deck archetype and completeness.
     * Values are bonuses/penalties applied per fight, shop, elite, rest, event.
     */
    private static DeckProfile getDeckProfile() {
        SynergyEngine.CharData data = SynergyEngine.getCharData();
        if (data == null) return new DeckProfile();

        Map<String, Integer> tags = SynergyEngine.buildDeckTags(data.cardTags);
        String archetype = data.archetype(tags);
        int deckSize = AbstractDungeon.player.masterDeck.group.size();

        // Completeness: how many synergy group tags are represented
        int groupsRepresented = 0;
        for (List<String> groupTags : data.synergyGroups.values()) {
            for (String t : groupTags) {
                if (tags.getOrDefault(t, 0) >= 2) { groupsRepresented++; break; }
            }
        }
        boolean deckDeveloped = groupsRepresented >= 2 || deckSize > 22;

        DeckProfile p = new DeckProfile();

        // ── Character + archetype adjustments ────────────────────────────────
        switch (archetype) {

            // WATCHER
            case "Stance Cycling":
                p.fightBonus  = deckDeveloped ? 1 : 3;  // needs specific cards early
                p.eventBonus  = 2;  // Watcher has good event pool
                p.eliteBonus  = 1;
                break;
            case "Mantra/Divinity":
                p.fightBonus  = deckDeveloped ? 1 : 4;  // combo needs specific pieces
                p.shopBonus   = deckDeveloped ? 3 : 1;
                break;
            case "Retain Control":
                p.shopBonus   = 3;  // removal helps thin the deck
                p.restBonus   = 2;  // survival matters for control
                p.eliteThresholdMod = 0.05f;
                break;
            case "Calm Block":
                p.restBonus   = 3;  // HP preservation is the win condition
                p.eliteThresholdMod = 0.10f;
                p.eliteBonus  = -2;
                break;
            case "Wrath Aggro":
                p.eliteBonus  = 4;  // relics synergize with damage output
                p.fightBonus  = 2;
                p.eliteThresholdMod = -0.05f;
                break;

            // IRONCLAD
            case "Exhaust Engine":
                p.shopBonus   = 5;  // needs Feel No Pain, Dark Embrace, Corruption
                p.fightBonus  = deckDeveloped ? 1 : 3;
                break;
            case "Strength Scaling":
                p.eliteBonus  = 4;  // Spot Weakness, relic synergy
                p.eliteThresholdMod = -0.05f;
                p.fightBonus  = 2;
                break;
            case "Block Fortress":
                p.restBonus   = 3;  // HP = block via Body Slam/Barricade
                p.eliteThresholdMod = 0.10f;
                p.eliteBonus  = -1;
                break;
            case "Aggro":
                p.eliteBonus  = 3;
                p.fightBonus  = 2;
                p.shopBonus   = 1;
                break;

            // SILENT
            case "Poison":
                p.shopBonus   = 5;  // Catalyst and poison cards are rare
                p.fightBonus  = deckDeveloped ? 1 : 3;
                p.eventBonus  = 2;  // Silent has good event pool
                break;
            case "Shiv Engine":
                p.fightBonus  = deckDeveloped ? 2 : 4;  // needs A Thousand Cuts, After Image
                p.eventBonus  = 2;
                p.shopBonus   = 2;
                break;
            case "Discard Cycle":
                p.fightBonus  = deckDeveloped ? 1 : 3;
                p.shopBonus   = 2;
                break;
            case "Block/Dex":
                p.restBonus   = 3;
                p.eliteThresholdMod = 0.10f;
                p.shopBonus   = 2;  // removal helps
                break;

            // DEFECT
            case "Frost Fortress":
                p.restBonus   = 2;
                p.fightBonus  = deckDeveloped ? 1 : 2;
                break;
            case "Lightning Storm":
                p.fightBonus  = deckDeveloped ? 1 : 3;  // needs specific orb cards
                p.eliteBonus  = 2;
                break;
            case "Nova/Plasma":
                p.shopBonus   = 5;  // Fusion, Fission very hard to find
                p.fightBonus  = deckDeveloped ? 2 : 4;
                break;
            case "Zero-Cost Loop":
                p.fightBonus  = deckDeveloped ? 1 : 4;  // All For One is rare
                p.shopBonus   = 3;
                break;

            default: // Balanced / undefined — needs card rewards most
                p.fightBonus  = 4;
                p.shopBonus   = 2;
                break;
        }

        // Universal: developed deck cares more about survival
        if (deckDeveloped) {
            p.restBonus   += 2;
            p.eliteThresholdMod += 0.05f;
        }

        return p;
    }

    private static class DeckProfile {
        int   fightBonus         = 0;
        int   shopBonus          = 0;
        int   eliteBonus         = 0;
        int   restBonus          = 0;
        int   eventBonus         = 0;
        float eliteThresholdMod  = 0f;
    }

    // ── Path scoring ──────────────────────────────────────────────────────────

    private static ScoredPath scorePath(List<MapRoomNode> nodes, int act,
                                         float hpPct, int gold) {
        int score = 20;
        List<String> reasons = new ArrayList<>();

        int fights = 0, elites = 0, rests = 0, shops = 0, events = 0, chests = 0;
        List<Integer> elitePos = new ArrayList<>();
        List<Integer> restPos  = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            MapRoomNode n = nodes.get(i);
            if      (n.room instanceof MonsterRoomElite) { elites++; elitePos.add(i); }
            else if (n.room instanceof RestRoom)         { rests++;  restPos.add(i);  }
            else if (n.room instanceof ShopRoom)         { shops++;                   }
            else if (n.room instanceof EventRoom)        { events++;                  }
            else if (n.room instanceof TreasureRoom)     { chests++;                  }
            else if (n.room instanceof MonsterRoom)      { fights++;                  }
        }

        DeckProfile deck = getDeckProfile();

        // Fights — card rewards, adjusted by deck needs
        score += fights * (act == 1 ? 3 + deck.fightBonus
                         : act == 2 ? 2 + deck.fightBonus
                         :            1 + Math.max(0, deck.fightBonus - 1));

        // Shops — adjusted by deck needs and gold
        int shopBase = gold >= 100 ? 7 : gold >= 50 ? 4 : 2;
        score += shops * (shopBase + deck.shopBonus);
        if (shops == 0 && act >= 2) {
            score -= 4;
            reasons.add("No shop for card removal");
        }

        // Events — adjusted by character/archetype
        score += events * (act == 1 ? 4 + deck.eventBonus
                         : act == 2 ? 2 + deck.eventBonus
                         :            1);

        // Chests
        score += chests * 7;

        // Elites — threshold adjusted by deck profile
        float threshold = act == 1 ? 0.55f : act == 2 ? 0.45f : 0.40f;
        threshold += deck.eliteThresholdMod;
        if (strategy == Strategy.SAFE)       threshold += 0.15f;
        if (strategy == Strategy.AGGRESSIVE) threshold -= 0.15f;

        if (elites == 0) {
            score -= 10;
            reasons.add("No elites — missing relic chances");
        } else if (elites == 1) {
            int eliteScore = hpPct >= threshold ? 10 + deck.eliteBonus : -5;
            score += eliteScore;
            if (hpPct < threshold) reasons.add("Elite at low HP");
            else reasons.add("1 elite with relic opportunity");
        } else if (elites == 2) {
            int eliteScore = hpPct >= threshold ? 18 + deck.eliteBonus * 2 : -8;
            score += eliteScore;
            if (hpPct < threshold) reasons.add("2 elites at low HP — risky");
            else reasons.add("2 elites — strong relic gain");
        } else {
            score += 12 - (elites - 2) * 10 + deck.eliteBonus;
            reasons.add(elites + " elites — overloaded");
        }

        // Elite spacing
        for (int i = 0; i < elitePos.size() - 1; i++) {
            boolean restBetween = false;
            for (int rp : restPos)
                if (rp > elitePos.get(i) && rp < elitePos.get(i + 1)) { restBetween = true; break; }
            if (!restBetween) { score -= 12; reasons.add("Back-to-back elites, no recovery"); break; }
        }

        // Rest sites — adjusted by deck profile
        int restVal = hpPct < 0.5f ? 10 : hpPct < 0.7f ? 6 : 3;
        restVal += (act - 1) * 2 + deck.restBonus;
        score += rests * restVal;

        // Act-specific
        if (act == 1 && fights >= 5)  score += 5;
        if (act == 3 && rests >= 2)   { score += 7; reasons.add("Multiple rests in Act 3"); }
        if (strategy == Strategy.AGGRESSIVE) score += elites * 4;
        if (strategy == Strategy.SAFE)       { score += rests * 3; score -= elites * 2; }

        if (reasons.isEmpty()) {
            if (elites >= 1 && shops >= 1) reasons.add("Balanced — elite + shop");
            else if (elites >= 1)          reasons.add("Good relic opportunity");
            else                           reasons.add("Safe but low reward");
        }

        return new ScoredPath(nodes, Math.max(0, Math.min(score, 75)),
                              fights, elites, rests, shops, reasons);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Map<String, MapRoomNode> buildLookup() {
        Map<String, MapRoomNode> lookup = new HashMap<>();
        for (ArrayList<MapRoomNode> row : AbstractDungeon.map)
            for (MapRoomNode node : row)
                if (node != null) lookup.put(node.x + "," + node.y, node);
        return lookup;
    }

    private static boolean hasRelic(String id) {
        for (AbstractRelic r : AbstractDungeon.player.relics)
            if (r.relicId.equals(id)) return true;
        return false;
    }

    private static String getArchetype() {
        SynergyEngine.CharData data = SynergyEngine.getCharData();
        if (data == null) return "Balanced";
        return data.archetype(SynergyEngine.buildDeckTags(data.cardTags));
    }

    // ── Data class ────────────────────────────────────────────────────────────

    public static class ScoredPath {
        public final List<MapRoomNode> nodes;
        public final int score;
        public final int fights, elites, rests, shops;
        public final List<String> reasons;

        public ScoredPath(List<MapRoomNode> nodes, int score,
                          int fights, int elites, int rests, int shops,
                          List<String> reasons) {
            this.nodes   = nodes;
            this.score   = score;
            this.fights  = fights;
            this.elites  = elites;
            this.rests   = rests;
            this.shops   = shops;
            this.reasons = reasons;
        }

        public MapRoomNode startNode() {
            return nodes.isEmpty() ? null : nodes.get(0);
        }
    }
}

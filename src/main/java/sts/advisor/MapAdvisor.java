package sts.advisor;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.*;

import java.util.*;

/**
 * Path-based map advisor.
 * Scores full paths from current position to boss, not individual nodes.
 */
public class MapAdvisor {

    public enum Strategy { SAFE, BALANCED, AGGRESSIVE }
    public static Strategy strategy = Strategy.BALANCED;

    private static final String WINGED_BOOTS_ID = "WingedBoots";

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns scored paths from the current position.
     * Only called at intersections (2+ choices) or start of run.
     */
    public static List<ScoredPath> advise() {
        if (AbstractDungeon.map == null || AbstractDungeon.player == null) {
            return Collections.emptyList();
        }

        boolean hasWingedBoots = hasRelic(WINGED_BOOTS_ID);
        List<MapRoomNode> startNodes = getChoiceNodes(hasWingedBoots);

        // Debug
        try (java.io.FileWriter fw = new java.io.FileWriter("/tmp/sts_map_path_debug.log", false)) {
            fw.write("currMapNode: " + AbstractDungeon.currMapNode + "\n");
            fw.write("startNodes count: " + startNodes.size() + "\n");
            for (MapRoomNode n : startNodes) {
                fw.write("  start: x=" + n.x + " y=" + n.y + " room=" + n.room.getClass().getSimpleName() + "\n");
                for (MapEdge e : n.getEdges()) {
                    fw.write("    edge -> " + e.dstX + "," + e.dstY + "\n");
                }
            }
            Map<String, MapRoomNode> lookup = buildLookup();
            fw.write("lookup size: " + lookup.size() + "\n");
            // Test one path
            if (!startNodes.isEmpty()) {
                List<List<MapRoomNode>> paths = findPaths(startNodes.get(0), lookup);
                fw.write("paths from first start: " + paths.size() + "\n");
                for (List<MapRoomNode> p : paths) {
                    StringBuilder sb2 = new StringBuilder("  path: ");
                    for (MapRoomNode pn : p) sb2.append(pn.room.getClass().getSimpleName().charAt(0)).append("(").append(pn.y).append(") ");
                    fw.write(sb2.toString() + "\n");
                }
            }
        } catch (Exception e) { /* ignore */ }

        if (startNodes.size() < 2) return Collections.emptyList();

        Map<String, MapRoomNode> lookup = buildLookup();
        int act = AbstractDungeon.actNum;
        float hpPct = (float) AbstractDungeon.player.currentHealth
                    / AbstractDungeon.player.maxHealth;
        int gold = AbstractDungeon.player.gold;
        String archetype = getArchetype();

        // Score best path per start node
        List<ScoredPath> bestPerStart = new ArrayList<>();
        for (MapRoomNode start : startNodes) {
            List<List<MapRoomNode>> fullPaths = findPaths(start, lookup);
            ScoredPath best = null;
            for (List<MapRoomNode> path : fullPaths) {
                ScoredPath sp = scorePath(path, act, hpPct, gold, archetype);
                if (best == null || sp.score > best.score) best = sp;
            }
            if (best != null) bestPerStart.add(best);
        }
        bestPerStart.sort((a, b) -> b.score - a.score);

        // Deduplicate — skip start nodes whose best path is nearly identical
        // to one we already kept (same elite/rest/shop/fight profile)
        List<ScoredPath> deduped = new ArrayList<>();
        for (ScoredPath sp : bestPerStart) {
            boolean duplicate = false;
            for (ScoredPath kept : deduped) {
                if (kept.elites == sp.elites && kept.rests == sp.rests
                        && kept.shops == sp.shops && kept.fights == sp.fights
                        && Math.abs(kept.score - sp.score) <= 3) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) deduped.add(sp);
            if (deduped.size() >= 3) break;
        }

        return deduped;
    }

    /** Returns true if there's a meaningful choice to present */
    public static boolean hasChoice() {
        if (AbstractDungeon.map == null || AbstractDungeon.player == null) return false;
        boolean hasWingedBoots = hasRelic(WINGED_BOOTS_ID);
        return getChoiceNodes(hasWingedBoots).size() >= 2;
    }

    // ── Path finding ──────────────────────────────────────────────────────────

    private static List<MapRoomNode> getChoiceNodes(boolean wingedBoots) {
        List<MapRoomNode> choices = new ArrayList<>();
        MapRoomNode curr = AbstractDungeon.currMapNode;
        int nextFloor = (curr == null || curr.y == -1) ? 0 : curr.y + 1;

        if (nextFloor >= AbstractDungeon.map.size()) return choices;

        List<MapRoomNode> nextRow = AbstractDungeon.map.get(nextFloor);
        for (MapRoomNode node : nextRow) {
            if (node == null || node.room == null) continue;
            if (wingedBoots || curr == null || curr.y == -1 || curr.isConnectedTo(node)) {
                choices.add(node);
            }
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
        if (depth > 20) return; // safety cap
        List<MapRoomNode> path = new ArrayList<>(current);
        path.add(node);

        List<MapRoomNode> children = getChildren(node, lookup);
        if (children.isEmpty()) {
            results.add(path);
            return;
        }
        for (MapRoomNode child : children) {
            dfs(child, path, lookup, results, depth + 1);
        }
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

    // ── Path scoring ──────────────────────────────────────────────────────────

    private static ScoredPath scorePath(List<MapRoomNode> nodes, int act,
                                         float hpPct, int gold, String archetype) {
        int score = 20; // low baseline — paths must earn their score
        List<String> reasons = new ArrayList<>();

        int fights  = 0, elites = 0, rests = 0, shops = 0, events = 0, chests = 0;
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

        // ── Build completion ──────────────────────────────────────────────────
        // Act 1: fights matter most for deck building
        // Act 2/3: fights still give cards but deck should be more complete
        score += fights * (act == 1 ? 4 : act == 2 ? 2 : 1);

        // Shops — valuable, scaled by gold available
        score += shops * (gold >= 100 ? 8 : gold >= 50 ? 5 : 2);
        if (shops == 0 && act >= 2) { score -= 5; reasons.add("No shop available"); }

        // Events — good in act 1, neutral in act 2, risky in act 3
        score += events * (act == 1 ? 5 : act == 2 ? 3 : 1);

        // Chests — free relic, always good
        score += chests * 8;

        // ── Elites — relics vs risk ───────────────────────────────────────────
        float eliteThreshold = act == 1 ? 0.55f : act == 2 ? 0.45f : 0.40f;
        if (strategy == Strategy.SAFE)       eliteThreshold += 0.15f;
        if (strategy == Strategy.AGGRESSIVE) eliteThreshold -= 0.15f;

        if (elites == 0) {
            score -= 12;
            reasons.add("No elites — missed relic chances");
        } else if (elites == 1) {
            if (hpPct < eliteThreshold) {
                score -= 8;
                reasons.add("Elite at low HP — risky");
            } else {
                score += 12;
                reasons.add("1 elite — good relic opportunity");
            }
        } else if (elites == 2) {
            if (hpPct < eliteThreshold) {
                score -= 15;
                reasons.add("2 elites at low HP — dangerous");
            } else {
                score += 16;
                reasons.add("2 elites — strong relic gain");
            }
        } else {
            score += 10 - (elites - 2) * 12;
            reasons.add(elites + " elites — overloaded");
        }

        // Back-to-back elites with no rest between
        for (int i = 0; i < elitePos.size() - 1; i++) {
            boolean restBetween = false;
            for (int rp : restPos) {
                if (rp > elitePos.get(i) && rp < elitePos.get(i + 1)) {
                    restBetween = true; break;
                }
            }
            if (!restBetween && elitePos.get(i+1) - elitePos.get(i) <= 4) {
                score -= 15;
                reasons.add("Elites with no recovery between");
                break;
            }
        }

        // ── Rest sites — survival ─────────────────────────────────────────────
        int restValue = hpPct < 0.5f ? 12 : hpPct < 0.7f ? 7 : 3;
        restValue += (act - 1) * 2;
        score += rests * restValue;

        // ── Act-specific ──────────────────────────────────────────────────────
        if (act == 1 && fights >= 5)        { score += 6;  reasons.add("Dense fights — fast deck building"); }
        if (act == 2 && events >= 3)        { score -= 4;  }
        if (act == 3 && rests >= 2)         { score += 8;  reasons.add("Multiple rests — strong late path"); }
        if (strategy == Strategy.AGGRESSIVE){ score += elites * 5; }
        if (strategy == Strategy.SAFE)      { score += rests * 4; score -= elites * 3; }

        if (reasons.isEmpty()) {
            if (elites >= 1 && shops >= 1)  reasons.add("Balanced — elite + shop");
            else if (fights >= 4)           reasons.add("Fight-heavy — good for building");
            else                            reasons.add("Standard path");
        }

        return new ScoredPath(nodes, Math.max(0, Math.min(score, 99)),
                              fights, elites, rests, shops, reasons);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Map<String, MapRoomNode> buildLookup() {
        Map<String, MapRoomNode> lookup = new HashMap<>();
        for (ArrayList<MapRoomNode> row : AbstractDungeon.map) {
            for (MapRoomNode node : row) {
                if (node != null) lookup.put(node.x + "," + node.y, node);
            }
        }
        return lookup;
    }

    private static boolean hasRelic(String relicId) {
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            if (r.relicId.equals(relicId)) return true;
        }
        return false;
    }

    private static String getArchetype() {
        SynergyEngine.CharData data = SynergyEngine.getCharData();
        if (data == null) return "Balanced";
        Map<String, Integer> tags = SynergyEngine.buildDeckTags(data.cardTags);
        return data.archetype(tags);
    }

    public static String getRoomSymbol(MapRoomNode node) {
        if (node.room instanceof MonsterRoomElite)  return "E";
        if (node.room instanceof RestRoom)           return "R";
        if (node.room instanceof ShopRoom)           return "$";
        if (node.room instanceof EventRoom)          return "?";
        if (node.room instanceof TreasureRoom)       return "T";
        if (node.room instanceof MonsterRoomBoss)    return "B";
        return "M";
    }

    // ── Data class ────────────────────────────────────────────────────────────

    public static class ScoredPath {
        public final List<MapRoomNode> nodes;
        public final int score;
        public final int fights;
        public final int elites;
        public final int rests;
        public final int shops;
        public final List<String> reasons;
        public MapRoomNode startNode() { return nodes.isEmpty() ? null : nodes.get(0); }

        public ScoredPath(List<MapRoomNode> nodes, int score, int fights,
                          int elites, int rests, int shops, List<String> reasons) {
            this.nodes   = nodes;
            this.score   = score;
            this.fights  = fights;
            this.elites  = elites;
            this.rests   = rests;
            this.shops   = shops;
            this.reasons = reasons;
        }
    }
}

package sts.advisor;

import basemod.BaseMod;
import basemod.interfaces.PostRenderSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostCreateShopRelicSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpireInitializer
public class STSAdvisorMod implements PostInitializeSubscriber,
                                      PostRenderSubscriber,
                                      PostCreateShopRelicSubscriber {

    private static final Color COLOR_S     = new Color(1.0f,  0.84f, 0.0f,  1.0f);
    private static final Color COLOR_A     = new Color(0.78f, 0.66f, 0.43f, 1.0f);
    private static final Color COLOR_B     = new Color(0.48f, 0.67f, 0.54f, 1.0f);
    private static final Color COLOR_C     = new Color(0.48f, 0.60f, 0.67f, 1.0f);
    private static final Color COLOR_D     = new Color(0.48f, 0.48f, 0.54f, 1.0f);
    private static final Color COLOR_TAKE  = new Color(0.35f, 0.72f, 0.48f, 1.0f);
    private static final Color COLOR_CONS  = new Color(0.78f, 0.66f, 0.43f, 1.0f);
    private static final Color COLOR_SKIP  = new Color(0.72f, 0.35f, 0.35f, 1.0f);
    private static final Color COLOR_TEXT  = new Color(0.05f, 0.05f, 0.05f, 1.0f);
    private static final Color COLOR_WHITE = new Color(1.0f,  1.0f,  1.0f,  1.0f);
    private static final Color COLOR_BG    = new Color(0.0f,  0.0f,  0.0f,  0.82f);
    private static final Color COLOR_WARN  = new Color(0.9f,  0.7f,  0.1f,  1.0f);
    private static final Color COLOR_LIGHT = new Color(0.9f,  0.9f,  0.9f,  1.0f);

    // Font scale — buttonLabelFont is large so we scale it down
    private static final float FONT_SCALE_SMALL = 0.55f;
    private static final float FONT_SCALE_TINY  = 0.42f;

    public static void initialize() {
        BaseMod.subscribe(new STSAdvisorMod());
    }

    @Override
    public void receivePostInitialize() {
        System.out.println("[STSAdvisor] Initialized!");
    }

    @Override
    public void receiveCreateShopRelics(ArrayList<StoreRelic> relics, ShopScreen screen) {
        ShopAdvisor.cacheRelics(relics);
    }

    @Override
    public void receivePostRender(SpriteBatch sb) {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD) {
            renderCardReward(sb);
        } else if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP) {
            renderMap(sb);
        } else if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SHOP) {
            renderShop(sb);
        }
    }

    private void renderShop(SpriteBatch sb) {
        ShopScreen shop = AbstractDungeon.shopScreen;
        if (shop == null) return;

        float s = Settings.scale;

        // ── Score shop cards ──────────────────────────────────────────────────
        List<AbstractCard> allCards = new ArrayList<>();
        if (shop.coloredCards != null)   allCards.addAll(shop.coloredCards);
        if (shop.colorlessCards != null) allCards.addAll(shop.colorlessCards);

        for (AbstractCard card : allCards) {
            if (card.hb == null) continue;
            if (card.hb.cX < -1000 || card.hb.cY < -1000) continue;

            AdvisorResult result = CardScorer.score(card);
            renderShopCardBadge(sb, card, result, s);
        }

        // ── Score relics ──────────────────────────────────────────────────────
        for (StoreRelic sr : ShopAdvisor.getCachedRelics()) {
            if (sr.isPurchased) continue;
            if (sr.relic == null || sr.relic.hb == null) continue;
            if (sr.relic.hb.cX < -1000 || sr.relic.hb.cY < -1000) continue;

            ShopAdvisor.RelicAdvice advice = ShopAdvisor.scoreRelic(sr);
            renderShopRelicBadge(sb, sr, advice, s);
        }

        // ── Card removal recommendation ───────────────────────────────────────
        if (shop.purgeAvailable) {
            ShopAdvisor.RemovalAdvice removal = ShopAdvisor.scoreRemoval();
            renderRemovalAdvice(sb, removal, s);
        }
    }

    private void renderShopCardBadge(SpriteBatch sb, AbstractCard card,
                                      AdvisorResult result, float s) {
        float cx = card.hb.cX;
        float cy = card.hb.cY + card.hb.height / 2f + 8f * s;

        float bW = 90f * s;
        float bH = 26f * s;
        float bX = cx - bW / 2f;

        Color tierColor = getTierColor(result.tier);
        drawRect(sb, bX, cy, bW, bH, tierColor);

        // Tier letter
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            result.tier, bX + 12f * s, cy + bH / 2f + 6f * s,
            COLOR_TEXT, FONT_SCALE_SMALL);

        // Score + synergy bonus
        String scoreText = result.baseScore
            + (result.synergyBonus > 0 ? " +" + result.synergyBonus
               : result.synergyBonus < 0 ? " " + result.synergyBonus : "");
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            scoreText, bX + bW * 0.65f, cy + bH / 2f + 6f * s,
            COLOR_TEXT, FONT_SCALE_SMALL);

        // Recommendation pill
        Color recColor = getRecColor(result.recommendation);
        float rW = 70f * s, rH = 18f * s;
        float rY = cy - rH - 2f * s;
        drawRect(sb, cx - rW / 2f, rY, rW, rH, recColor);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            result.recommendation, cx, rY + rH / 2f + 5f * s,
            COLOR_TEXT, FONT_SCALE_TINY);
    }

    private void renderShopRelicBadge(SpriteBatch sb, StoreRelic sr,
                                       ShopAdvisor.RelicAdvice advice, float s) {
        float cx = sr.relic.hb.cX;
        // Place badge well above the relic icon
        float cy = sr.relic.hb.cY + sr.relic.hb.height * 0.5f + 30f * s;

        float bW = 80f * s;
        float bH = 24f * s;
        float bX = cx - bW / 2f;

        Color tierColor = getTierColor(advice.tier());
        // Dim if can't afford
        if (!advice.canAfford) tierColor = new Color(
            tierColor.r * 0.5f, tierColor.g * 0.5f, tierColor.b * 0.5f, 0.7f);

        drawRect(sb, bX, cy, bW, bH, tierColor);

        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            advice.tier() + "  " + advice.score,
            cx, cy + bH / 2f + 6f * s,
            COLOR_TEXT, FONT_SCALE_SMALL);

        // Buy/Skip pill
        Color recColor = advice.recommendation.equals("Buy") ? COLOR_TAKE
                       : advice.recommendation.equals("Consider") ? COLOR_CONS
                       : COLOR_SKIP;
        if (!advice.canAfford) recColor = new Color(0.4f, 0.4f, 0.4f, 0.8f);

        float rW = 65f * s, rH = 18f * s;
        float rY = cy - rH - 2f * s;
        drawRect(sb, cx - rW / 2f, rY, rW, rH, recColor);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            advice.canAfford ? advice.recommendation : "Can't afford",
            cx, rY + rH / 2f + 5f * s, COLOR_TEXT, FONT_SCALE_TINY);
    }

    private void renderRemovalAdvice(SpriteBatch sb, ShopAdvisor.RemovalAdvice advice,
                                      float s) {
        if (advice.reason.isEmpty()) return;

        ShopScreen shop = AbstractDungeon.shopScreen;
        if (shop == null || shop.confirmButton == null || shop.confirmButton.hb == null) return;

        // Position above the Card Removal Service confirm button
        float cx = shop.confirmButton.hb.cX;
        float cy = shop.confirmButton.hb.cY + shop.confirmButton.hb.height / 2f + 8f * s;

        float bW = 260f * s;
        float bH = 26f  * s;
        float bX = cx - bW / 2f;

        Color bg = advice.shouldRemove
            ? new Color(0.3f, 0.5f, 0.2f, 0.92f)
            : COLOR_BG;
        Color fg = advice.shouldRemove ? new Color(0.7f, 1.0f, 0.5f, 1.0f) : COLOR_LIGHT;

        drawRect(sb, bX, cy, bW, bH, bg);
        if (advice.shouldRemove) drawRectOutline(sb, bX, cy, bW, bH, COLOR_TAKE);

        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            advice.reason + "  (" + advice.cost + "g)",
            cx, cy + bH / 2f + 6f * s, fg, FONT_SCALE_TINY);
    }

    private void renderCardReward(SpriteBatch sb) {
        if (AbstractDungeon.cardRewardScreen == null) return;
        if (AbstractDungeon.cardRewardScreen.rewardGroup == null) return;
        ArrayList<AbstractCard> cards = AbstractDungeon.cardRewardScreen.rewardGroup;
        if (cards.isEmpty()) return;
        List<AdvisorResult> results = CardScorer.scoreAll(cards);
        for (int i = 0; i < cards.size(); i++) {
            renderCardBadge(sb, cards.get(i), results.get(i));
        }
        if (AdvisorResult.allSkip(results)) {
            renderSkipAllBanner(sb, cards);
        }
    }

    // ShapeRenderer for drawing path lines
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    private com.badlogic.gdx.graphics.glutils.ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        }
        return shapeRenderer;
    }

    private static final Color PATH_GOLD   = new Color(1.0f,  0.80f, 0.0f,  0.85f);
    private static final Color PATH_SILVER = new Color(0.70f, 0.70f, 0.75f, 0.65f);
    private static final float PATH_WIDTH  = 6f;

    private void renderMap(SpriteBatch sb) {
        if (!MapAdvisor.hasChoice()) return;
        List<MapAdvisor.ScoredPath> paths = MapAdvisor.advise();
        if (paths.isEmpty()) return;

        Map<String, com.megacrit.cardcrawl.map.MapRoomNode> lookup = buildMapLookup();

        // End SpriteBatch — ShapeRenderer needs GL state
        sb.end();

        com.badlogic.gdx.graphics.glutils.ShapeRenderer sr = getShapeRenderer();
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(
            com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
            com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(sb.getProjectionMatrix());
        sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        // Draw silver (second best) first so gold draws on top
        if (paths.size() >= 2) {
            drawPathLines(sr, paths.get(1), PATH_SILVER, PATH_WIDTH, lookup);
        }
        drawPathLines(sr, paths.get(0), PATH_GOLD, PATH_WIDTH + 2f, lookup);

        sr.end();

        // Resume SpriteBatch
        sb.begin();

        // Draw reason text below the start node of the best path
        MapAdvisor.ScoredPath best = paths.get(0);
        com.megacrit.cardcrawl.map.MapRoomNode startNode = best.startNode();
        if (startNode != null && startNode.hb != null
                && startNode.hb.cX > -1000 && startNode.hb.cY > -1000
                && !best.reasons.isEmpty()) {
            float s  = Settings.scale;
            float cx = startNode.hb.cX;
            float cy = startNode.hb.cY;
            String reason = truncate(best.reasons.get(0), 32);
            float rW = 200f * s;
            float rH = 20f  * s;
            float rY = cy - 36f * s;
            drawRect(sb, cx - rW / 2f, rY, rW, rH, COLOR_BG);
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                reason, cx, rY + rH / 2f + 6f * s, PATH_GOLD, FONT_SCALE_TINY);
        }
    }

    private void drawPathLines(
            com.badlogic.gdx.graphics.glutils.ShapeRenderer sr,
            MapAdvisor.ScoredPath path,
            Color color, float width,
            Map<String, com.megacrit.cardcrawl.map.MapRoomNode> lookup) {

        sr.setColor(color);
        List<com.megacrit.cardcrawl.map.MapRoomNode> nodes = path.nodes;

        for (int i = 0; i < nodes.size() - 1; i++) {
            com.megacrit.cardcrawl.map.MapRoomNode from = nodes.get(i);
            com.megacrit.cardcrawl.map.MapRoomNode to   = nodes.get(i + 1);

            if (from.hb == null || to.hb == null) continue;
            if (from.hb.cX < -1000 || to.hb.cX < -1000) continue;

            float x1 = from.hb.cX, y1 = from.hb.cY;
            float x2 = to.hb.cX,   y2 = to.hb.cY;

            // Draw thick line as a rectangle rotated along the edge direction
            float dx = x2 - x1, dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 1f) continue;
            float nx = -dy / len * width / 2f;
            float ny =  dx / len * width / 2f;

            sr.triangle(
                x1 + nx, y1 + ny,
                x1 - nx, y1 - ny,
                x2 + nx, y2 + ny);
            sr.triangle(
                x1 - nx, y1 - ny,
                x2 - nx, y2 - ny,
                x2 + nx, y2 + ny);
        }
    }

    private Map<String, com.megacrit.cardcrawl.map.MapRoomNode> buildMapLookup() {
        Map<String, com.megacrit.cardcrawl.map.MapRoomNode> lookup = new HashMap<>();
        if (AbstractDungeon.map == null) return lookup;
        for (java.util.ArrayList<com.megacrit.cardcrawl.map.MapRoomNode> row : AbstractDungeon.map)
            for (com.megacrit.cardcrawl.map.MapRoomNode node : row)
                if (node != null) lookup.put(node.x + "," + node.y, node);
        return lookup;
    }

    private void renderCardBadge(SpriteBatch sb, AbstractCard card, AdvisorResult result) {
        float s = Settings.scale;

        // Layout constants
        float badgeW   = 110f * s;
        float badgeH   = 30f  * s;
        float recW     = 80f  * s;
        float recH     = 20f  * s;
        float reasonLineH = 18f * s;

        float cx = card.current_x;
        // Place badge above the card — cards are ~430 units tall, hovered cards expand
        // Use a fixed offset above the card centre
        float badgeY = card.current_y + 260f * s;
        float badgeX = cx - badgeW / 2f;

        // ── Tier + score badge ────────────────────────────────────────────────
        Color tierColor = getTierColor(result.tier);
        drawRect(sb, badgeX, badgeY, badgeW, badgeH, tierColor);

        // Tier letter (left side of badge)
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            result.tier,
            badgeX + 14f * s,
            badgeY + badgeH / 2f + 6f * s,
            COLOR_TEXT,
            FONT_SCALE_SMALL);

        // Score — strip any color codes before rendering
        String scoreText = result.baseScore
            + (result.synergyBonus > 0 ? " +" + result.synergyBonus
               : result.synergyBonus < 0 ? " " + result.synergyBonus : "");
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            scoreText,
            badgeX + badgeW * 0.62f,
            badgeY + badgeH / 2f + 6f * s,
            COLOR_TEXT,
            FONT_SCALE_SMALL);

        // ── Recommendation pill ───────────────────────────────────────────────
        float recX = cx - recW / 2f;
        float recY = badgeY - recH - 3f * s;
        drawRect(sb, recX, recY, recW, recH, getRecColor(result.recommendation));
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            result.recommendation,
            cx,
            recY + recH / 2f + 5f * s,
            COLOR_TEXT,
            FONT_SCALE_TINY);

        // ── Synergy reasons ───────────────────────────────────────────────────
        int maxReasons = Math.min(result.reasons.size(), 3);
        if (maxReasons > 0) {
            float reasonW = 185f * s;
            float lineH   = reasonLineH;
            float padding = 5f * s;

            // Calculate positions top-down from below the rec pill
            float startY = recY - padding;

            // Draw background first — spans all reason lines
            float bgH = maxReasons * lineH + padding * 2f;
            float bgY = startY - bgH;
            drawRect(sb, cx - reasonW / 2f, bgY, reasonW, bgH, COLOR_BG);

            // Draw each reason — STS renderFontCentered draws text ABOVE the y coord
            for (int i = 0; i < maxReasons; i++) {
                String reason = truncate(result.reasons.get(i), 28);
                float textY = startY - padding - i * lineH;
                FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                    reason, cx, textY, COLOR_LIGHT, FONT_SCALE_TINY);
            }
        }
    }

    private void renderSkipAllBanner(SpriteBatch sb, ArrayList<AbstractCard> cards) {
        float s = Settings.scale;
        float centerX = Settings.WIDTH / 2f;
        float topY = 0f;
        for (AbstractCard c : cards) topY = Math.max(topY, c.current_y);

        float bW = 500f * s;
        float bH = 38f  * s;
        float bX = centerX - bW / 2f;
        float bY = topY + 290f * s;

        drawRect(sb, bX, bY, bW, bH, new Color(0.4f, 0.25f, 0.0f, 0.92f));
        drawRectOutline(sb, bX, bY, bW, bH, COLOR_WARN);

        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            "CONSIDER SKIPPING ALL  -  none of these fit your deck",
            centerX,
            bY + bH / 2f + 7f * s,
            COLOR_WARN,
            FONT_SCALE_SMALL);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawRect(SpriteBatch sb, float x, float y, float w, float h, Color c) {
        sb.setColor(c);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, w, h);
        sb.setColor(COLOR_WHITE);
    }

    private void drawRectOutline(SpriteBatch sb, float x, float y, float w, float h, Color c) {
        float t = 2f * Settings.scale;
        drawRect(sb, x,         y,         w, t, c);
        drawRect(sb, x,         y + h - t, w, t, c);
        drawRect(sb, x,         y,         t, h, c);
        drawRect(sb, x + w - t, y,         t, h, c);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    private Color getTierColor(String tier) {
        switch (tier) {
            case "S": return COLOR_S;
            case "A": return COLOR_A;
            case "B": return COLOR_B;
            case "C": return COLOR_C;
            default:  return COLOR_D;
        }
    }

    private Color getRecColor(String rec) {
        switch (rec) {
            case "TAKE":     return COLOR_TAKE;
            case "CONSIDER": return COLOR_CONS;
            default:         return COLOR_SKIP;
        }
    }
}

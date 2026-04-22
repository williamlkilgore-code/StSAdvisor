package sts.advisor;

import basemod.BaseMod;
import basemod.interfaces.PostRenderSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import java.util.ArrayList;
import java.util.List;

@SpireInitializer
public class STSAdvisorMod implements PostInitializeSubscriber, PostRenderSubscriber {

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
    public void receivePostRender(SpriteBatch sb) {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD) {
            renderCardReward(sb);
        } else if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP) {
            renderMap(sb);
        }
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

    private void renderMap(SpriteBatch sb) {
        if (!MapAdvisor.hasChoice()) return;
        List<MapAdvisor.ScoredPath> paths = MapAdvisor.advise();
        if (paths.isEmpty()) return;

        float s = Settings.scale;

        for (int i = 0; i < Math.min(paths.size(), 3); i++) {
            MapAdvisor.ScoredPath path = paths.get(i);
            MapRoomNode start = path.startNode();
            if (start == null || start.hb == null) continue;
            if (start.hb.cX < -1000 || start.hb.cY < -1000) continue;
            renderPathBadge(sb, path, i, paths.size());
        }
    }

    private void renderPathBadge(SpriteBatch sb, MapAdvisor.ScoredPath path,
                                  int rank, int total) {
        MapRoomNode start = path.startNode();
        float s  = Settings.scale;
        float cx = start.hb.cX;
        float cy = start.hb.cY;

        boolean best = (rank == 0);

        // Badge dimensions — wider for path summary
        float bW = 90f * s;
        float bH = 28f * s;
        float bX = cx - bW / 2f;
        float bY = cy + 30f * s;

        // Color: gold = best, silver = second, muted = third
        Color bgColor;
        Color textColor;
        if (rank == 0) {
            bgColor   = new Color(0.55f, 0.40f, 0.0f, 0.95f);
            textColor = COLOR_S;
        } else if (rank == 1) {
            bgColor   = new Color(0.18f, 0.18f, 0.22f, 0.92f);
            textColor = new Color(0.75f, 0.75f, 0.80f, 1.0f);
        } else {
            bgColor   = new Color(0.12f, 0.12f, 0.14f, 0.88f);
            textColor = new Color(0.55f, 0.55f, 0.60f, 1.0f);
        }

        drawRect(sb, bX, bY, bW, bH, bgColor);
        if (best) drawRectOutline(sb, bX, bY, bW, bH, COLOR_S);

        // Score
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            String.valueOf(path.score),
            cx, bY + bH / 2f + 7f * s,
            textColor, FONT_SCALE_SMALL);

        // Path stats row: E:1 R:1 $:1 below score
        String stats = "E:" + path.elites + " R:" + path.rests + " $:" + path.shops;
        float statsY = bY - 18f * s;
        float statsW = bW + 10f * s;
        drawRect(sb, cx - statsW / 2f, statsY, statsW, 16f * s,
                 new Color(0.0f, 0.0f, 0.0f, 0.75f));
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
            stats, cx, statsY + 12f * s,
            textColor, FONT_SCALE_TINY);

        // Top reason for best path
        if (best && !path.reasons.isEmpty()) {
            float rW = 180f * s;
            float rH = 18f * s;
            float rY = statsY - rH - 2f * s;
            drawRect(sb, cx - rW / 2f, rY, rW, rH, COLOR_BG);
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                truncate(path.reasons.get(0), 26),
                cx, rY + rH / 2f + 5f * s,
                COLOR_LIGHT, FONT_SCALE_TINY);
        }
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

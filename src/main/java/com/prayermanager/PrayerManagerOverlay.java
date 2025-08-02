package com.prayermanager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class PrayerManagerOverlay extends OverlayPanel
{
    private static final int PRAYER_ICON_SIZE = 24;
    private static final int GRID_COLUMNS = 6;
    private static final Color SELECTED_COLOR = new Color(0, 255, 0, 80);
    private static final Color UNSELECTED_COLOR = new Color(64, 64, 64, 80);

    @Inject
    private Client client;

    @Inject
    private PrayerManagerConfig config;

    private final Map<Prayer, Rectangle> prayerBounds = new HashMap<>();

    public PrayerManagerOverlay()
    {
        setPosition(OverlayPosition.TOP_LEFT);
        setDragTargetable(true);
        setResizable(true);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return null;
        }

        renderPrayerPointsBar(graphics);

        return super.render(graphics);
    }

    private void renderPrayerPointsBar(Graphics2D graphics)
    {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);

        if (maxPrayer == 0) return;

        int barWidth = 200;
        int barHeight = 20;
        double percentage = (double) currentPrayer / maxPrayer;

        // Draw background
        graphics.setColor(new Color(64, 64, 64));
        graphics.fillRect(0, 0, barWidth, barHeight);

        // Draw prayer bar
        int fillWidth = (int) (barWidth * percentage);
        Color barColor = percentage > 0.5 ? Color.GREEN :
                percentage > 0.25 ? Color.YELLOW : Color.RED;
        graphics.setColor(barColor);
        graphics.fillRect(0, 0, fillWidth, barHeight);

        // Draw border
        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, 0, barWidth - 1, barHeight - 1);

        // Draw text
        String prayerText = currentPrayer + " / " + maxPrayer;
        FontMetrics fm = graphics.getFontMetrics();
        int textX = (barWidth - fm.stringWidth(prayerText)) / 2;
        int textY = (barHeight + fm.getAscent()) / 2;

        graphics.setColor(Color.BLACK);
        graphics.drawString(prayerText, textX + 1, textY + 1);
        graphics.setColor(Color.WHITE);
        graphics.drawString(prayerText, textX, textY);
    }

    public void updatePrayerStates()
    {
        // Called when prayer states change
    }
}
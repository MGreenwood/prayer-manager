package com.prayermanager;

import java.awt.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class PrayerTimingOverlay extends Overlay
{
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final Color TICK_COLOR = new Color(255, 255, 0, 150);
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 100);

    @Inject
    private Client client;

    @Inject
    private PrayerManagerConfig config;

    private long lastTickTime = 0;

    public PrayerTimingOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !config.showTimingBar())
        {
            return null;
        }

        // Try to position near the prayer orb
        net.runelite.api.widgets.Widget prayerOrb = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if (prayerOrb != null)
        {
            // Position the timing bar near the prayer orb
            int orbX = prayerOrb.getCanvasLocation().getX();
            int orbY = prayerOrb.getCanvasLocation().getY();
            
            // Offset the timing bar below the prayer orb
            graphics.translate(orbX - BAR_WIDTH / 2, orbY + 30);
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastTick = currentTime - lastTickTime;

        double tickProgress = (timeSinceLastTick % 600) / 600.0;

        renderTimingBar(graphics, tickProgress);

        return new Dimension(BAR_WIDTH, BAR_HEIGHT + 30);
    }

    private void renderTimingBar(Graphics2D graphics, double progress)
    {
        // Draw background
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, BAR_WIDTH, BAR_HEIGHT);

        // Draw tick progress
        int progressWidth = (int) (BAR_WIDTH * progress);
        graphics.setColor(TICK_COLOR);
        graphics.fillRect(0, 0, progressWidth, BAR_HEIGHT);

        // Draw border
        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, 0, BAR_WIDTH - 1, BAR_HEIGHT - 1);

        // Add simple text
        graphics.setColor(Color.WHITE);
        graphics.drawString("Tick Progress", 5, BAR_HEIGHT + 15);
    }

    public void onGameTick()
    {
        lastTickTime = System.currentTimeMillis();
    }
}
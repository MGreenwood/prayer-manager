package com.prayermanager;

import com.google.inject.Provides;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import java.util.logging.Logger;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Prayer Manager",
    description = "Enhanced prayer management with grid UI and timing visualizer",
    tags = {"prayer", "overlay", "ui", "flicking"}
)
public class PrayerManager extends Plugin
{
    private static final Logger log = Logger.getLogger(PrayerManager.class.getName());
    
    @Inject
    private Client client;
    
    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private MouseManager mouseManager;
    
    @Inject
    private PrayerManagerOverlay prayerManagerOverlay;
    
    @Inject
    private PrayerTimingOverlay prayerTimingOverlay;
    
    @Inject
    private PrayerManagerConfig config;

    private final MouseAdapter mouseAdapter = new MouseAdapter()
    {
        @Override
        public MouseEvent mouseClicked(MouseEvent mouseEvent)
        {
            // Let the overlay handle the click and consume it if necessary
            MouseEvent result = prayerManagerOverlay.handleMouseClick(mouseEvent);
            if (result == null)
            {
                // Event was consumed by overlay
                return null;
            }
            
            // Fallback to old handling if overlay didn't consume the event
            if (mouseEvent.getButton() == MouseEvent.BUTTON1) // Left click
            {
                handleLeftClick(mouseEvent);
            }
            return mouseEvent;
        }

        @Override
        public MouseEvent mousePressed(MouseEvent mouseEvent)
        {
            // Let the overlay handle the press and consume it if necessary
            return prayerManagerOverlay.handleMousePress(mouseEvent);
        }

        @Override
        public MouseEvent mouseReleased(MouseEvent mouseEvent)
        {
            // Let the overlay handle the release and consume it if necessary
            return prayerManagerOverlay.handleMouseRelease(mouseEvent);
        }

        @Override
        public MouseEvent mouseMoved(MouseEvent mouseEvent)
        {
            // Let the overlay handle mouse movement for hover effects
            return prayerManagerOverlay.handleMouseMove(mouseEvent);
        }
    };
    
    @Override
    protected void startUp()
    {
        log.info("Prayer Manager Plugin started!");
        overlayManager.add(prayerManagerOverlay);
        overlayManager.add(prayerTimingOverlay);
        mouseManager.registerMouseListener(mouseAdapter);
    }
    
    @Override
    protected void shutDown()
    {
        log.info("Prayer Manager Plugin stopped!");
        overlayManager.remove(prayerManagerOverlay);
        overlayManager.remove(prayerTimingOverlay);
        mouseManager.unregisterMouseListener(mouseAdapter);
    }
    
    private void handleLeftClick(MouseEvent mouseEvent)
    {
        // Check if quick prayer button was clicked
        if (prayerManagerOverlay.isQuickPrayerButtonClicked(mouseEvent.getPoint()))
        {
            toggleQuickPrayer();
            return;
        }

        // Check if a prayer in the grid was clicked
        Prayer clickedPrayer = prayerManagerOverlay.getPrayerAtPoint(mouseEvent.getPoint());
        if (clickedPrayer != null)
        {
            toggleQuickPrayerSelection(clickedPrayer);
        }
    }

    private void toggleQuickPrayer()
    {
        // This method is now handled by the overlay directly
        // Keeping for backward compatibility but functionality moved to overlay
        log.info("Quick Prayer button clicked - handled by overlay");
    }

    private void toggleQuickPrayerSelection(Prayer prayer)
    {
        // This method is now handled by the overlay directly
        // Keeping for backward compatibility but functionality moved to overlay
        log.info("Prayer clicked: " + prayer.name() + " - handled by overlay");
    }

    private Widget getPrayerWidget(Prayer prayer)
    {
        // Prayer widget mapping is complex and varies by RuneLite version
        // For now, return null and handle prayer selection differently
        return null;
    }
    
    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        // Update timing bar position for 1-tick flick visualization
        prayerTimingOverlay.onGameTick();
    }
    
    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged)
    {
        // Update prayer selection states when quick prayers change
        if (isQuickPrayerVarbit(varbitChanged.getVarbitId()))
        {
            prayerManagerOverlay.updatePrayerStates();
        }
    }
    
    private boolean isQuickPrayerVarbit(int varbitId)
    {
        // Check if the changed varbit relates to quick prayer selections
        return varbitId >= 4102 && varbitId <= 4126; // Quick prayer varbits range
    }
    
    @Provides
    PrayerManagerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PrayerManagerConfig.class);
    }
}
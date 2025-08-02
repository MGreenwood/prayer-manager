package com.prayermanager;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "Prayer Manager",
    description = "Enhanced prayer management with grid UI and timing visualizer",
    tags = {"prayer", "overlay", "ui", "flicking"}
)
public class PrayerManager extends Plugin
{
    @Inject
    private Client client;
    
    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private PrayerManagerOverlay prayerManagerOverlay;
    
    @Inject
    private PrayerTimingOverlay prayerTimingOverlay;
    
    @Inject
    private PrayerManagerConfig config;
    
    @Override
    protected void startUp()
    {
        log.info("Prayer Grid Plugin started!");
        overlayManager.add(prayerManagerOverlay);
        overlayManager.add(prayerTimingOverlay);
    }
    
    @Override
    protected void shutDown()
    {
        log.info("Prayer Grid Plugin stopped!");
        overlayManager.remove(prayerManagerOverlay);
        overlayManager.remove(prayerTimingOverlay);
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
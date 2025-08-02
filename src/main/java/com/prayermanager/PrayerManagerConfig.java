package com.prayermanager;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("com/prayermanager")
public interface PrayerManagerConfig extends Config
{
    @ConfigItem(
        keyName = "showTimingBar",
        name = "Show Timing Bar",
        description = "Display the 1-tick flick timing visualization"
    )
    default boolean showTimingBar()
    {
        return true;
    }
    
    @ConfigItem(
        keyName = "gridColumns",
        name = "Grid Columns",
        description = "Number of columns in the prayer grid"
    )
    @Range(min = 3, max = 8)
    default int gridColumns()
    {
        return 6;
    }
    
    @ConfigItem(
        keyName = "showPrayerPoints",
        name = "Show Prayer Points Bar",
        description = "Display current prayer points as a bar"
    )
    default boolean showPrayerPoints()
    {
        return true;
    }
}
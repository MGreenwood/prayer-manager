package com.prayermanager;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("prayermanager")
public interface PrayerManagerConfig extends Config
{
    @ConfigItem(
        keyName = "showPrayerGrid",
        name = "Show Prayer Grid",
        description = "Display the quick prayer selection grid"
    )
    default boolean showPrayerGrid()
    {
        return true;
    }
    
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
        keyName = "showPrayerPoints",
        name = "Show Prayer Points Bar",
        description = "Display current prayer points as a bar"
    )
    default boolean showPrayerPoints()
    {
        return true;
    }
    
    @ConfigItem(
        keyName = "showQuickPrayerButton",
        name = "Show Quick Prayer Button",
        description = "Display the quick prayer toggle button"
    )
    default boolean showQuickPrayerButton()
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
    
    enum ButtonSize
    {
        SMALL(24),
        MEDIUM(32),
        LARGE(40);
        
        private final int size;
        
        ButtonSize(int size)
        {
            this.size = size;
        }
        
        public int getSize()
        {
            return size;
        }
    }
    
    @ConfigItem(
        keyName = "quickPrayerButtonSize",
        name = "Quick Prayer Button Size",
        description = "Size of the quick prayer button"
    )
    default ButtonSize quickPrayerButtonSize()
    {
        return ButtonSize.MEDIUM;
    }
    
    @ConfigItem(
        keyName = "quickPrayerButtonColor",
        name = "Quick Prayer Button Color",
        description = "Background color of the quick prayer button"
    )
    default Color quickPrayerButtonColor()
    {
        return Color.BLUE;
    }
}
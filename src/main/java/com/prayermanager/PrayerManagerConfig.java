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
    
    @ConfigItem(
        keyName = "prayerGridPositionX",
        name = "",
        description = "",
        hidden = true
    )
    default int prayerGridPositionX()
    {
        return -1; // -1 indicates no saved position
    }
    
    @ConfigItem(
        keyName = "prayerGridPositionY",
        name = "",
        description = "",
        hidden = true
    )
    default int prayerGridPositionY()
    {
        return -1; // -1 indicates no saved position
    }
    
    @ConfigItem(
        keyName = "timingOverlayPositionX",
        name = "",
        description = "",
        hidden = true
    )
    default int timingOverlayPositionX()
    {
        return -1; // -1 indicates no saved position
    }
    
    @ConfigItem(
        keyName = "timingOverlayPositionY",
        name = "",
        description = "",
        hidden = true
    )
    default int timingOverlayPositionY()
    {
        return -1; // -1 indicates no saved position
    }
    
    // UI Polish and Visual Feedback Options
    
    @ConfigItem(
        keyName = "enableHoverEffects",
        name = "Enable Hover Effects",
        description = "Show visual feedback when hovering over interactive elements"
    )
    default boolean enableHoverEffects()
    {
        return true;
    }
    
    @ConfigItem(
        keyName = "enableClickFeedback",
        name = "Enable Click Feedback",
        description = "Show visual feedback when clicking on elements"
    )
    default boolean enableClickFeedback()
    {
        return true;
    }
    
    @ConfigItem(
        keyName = "enableSmoothTransitions",
        name = "Enable Smooth Transitions",
        description = "Use smooth color transitions for state changes"
    )
    default boolean enableSmoothTransitions()
    {
        return true;
    }
    
    @ConfigItem(
        keyName = "overlayOpacity",
        name = "Overlay Opacity",
        description = "Overall opacity of the prayer overlay (0-100%)"
    )
    @Range(min = 10, max = 100)
    default int overlayOpacity()
    {
        return 80;
    }
    
    @ConfigItem(
        keyName = "selectedPrayerColor",
        name = "Selected Prayer Color",
        description = "Color for selected prayers in the grid"
    )
    default Color selectedPrayerColor()
    {
        return new Color(0, 255, 0, 120);
    }
    
    @ConfigItem(
        keyName = "unselectedPrayerColor",
        name = "Unselected Prayer Color", 
        description = "Color for unselected prayers in the grid"
    )
    default Color unselectedPrayerColor()
    {
        return new Color(64, 64, 64, 120);
    }
    
    @ConfigItem(
        keyName = "hoverColor",
        name = "Hover Color",
        description = "Color when hovering over prayers"
    )
    default Color hoverColor()
    {
        return new Color(128, 128, 128, 120);
    }
    
    @ConfigItem(
        keyName = "textColor",
        name = "Text Color",
        description = "Color for prayer names and text"
    )
    default Color textColor()
    {
        return Color.WHITE;
    }
    
    @ConfigItem(
        keyName = "borderColor",
        name = "Border Color",
        description = "Color for element borders"
    )
    default Color borderColor()
    {
        return Color.WHITE;
    }
    
    @ConfigItem(
        keyName = "activeBorderColor",
        name = "Active Border Color",
        description = "Color for active element borders (e.g., quick prayer button when active)"
    )
    default Color activeBorderColor()
    {
        return new Color(255, 255, 0, 255);
    }
    
    @ConfigItem(
        keyName = "prayerPointsBarColor",
        name = "Prayer Points Bar Color",
        description = "Color scheme for the prayer points bar"
    )
    default PrayerBarColorScheme prayerPointsBarColor()
    {
        return PrayerBarColorScheme.CLASSIC;
    }
    
    enum PrayerBarColorScheme
    {
        CLASSIC("Classic", new Color[]{Color.RED, Color.YELLOW, Color.GREEN}),
        BLUE("Blue Tones", new Color[]{new Color(139, 0, 0), new Color(0, 0, 139), new Color(0, 100, 0)}),
        PURPLE("Purple Tones", new Color[]{new Color(128, 0, 128), new Color(147, 112, 219), new Color(138, 43, 226)}),
        MONOCHROME("Monochrome", new Color[]{new Color(64, 64, 64), new Color(128, 128, 128), new Color(192, 192, 192)});
        
        private final String displayName;
        private final Color[] colors; // [low, medium, high]
        
        PrayerBarColorScheme(String displayName, Color[] colors)
        {
            this.displayName = displayName;
            this.colors = colors;
        }
        
        public String getDisplayName()
        {
            return displayName;
        }
        
        public Color getColorForPercentage(double percentage)
        {
            if (percentage <= 0.25) return colors[0]; // Low - red/dark
            if (percentage <= 0.5) return colors[1];  // Medium - yellow/medium
            return colors[2]; // High - green/light
        }
        
        @Override
        public String toString()
        {
            return displayName;
        }
    }
    
    @ConfigItem(
        keyName = "errorHandlingMode",
        name = "Error Handling Mode",
        description = "How to handle API errors and edge cases"
    )
    default ErrorHandlingMode errorHandlingMode()
    {
        return ErrorHandlingMode.GRACEFUL;
    }
    
    enum ErrorHandlingMode
    {
        STRICT("Strict - Show all errors"),
        GRACEFUL("Graceful - Hide minor errors"),
        SILENT("Silent - Hide all errors");
        
        private final String displayName;
        
        ErrorHandlingMode(String displayName)
        {
            this.displayName = displayName;
        }
        
        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
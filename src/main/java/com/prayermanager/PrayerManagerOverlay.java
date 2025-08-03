package com.prayermanager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.callback.ClientThread;


@Singleton
public class PrayerManagerOverlay extends OverlayPanel
{
    private static final int PRAYER_TILE_WIDTH = 60;
    private static final int PRAYER_TILE_HEIGHT = 24;
    private static final int GRID_PADDING = 2;
    private static final int COMPONENT_SPACING = 5;
    private static final Color SELECTED_COLOR = new Color(0, 255, 0, 120);
    private static final Color UNSELECTED_COLOR = new Color(64, 64, 64, 120);
    private static final Color HOVER_COLOR = new Color(128, 128, 128, 120);
    private static final Color ACTIVE_BORDER_COLOR = new Color(255, 255, 0, 255);
    private static final Color HOVER_BRIGHTNESS_MODIFIER = new Color(25, 25, 25, 0);

    @Inject
    private Client client;

    @Inject
    private PrayerManagerConfig config;
    
    @Inject
    private ClientThread clientThread;



    private final Map<Prayer, Rectangle> prayerBounds = new HashMap<>();
    private Rectangle quickPrayerButtonBounds;
    private Prayer hoveredPrayer = null;
    private Prayer clickedPrayer = null;
    private long clickFeedbackTime = 0;
    private static final long CLICK_FEEDBACK_DURATION = 200; // 200ms click feedback
    private boolean isQuickPrayerButtonHovered = false;
    private boolean isQuickPrayerButtonClicked = false;
    private long buttonClickFeedbackTime = 0;

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

        int yOffset = 0;

        // Render prayer points bar
        if (config.showPrayerPoints())
        {
            yOffset += renderPrayerPointsBar(graphics, yOffset) + COMPONENT_SPACING;
        }

        // Render quick prayer button
        if (config.showQuickPrayerButton())
        {
            yOffset += renderQuickPrayerButton(graphics, yOffset) + COMPONENT_SPACING;
        }

        // Render prayer grid
        if (config.showPrayerGrid())
        {
            yOffset += renderPrayerGrid(graphics, yOffset);
        }

        return new Dimension(getMaxWidth(), yOffset);
    }

    private int renderPrayerPointsBar(Graphics2D graphics, int yOffset)
    {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);

        if (maxPrayer == 0) return 0;

        int barWidth = 200;
        int barHeight = 20;
        double percentage = (double) currentPrayer / maxPrayer;

        // Draw background
        graphics.setColor(new Color(64, 64, 64));
        graphics.fillRect(0, yOffset, barWidth, barHeight);

        // Draw prayer bar with gradient
        int fillWidth = (int) (barWidth * percentage);
        Color barColor = percentage > 0.5 ? Color.GREEN :
                percentage > 0.25 ? Color.YELLOW : Color.RED;
        graphics.setColor(barColor);
        graphics.fillRect(0, yOffset, fillWidth, barHeight);

        // Draw border
        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, yOffset, barWidth - 1, barHeight - 1);

        // Draw text
        String prayerText = currentPrayer + " / " + maxPrayer;
        FontMetrics fm = graphics.getFontMetrics();
        int textX = (barWidth - fm.stringWidth(prayerText)) / 2;
        int textY = yOffset + (barHeight + fm.getAscent()) / 2;

        graphics.setColor(Color.BLACK);
        graphics.drawString(prayerText, textX + 1, textY + 1);
        graphics.setColor(Color.WHITE);
        graphics.drawString(prayerText, textX, textY);

        return barHeight;
    }

    private int renderQuickPrayerButton(Graphics2D graphics, int yOffset)
    {
        int buttonSize = config.quickPrayerButtonSize().getSize();
        boolean quickPrayerActive = client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
        Color baseColor = config.quickPrayerButtonColor();

        quickPrayerButtonBounds = new Rectangle(0, yOffset, buttonSize, buttonSize);

        // Determine button state and colors
        Color backgroundColor = getButtonBackgroundColor(baseColor, quickPrayerActive);
        
        // Apply hover effect if button is hovered
        if (isQuickPrayerButtonHovered)
        {
            backgroundColor = brightenColor(backgroundColor, 0.1f);
        }
        
        // Apply click feedback effect
        boolean isClickFeedbackActive = isQuickPrayerButtonClicked && 
                                      (System.currentTimeMillis() - buttonClickFeedbackTime) < CLICK_FEEDBACK_DURATION;
        if (isClickFeedbackActive)
        {
            // Scale animation effect - draw slightly smaller then back to normal
            long elapsed = System.currentTimeMillis() - buttonClickFeedbackTime;
            float scale = elapsed < CLICK_FEEDBACK_DURATION / 2 ? 0.95f : 1.0f;
            int scaledSize = (int) (buttonSize * scale);
            int offset = (buttonSize - scaledSize) / 2;
            
            graphics.setColor(backgroundColor);
            graphics.fillRect(offset, yOffset + offset, scaledSize, scaledSize);
            
            // Draw border for scaled button
            graphics.setColor(quickPrayerActive ? ACTIVE_BORDER_COLOR : Color.WHITE);
            graphics.drawRect(offset, yOffset + offset, scaledSize - 1, scaledSize - 1);
        }
        else
        {
            // Draw normal button background
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, yOffset, buttonSize, buttonSize);

            // Draw border - yellow for active, white for inactive
            graphics.setColor(quickPrayerActive ? ACTIVE_BORDER_COLOR : Color.WHITE);
            graphics.drawRect(0, yOffset, buttonSize - 1, buttonSize - 1);
        }

        // Draw "QP" text
        graphics.setColor(Color.WHITE);
        FontMetrics fm = graphics.getFontMetrics();
        String text = "QP";
        int textX = (buttonSize - fm.stringWidth(text)) / 2;
        int textY = yOffset + (buttonSize + fm.getAscent()) / 2;
        
        // Add text shadow for better readability
        graphics.setColor(Color.BLACK);
        graphics.drawString(text, textX + 1, textY + 1);
        graphics.setColor(Color.WHITE);
        graphics.drawString(text, textX, textY);

        return buttonSize;
    }
    
    private Color getButtonBackgroundColor(Color baseColor, boolean isActive)
    {
        if (isActive)
        {
            // Active: full opacity
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 255);
        }
        else
        {
            // Inactive: 50% opacity
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 127);
        }
    }
    
    private Color brightenColor(Color color, float factor)
    {
        int r = Math.min(255, (int) (color.getRed() * (1 + factor)));
        int g = Math.min(255, (int) (color.getGreen() * (1 + factor)));
        int b = Math.min(255, (int) (color.getBlue() * (1 + factor)));
        return new Color(r, g, b, color.getAlpha());
    }

    private int renderPrayerGrid(Graphics2D graphics, int yOffset)
    {
        Prayer[] prayers = Prayer.values();
        int columns = config.gridColumns();
        int rows = (prayers.length + columns - 1) / columns;

        synchronized (prayerBounds)
        {
            prayerBounds.clear();
        }

        int gridWidth = columns * (PRAYER_TILE_WIDTH + GRID_PADDING) - GRID_PADDING;
        int gridHeight = rows * (PRAYER_TILE_HEIGHT + GRID_PADDING) - GRID_PADDING;

        // Draw grid background
        graphics.setColor(new Color(0, 0, 0, 100));
        graphics.fillRect(0, yOffset, gridWidth, gridHeight);

        // Set up font for prayer names - use a readable size
        Font originalFont = graphics.getFont();
        Font prayerFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        graphics.setFont(prayerFont);



        for (int i = 0; i < prayers.length; i++)
        {
            Prayer prayer = prayers[i];
            int row = i / columns;
            int col = i % columns;

            int x = col * (PRAYER_TILE_WIDTH + GRID_PADDING);
            int y = yOffset + row * (PRAYER_TILE_HEIGHT + GRID_PADDING);

            Rectangle bounds = new Rectangle(x, y, PRAYER_TILE_WIDTH, PRAYER_TILE_HEIGHT);
            synchronized (prayerBounds)
            {
                prayerBounds.put(prayer, bounds);
            }

            // Check if prayer is selected in quick prayers
            boolean isSelected = isQuickPrayerSelected(prayer);
            boolean isHovered = prayer.equals(hoveredPrayer);
            boolean isClicked = prayer.equals(clickedPrayer) && 
                              (System.currentTimeMillis() - clickFeedbackTime) < CLICK_FEEDBACK_DURATION;

            // Determine background color based on state
            Color backgroundColor;
            if (isClicked) {
                // Bright flash for click feedback
                backgroundColor = new Color(255, 255, 255, 180);
            } else if (isSelected) {
                backgroundColor = SELECTED_COLOR;
            } else if (isHovered) {
                backgroundColor = HOVER_COLOR;
            } else {
                backgroundColor = UNSELECTED_COLOR;
            }

            // Draw prayer tile background
            graphics.setColor(backgroundColor);
            graphics.fillRect(x, y, PRAYER_TILE_WIDTH, PRAYER_TILE_HEIGHT);

            // Draw prayer tile border
            graphics.setColor(Color.WHITE);
            graphics.drawRect(x, y, PRAYER_TILE_WIDTH - 1, PRAYER_TILE_HEIGHT - 1);

            // Add hover effect with brighter border
            if (isHovered) {
                graphics.setColor(new Color(200, 200, 200));
                graphics.drawRect(x + 1, y + 1, PRAYER_TILE_WIDTH - 3, PRAYER_TILE_HEIGHT - 3);
            }

            // Draw prayer name (full name or abbreviated if too long)
            String prayerName = getPrayerDisplayName(prayer);
            FontMetrics fm = graphics.getFontMetrics();
            
            // Center the text horizontally and vertically
            int textX = x + (PRAYER_TILE_WIDTH - fm.stringWidth(prayerName)) / 2;
            int textY = y + (PRAYER_TILE_HEIGHT + fm.getAscent()) / 2 - 1;

            // Draw text shadow for better readability
            graphics.setColor(Color.BLACK);
            graphics.drawString(prayerName, textX + 1, textY + 1);
            
            // Draw main text
            graphics.setColor(Color.WHITE);
            graphics.drawString(prayerName, textX, textY);
        }

        // Restore original font
        graphics.setFont(originalFont);

        return gridHeight;
    }

    private boolean isQuickPrayerSelected(Prayer prayer)
    {
        int varbitId = getQuickPrayerVarbitId(prayer);
        return varbitId != -1 && client.getVarbitValue(varbitId) == 1;
    }

    private int getQuickPrayerVarbitId(Prayer prayer)
    {
        // Map prayers to their quick prayer varbit IDs
        switch (prayer)
        {
            case THICK_SKIN: return 4102;
            case BURST_OF_STRENGTH: return 4103;
            case CLARITY_OF_THOUGHT: return 4104;
            case SHARP_EYE: return 4105;
            case MYSTIC_WILL: return 4106;
            case ROCK_SKIN: return 4107;
            case SUPERHUMAN_STRENGTH: return 4108;
            case IMPROVED_REFLEXES: return 4109;
            case RAPID_RESTORE: return 4110;
            case RAPID_HEAL: return 4111;
            case PROTECT_ITEM: return 4112;
            case HAWK_EYE: return 4113;
            case MYSTIC_LORE: return 4114;
            case STEEL_SKIN: return 4115;
            case ULTIMATE_STRENGTH: return 4116;
            case INCREDIBLE_REFLEXES: return 4117;
            case PROTECT_FROM_MAGIC: return 4118;
            case PROTECT_FROM_MISSILES: return 4119;
            case PROTECT_FROM_MELEE: return 4120;
            case EAGLE_EYE: return 4121;
            case MYSTIC_MIGHT: return 4122;
            case RETRIBUTION: return 4123;
            case REDEMPTION: return 4124;
            case SMITE: return 4125;
            case CHIVALRY: return 4127;
            case DEADEYE: return 4128;
            case MYSTIC_VIGOUR: return 4129;
            case PIETY: return 4130;
            case PRESERVE: return 4126;
            case RIGOUR: return 4131;
            case AUGURY: return 4132;
            // Ruinous Powers prayers - these may use different varbits or not support quick prayers
            case RP_REJUVENATION: return -1; // Not supported in quick prayers
            case RP_ANCIENT_STRENGTH: return -1;
            case RP_ANCIENT_SIGHT: return -1;
            case RP_ANCIENT_WILL: return -1;
            case RP_PROTECT_ITEM: return -1;
            case RP_RUINOUS_GRACE: return -1;
            case RP_DAMPEN_MAGIC: return -1;
            case RP_DAMPEN_RANGED: return -1;
            case RP_DAMPEN_MELEE: return -1;
            case RP_TRINITAS: return -1;
            case RP_BERSERKER: return -1;
            case RP_PURGE: return -1;
            case RP_METABOLISE: return -1;
            case RP_REBUKE: return -1;
            case RP_VINDICATION: return -1;
            case RP_DECIMATE: return -1;
            case RP_ANNIHILATE: return -1;
            case RP_VAPORISE: return -1;
            case RP_FUMUS_VOW: return -1;
            case RP_UMBRA_VOW: return -1;
            case RP_CRUORS_VOW: return -1;
            case RP_GLACIES_VOW: return -1;
            case RP_WRATH: return -1;
            case RP_INTENSIFY: return -1;
            default: return -1;
        }
    }

    private String getPrayerDisplayName(Prayer prayer)
    {
        // With 60px width, we can fit most prayer names or use smart abbreviations
        String fullName = getPrayerFullName(prayer);
        
        // Simple length-based check - most characters are ~6px wide at size 10
        // With 58px available width (60px - 2px padding), we can fit ~9-10 characters
        if (fullName.length() <= 9) {
            return fullName;
        }
        
        // Otherwise use abbreviation
        return getPrayerAbbreviation(prayer);
    }

    private String getPrayerFullName(Prayer prayer)
    {
        switch (prayer)
        {
            case THICK_SKIN: return "Thick Skin";
            case BURST_OF_STRENGTH: return "Burst Str";
            case CLARITY_OF_THOUGHT: return "Clarity";
            case SHARP_EYE: return "Sharp Eye";
            case MYSTIC_WILL: return "Mystic Will";
            case ROCK_SKIN: return "Rock Skin";
            case SUPERHUMAN_STRENGTH: return "Super Str";
            case IMPROVED_REFLEXES: return "Imp Reflex";
            case RAPID_RESTORE: return "Rapid Rest";
            case RAPID_HEAL: return "Rapid Heal";
            case PROTECT_ITEM: return "Prot Item";
            case HAWK_EYE: return "Hawk Eye";
            case MYSTIC_LORE: return "Myst Lore";
            case STEEL_SKIN: return "Steel Skin";
            case ULTIMATE_STRENGTH: return "Ult Str";
            case INCREDIBLE_REFLEXES: return "Inc Reflex";
            case PROTECT_FROM_MAGIC: return "Prot Mage";
            case PROTECT_FROM_MISSILES: return "Prot Range";
            case PROTECT_FROM_MELEE: return "Prot Melee";
            case EAGLE_EYE: return "Eagle Eye";
            case MYSTIC_MIGHT: return "Myst Might";
            case RETRIBUTION: return "Retrib";
            case REDEMPTION: return "Redemp";
            case SMITE: return "Smite";
            case CHIVALRY: return "Chivalry";
            case DEADEYE: return "Deadeye";
            case MYSTIC_VIGOUR: return "Myst Vigour";
            case PIETY: return "Piety";
            case PRESERVE: return "Preserve";
            case RIGOUR: return "Rigour";
            case AUGURY: return "Augury";
            // Ruinous Powers prayers
            case RP_REJUVENATION: return "Rejuv";
            case RP_ANCIENT_STRENGTH: return "Anc Str";
            case RP_ANCIENT_SIGHT: return "Anc Sight";
            case RP_ANCIENT_WILL: return "Anc Will";
            case RP_PROTECT_ITEM: return "RP Item";
            case RP_RUINOUS_GRACE: return "Ruin Grace";
            case RP_DAMPEN_MAGIC: return "Damp Mage";
            case RP_DAMPEN_RANGED: return "Damp Range";
            case RP_DAMPEN_MELEE: return "Damp Melee";
            case RP_TRINITAS: return "Trinitas";
            case RP_BERSERKER: return "Berserker";
            case RP_PURGE: return "Purge";
            case RP_METABOLISE: return "Metabol";
            case RP_REBUKE: return "Rebuke";
            case RP_VINDICATION: return "Vindicat";
            case RP_DECIMATE: return "Decimate";
            case RP_ANNIHILATE: return "Annihil";
            case RP_VAPORISE: return "Vaporise";
            case RP_FUMUS_VOW: return "Fumus";
            case RP_UMBRA_VOW: return "Umbra";
            case RP_CRUORS_VOW: return "Cruors";
            case RP_GLACIES_VOW: return "Glacies";
            case RP_WRATH: return "Wrath";
            case RP_INTENSIFY: return "Intensify";
            default: return "Unknown";
        }
    }

    private String getPrayerAbbreviation(Prayer prayer)
    {
        switch (prayer)
        {
            case THICK_SKIN: return "Thick";
            case BURST_OF_STRENGTH: return "Burst";
            case CLARITY_OF_THOUGHT: return "Clarity";
            case SHARP_EYE: return "Sharp";
            case MYSTIC_WILL: return "Mystic";
            case ROCK_SKIN: return "Rock";
            case SUPERHUMAN_STRENGTH: return "Super";
            case IMPROVED_REFLEXES: return "Reflex";
            case RAPID_RESTORE: return "Restore";
            case RAPID_HEAL: return "Heal";
            case PROTECT_ITEM: return "Item";
            case HAWK_EYE: return "Hawk";
            case MYSTIC_LORE: return "Lore";
            case STEEL_SKIN: return "Steel";
            case ULTIMATE_STRENGTH: return "Ultimate";
            case INCREDIBLE_REFLEXES: return "Incredib";
            case PROTECT_FROM_MAGIC: return "Mage";
            case PROTECT_FROM_MISSILES: return "Range";
            case PROTECT_FROM_MELEE: return "Melee";
            case EAGLE_EYE: return "Eagle";
            case MYSTIC_MIGHT: return "Might";
            case RETRIBUTION: return "Retrib";
            case REDEMPTION: return "Redemp";
            case SMITE: return "Smite";
            case CHIVALRY: return "Chiv";
            case DEADEYE: return "Dead";
            case MYSTIC_VIGOUR: return "Vigour";
            case PIETY: return "Piety";
            case PRESERVE: return "Preserve";
            case RIGOUR: return "Rigour";
            case AUGURY: return "Augury";
            // Ruinous Powers prayers
            case RP_REJUVENATION: return "Rejuv";
            case RP_ANCIENT_STRENGTH: return "AncStr";
            case RP_ANCIENT_SIGHT: return "AncSgt";
            case RP_ANCIENT_WILL: return "AncWil";
            case RP_PROTECT_ITEM: return "RPItem";
            case RP_RUINOUS_GRACE: return "RGrace";
            case RP_DAMPEN_MAGIC: return "DMage";
            case RP_DAMPEN_RANGED: return "DRange";
            case RP_DAMPEN_MELEE: return "DMelee";
            case RP_TRINITAS: return "Trinit";
            case RP_BERSERKER: return "Bersrk";
            case RP_PURGE: return "Purge";
            case RP_METABOLISE: return "Metab";
            case RP_REBUKE: return "Rebuke";
            case RP_VINDICATION: return "Vindic";
            case RP_DECIMATE: return "Decim";
            case RP_ANNIHILATE: return "Annih";
            case RP_VAPORISE: return "Vapor";
            case RP_FUMUS_VOW: return "Fumus";
            case RP_UMBRA_VOW: return "Umbra";
            case RP_CRUORS_VOW: return "Cruors";
            case RP_GLACIES_VOW: return "Glacies";
            case RP_WRATH: return "Wrath";
            case RP_INTENSIFY: return "Intens";
            default: return "?";
        }
    }

    private int getMaxWidth()
    {
        int maxWidth = 200; // Prayer points bar width

        if (config.showPrayerGrid())
        {
            int gridWidth = config.gridColumns() * (PRAYER_TILE_WIDTH + GRID_PADDING) - GRID_PADDING;
            maxWidth = Math.max(maxWidth, gridWidth);
        }

        return maxWidth;
    }

    @Override
    public Rectangle getBounds()
    {
        // Calculate actual overlay height based on enabled components
        int totalHeight = 0;
        
        if (config.showPrayerPoints())
        {
            totalHeight += 20 + COMPONENT_SPACING; // Prayer points bar height
        }
        
        if (config.showQuickPrayerButton())
        {
            totalHeight += config.quickPrayerButtonSize().getSize() + COMPONENT_SPACING; // Quick prayer button height
        }
        
        if (config.showPrayerGrid())
        {
            Prayer[] prayers = Prayer.values();
            int columns = config.gridColumns();
            int rows = (prayers.length + columns - 1) / columns;
            totalHeight += rows * (PRAYER_TILE_HEIGHT + GRID_PADDING) - GRID_PADDING;
        }
        
        // Remove the last component spacing if we added any components
        if (totalHeight > 0 && (config.showPrayerPoints() || config.showQuickPrayerButton()))
        {
            totalHeight -= COMPONENT_SPACING;
        }
        
        return new Rectangle(0, 0, getMaxWidth(), Math.max(totalHeight, 1));
    }

    public void updatePrayerStates()
    {
        // Called when prayer states change - overlay will update on next render
    }

    public Prayer getPrayerAtPoint(Point point)
    {
        // Convert screen coordinates to overlay-relative coordinates
        Point overlayPoint = getOverlayRelativePoint(point);
        
        // Use synchronized access to avoid ConcurrentModificationException
        synchronized (prayerBounds)
        {
            for (Map.Entry<Prayer, Rectangle> entry : prayerBounds.entrySet())
            {
                if (entry.getValue().contains(overlayPoint))
                {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean isQuickPrayerButtonClicked(Point point)
    {
        if (quickPrayerButtonBounds == null) 
        {
            return false;
        }
        
        Point overlayPoint = getOverlayRelativePoint(point);
        return quickPrayerButtonBounds.contains(overlayPoint);
    }
    
    private boolean isPointInQuickPrayerButton(Point point)
    {
        if (quickPrayerButtonBounds == null) return false;
        
        Point overlayPoint = getOverlayRelativePoint(point);
        return quickPrayerButtonBounds.contains(overlayPoint);
    }

    private Point getOverlayRelativePoint(Point screenPoint)
    {
        // For RuneLite OverlayPanel, the mouse coordinates need to be converted
        // from screen coordinates to overlay-relative coordinates
        Rectangle panelBounds = super.getBounds();
        if (panelBounds == null)
        {
            return new Point(0, 0);
        }
        
        // Convert screen coordinates to overlay-relative coordinates
        return new Point(screenPoint.x - panelBounds.x, screenPoint.y - panelBounds.y);
    }

    /**
     * Handles mouse click events for the overlay.
     * Returns the original event - consumption is handled differently.
     */
    public MouseEvent handleMouseClick(MouseEvent mouseEvent)
    {
        if (isWithinOverlayBounds(mouseEvent.getPoint()))
        {
            handleOverlayClick(mouseEvent);
            // Don't return null - let the event continue but mark it as handled
        }
        return mouseEvent;
    }

    /**
     * Handles mouse press events for the overlay.
     * Returns null if the event should be consumed, otherwise returns the original event.
     */
    public MouseEvent handleMousePress(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) {
            return null;
        }
        
        if (isWithinOverlayBounds(mouseEvent.getPoint()))
        {
            // Don't consume press events, let them through
            return mouseEvent;
        }
        return mouseEvent;
    }

    /**
     * Handles mouse release events for the overlay.
     * Returns null if the event should be consumed, otherwise returns the original event.
     */
    public MouseEvent handleMouseRelease(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) {
            return null;
        }
        
        if (isWithinOverlayBounds(mouseEvent.getPoint()))
        {
            // Don't consume release events, let them through
            return mouseEvent;
        }
        return mouseEvent;
    }

    /**
     * Handles mouse movement events for hover effects.
     * Returns null if the event should be consumed, otherwise returns the original event.
     */
    public MouseEvent handleMouseMove(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) {
            return null;
        }
        
        if (isWithinOverlayBounds(mouseEvent.getPoint()))
        {
            updateHoverState(mouseEvent.getPoint());
            return mouseEvent; // Don't consume mouse move events
        } else {
            // Clear hover state when mouse leaves overlay
            if (hoveredPrayer != null) {
                hoveredPrayer = null;
            }
            if (isQuickPrayerButtonHovered) {
                isQuickPrayerButtonHovered = false;
            }
        }
        return mouseEvent;
    }

    private void updateHoverState(Point point)
    {
        Prayer newHoveredPrayer = getPrayerAtPoint(point);
        if (newHoveredPrayer != hoveredPrayer) {
            hoveredPrayer = newHoveredPrayer;
            // The overlay will repaint automatically, showing the hover effect
        }
        
        // Update quick prayer button hover state
        boolean newButtonHoverState = isPointInQuickPrayerButton(point);
        if (newButtonHoverState != isQuickPrayerButtonHovered) {
            isQuickPrayerButtonHovered = newButtonHoverState;
        }
    }

    private boolean isWithinOverlayBounds(Point point)
    {
        // For OverlayPanel, we need to check if the point is within our rendered area
        // The overlay's position is managed by RuneLite, so we check against our content bounds
        Rectangle contentBounds = getBounds();
        
        // Get the overlay's actual position from the panel bounds
        Rectangle panelBounds = super.getBounds();
        if (panelBounds != null)
        {
            // Create a rectangle representing our actual screen area
            Rectangle screenBounds = new Rectangle(panelBounds.x, panelBounds.y, 
                                                 contentBounds.width, contentBounds.height);
            return screenBounds.contains(point);
        }
        

        return false;
    }

    private boolean isWithinInteractiveElement(Point point)
    {
        Point overlayPoint = getOverlayRelativePoint(point);
        
        // Check if point is within quick prayer button
        if (quickPrayerButtonBounds != null && quickPrayerButtonBounds.contains(overlayPoint))
        {
            return true;
        }
        
        // Check if point is within any prayer tile
        for (Rectangle prayerBound : prayerBounds.values())
        {
            if (prayerBound.contains(overlayPoint))
            {
                return true;
            }
        }
        
        return false;
    }

    private void handleOverlayClick(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton() != MouseEvent.BUTTON1)
        {
            return; // Only handle left clicks
        }

        Point clickPoint = mouseEvent.getPoint();
        // Check if quick prayer button was clicked
        if (isQuickPrayerButtonClicked(clickPoint))
        {
            handleQuickPrayerButtonClick();
            return;
        }

        // Check if a prayer tile was clicked
        Prayer clickedPrayer = getPrayerAtPoint(clickPoint);
        if (clickedPrayer != null)
        {
            handlePrayerTileClick(clickedPrayer);
        }
    }

    private void handleQuickPrayerButtonClick()
    {
        try
        {
            // Provide immediate visual feedback
            isQuickPrayerButtonClicked = true;
            buttonClickFeedbackTime = System.currentTimeMillis();
            
            System.out.println("Quick Prayer button clicked!");
            
            // Execute client calls on the client thread
            clientThread.invoke(() -> {
                try {
                    boolean currentState = client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
                    System.out.println("Current quick prayer state: " + currentState);
                    
                    // Try multiple approaches to toggle quick prayer
                    String option = currentState ? "Deactivate" : "Activate";
                    String target = "Quick-prayers";
                    
                    System.out.println("Attempting to toggle quick prayer: " + option);
                    
                    // Approach 1: Try the minimap orb with different parameters
                    try {
                        int orbWidgetId = WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getId();
                        client.menuAction(
                            1, // p0 - try 1 for first option
                            orbWidgetId, // p1 - widget id
                            MenuAction.CC_OP, // action
                            orbWidgetId, // id
                            -1, // itemId
                            option, // option
                            target // target
                        );
                        System.out.println("Tried minimap orb approach");
                    } catch (Exception e1) {
                        System.out.println("Minimap orb approach failed: " + e1.getMessage());
                        
                        // Approach 2: Try using the prayer tab quick prayer button
                        try {
                            // The quick prayer button in the prayer tab might have a different widget ID
                            client.menuAction(
                                0, // p0
                                0, // p1
                                MenuAction.WIDGET_FIRST_OPTION, // action
                                1, // id - generic component click
                                -1, // itemId
                                option, // option
                                target // target
                            );
                            System.out.println("Tried prayer tab approach");
                        } catch (Exception e2) {
                            System.out.println("Prayer tab approach also failed: " + e2.getMessage());
                        }
                    }
                    
                    System.out.println("Toggled quick prayer: " + (currentState ? "OFF" : "ON"));
                } catch (Exception e) {
                    System.err.println("Failed to toggle quick prayer: " + e.getMessage());
                }
            });
            
        }
        catch (Exception e)
        {
            System.err.println("Failed to handle quick prayer button click: " + e.getMessage());
        }
    }



    private void handlePrayerTileClick(Prayer prayer)
    {
        try
        {
            // Provide immediate visual feedback
            clickedPrayer = prayer;
            clickFeedbackTime = System.currentTimeMillis();
            
            System.out.println("Prayer tile clicked: " + prayer.name());
            
            // Execute client calls on the client thread
            clientThread.invoke(() -> {
                try {
                    // Get the current quick prayer selection state
                    boolean isCurrentlySelected = isQuickPrayerSelected(prayer);
                    int childId = getPrayerChildId(prayer);
                    
                    System.out.println("Prayer state - currently selected: " + isCurrentlySelected + 
                                     ", child ID: " + childId);
                    
                    // Toggle the quick prayer selection for this prayer
                    toggleQuickPrayerSelection(prayer);
                } catch (Exception e) {
                    System.err.println("Failed to process prayer tile click: " + e.getMessage());
                }
            });
            
        }
        catch (Exception e)
        {
            System.err.println("Failed to handle prayer tile click: " + e.getMessage());
        }
    }

    private void toggleQuickPrayerSelection(Prayer prayer)
    {
        try
        {
            // Check if this prayer supports quick prayer functionality
            int varbitId = getQuickPrayerVarbitId(prayer);
            if (varbitId == -1)
            {
                System.out.println("Prayer " + prayer.name() + " does not support quick prayer selection");
                return;
            }

            // Get the prayer widget child ID for the specific prayer
            int prayerChildId = getPrayerChildId(prayer);
            if (prayerChildId == -1)
            {
                System.err.println("Unknown prayer child ID for: " + prayer.name());
                return;
            }

            // Use menu action to toggle quick prayer selection
            // Use the standard prayer tab group ID (541 is correct for prayer tab)
            int prayerTabGroupId = 541; // Prayer tab widget group ID
            
            client.menuAction(
                prayerChildId, // p0 (child ID)
                prayerTabGroupId, // p1 (parent widget group ID)
                MenuAction.CC_OP, // action
                (prayerTabGroupId << 16) | prayerChildId, // id (prayer widget ID)
                -1, // itemId
                "Toggle Quick-prayer", // option
                prayer.name() // target
            );
            
            System.out.println("Invoked menu action for: " + prayer.name() + " (childId: " + prayerChildId + ", varbitId: " + varbitId + ")");
            
        }
        catch (Exception e)
        {
            System.err.println("Failed to toggle quick prayer selection for " + prayer.name() + ": " + e.getMessage());
        }
    }



    private int getPrayerChildId(Prayer prayer)
    {
        // Map prayers to their child IDs within the prayer tab widget
        // These correspond to the ordinal values of the Prayer enum
        switch (prayer)
        {
            case THICK_SKIN: return 0;
            case BURST_OF_STRENGTH: return 1;
            case CLARITY_OF_THOUGHT: return 2;
            case SHARP_EYE: return 3;
            case MYSTIC_WILL: return 4;
            case ROCK_SKIN: return 5;
            case SUPERHUMAN_STRENGTH: return 6;
            case IMPROVED_REFLEXES: return 7;
            case RAPID_RESTORE: return 8;
            case RAPID_HEAL: return 9;
            case PROTECT_ITEM: return 10;
            case HAWK_EYE: return 11;
            case MYSTIC_LORE: return 12;
            case STEEL_SKIN: return 13;
            case ULTIMATE_STRENGTH: return 14;
            case INCREDIBLE_REFLEXES: return 15;
            case PROTECT_FROM_MAGIC: return 16;
            case PROTECT_FROM_MISSILES: return 17;
            case PROTECT_FROM_MELEE: return 18;
            case EAGLE_EYE: return 19;
            case MYSTIC_MIGHT: return 20;
            case RETRIBUTION: return 21;
            case REDEMPTION: return 22;
            case SMITE: return 23;
            case CHIVALRY: return 24;
            case DEADEYE: return 25;
            case MYSTIC_VIGOUR: return 26;
            case PIETY: return 27;
            case PRESERVE: return 28;
            case RIGOUR: return 29;
            case AUGURY: return 30;
            case RP_REJUVENATION: return 31;
            case RP_ANCIENT_STRENGTH: return 32;
            case RP_ANCIENT_SIGHT: return 33;
            case RP_ANCIENT_WILL: return 34;
            case RP_PROTECT_ITEM: return 35;
            case RP_RUINOUS_GRACE: return 36;
            case RP_DAMPEN_MAGIC: return 37;
            case RP_DAMPEN_RANGED: return 38;
            case RP_DAMPEN_MELEE: return 39;
            case RP_TRINITAS: return 40;
            case RP_BERSERKER: return 41;
            case RP_PURGE: return 42;
            case RP_METABOLISE: return 43;
            case RP_REBUKE: return 44;
            case RP_VINDICATION: return 45;
            case RP_DECIMATE: return 46;
            case RP_ANNIHILATE: return 47;
            case RP_VAPORISE: return 48;
            case RP_FUMUS_VOW: return 49;
            case RP_UMBRA_VOW: return 50;
            case RP_CRUORS_VOW: return 51;
            case RP_GLACIES_VOW: return 52;
            case RP_WRATH: return 53;
            case RP_INTENSIFY: return 54;
            default: return -1;
        }
    }


}
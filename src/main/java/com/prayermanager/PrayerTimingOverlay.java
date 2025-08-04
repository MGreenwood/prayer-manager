package com.prayermanager;

import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.config.ConfigManager;

@Singleton
public class PrayerTimingOverlay extends Overlay
{
    private static final Logger log = Logger.getLogger(PrayerTimingOverlay.class.getName());
    
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;

    @Inject
    private Client client;

    @Inject
    private PrayerManagerConfig config;
    
    @Inject
    private ConfigManager configManager;

    private long lastTickTime = 0;
    private boolean isDragging = false;
    private Point dragStartPoint = null;
    private Point overlayStartPosition = null;

    public PrayerTimingOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED); // Different priority to prevent interference
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setDragTargetable(true);
        setMovable(true);
        setResizable(false);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        try
        {
            if (client.getGameState() != GameState.LOGGED_IN || !config.showTimingBar())
            {
                return null;
            }

            // Load saved position if not already loaded - independent from prayer grid overlay
            loadSavedPosition();
            
            // Apply overlay opacity setting
            float opacity = config.overlayOpacity() / 100.0f;
            if (isDragging)
            {
                opacity *= 0.7f; // Additional transparency during drag
            }
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            
            // Apply visual feedback during dragging
            if (isDragging)
            {
                // Draw a border around the timing overlay during drag to show it's moving independently
                graphics.setColor(new Color(0, 255, 255, 150)); // Cyan border to distinguish from prayer grid
                graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3}, 0));
                graphics.drawRect(0, 0, BAR_WIDTH - 1, BAR_HEIGHT + 29);
            }

            // If no saved position, try to position near the prayer orb as default
            if (getPreferredLocation() == null)
            {
                try
                {
                    net.runelite.api.widgets.Widget prayerOrb = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
                    if (prayerOrb != null)
                    {
                        // Position the timing bar near the prayer orb as default
                        int orbX = prayerOrb.getCanvasLocation().getX();
                        int orbY = prayerOrb.getCanvasLocation().getY();
                        
                        setPreferredLocation(new Point(orbX - BAR_WIDTH / 2, orbY + 30));
                    }
                }
                catch (Exception e)
                {
                    handleError("Failed to position timing overlay near prayer orb", e);
                }
            }

            long currentTime = System.currentTimeMillis();
            long timeSinceLastTick = currentTime - lastTickTime;

            double tickProgress = (timeSinceLastTick % 600) / 600.0;

            renderTimingBar(graphics, tickProgress);

            return new Dimension(BAR_WIDTH, BAR_HEIGHT + 30);
        }
        catch (Exception e)
        {
            handleError("Error rendering timing overlay", e);
            return null;
        }
    }

    private void renderTimingBar(Graphics2D graphics, double progress)
    {
        try
        {
            // Use configured colors
            Color backgroundColor = new Color(0, 0, 0, 100);
            Color tickColor = new Color(255, 255, 0, 150);
            
            // Draw background
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, BAR_WIDTH, BAR_HEIGHT);

            // Draw tick progress
            int progressWidth = (int) (BAR_WIDTH * progress);
            graphics.setColor(tickColor);
            graphics.fillRect(0, 0, progressWidth, BAR_HEIGHT);

            // Draw border using configured border color
            graphics.setColor(config.borderColor());
            graphics.drawRect(0, 0, BAR_WIDTH - 1, BAR_HEIGHT - 1);

            // Add text using configured text color
            graphics.setColor(config.textColor());
            graphics.drawString("Tick Progress", 5, BAR_HEIGHT + 15);
        }
        catch (Exception e)
        {
            handleError("Error rendering timing bar", e);
        }
    }

    public void onGameTick()
    {
        lastTickTime = System.currentTimeMillis();
    }
    
    @Override
    public Rectangle getBounds()
    {
        Point location = getPreferredLocation();
        if (location == null)
        {
            location = new Point(0, 0);
        }
        
        return new Rectangle(location.x, location.y, BAR_WIDTH, BAR_HEIGHT + 30);
    }
    
    // Position management methods - independent from prayer grid overlay
    private void loadSavedPosition()
    {
        int savedX = config.timingOverlayPositionX();
        int savedY = config.timingOverlayPositionY();
        
        if (savedX != -1 && savedY != -1)
        {
            setPreferredLocation(new Point(savedX, savedY));
        }
    }
    
    private void savePosition()
    {
        Point position = getPreferredLocation();
        if (position != null)
        {
            configManager.setConfiguration("prayermanager", "timingOverlayPositionX", position.x);
            configManager.setConfiguration("prayermanager", "timingOverlayPositionY", position.y);
        }
    }
    
    // Mouse event handling for independent interaction
    public boolean isWithinOverlayBounds(Point point)
    {
        Point location = getPreferredLocation();
        if (location == null) return false;
        
        Rectangle bounds = new Rectangle(location.x, location.y, BAR_WIDTH, BAR_HEIGHT + 30);
        return bounds.contains(point);
    }
    
    public boolean handleMouseClick(Point point)
    {
        // Consume clicks within the timing overlay bounds to prevent pass-through
        return isWithinOverlayBounds(point);
    }
    
    public boolean handleMousePress(Point point)
    {
        if (isWithinOverlayBounds(point))
        {
            startDrag(point);
            return true; // Event consumed
        }
        return false;
    }
    
    public boolean handleMouseRelease(Point point)
    {
        if (isDragging)
        {
            completeDrag();
            return true; // Event consumed
        }
        return false;
    }
    
    public boolean handleMouseDrag(Point point)
    {
        if (isDragging)
        {
            updateDrag(point);
            return true; // Event consumed
        }
        return false;
    }
    
    // Custom drag handling through mouse events
    public void startDrag(Point point)
    {
        isDragging = true;
        dragStartPoint = new Point(point);
        overlayStartPosition = getPreferredLocation();
        if (overlayStartPosition == null)
        {
            overlayStartPosition = new Point(0, 0);
        }
    }
    
    public void updateDrag(Point point)
    {
        if (isDragging && dragStartPoint != null && overlayStartPosition != null)
        {
            int deltaX = point.x - dragStartPoint.x;
            int deltaY = point.y - dragStartPoint.y;
            
            Point newPosition = new Point(
                overlayStartPosition.x + deltaX,
                overlayStartPosition.y + deltaY
            );
            
            setPreferredLocation(newPosition);
        }
    }
    
    public void completeDrag()
    {
        isDragging = false;
        dragStartPoint = null;
        overlayStartPosition = null;
        
        // Save the new position
        savePosition();
    }
    
    // Utility methods for independence verification
    public boolean isDragging()
    {
        return isDragging;
    }
    
    public boolean hasIndependentPosition()
    {
        return config.timingOverlayPositionX() != -1 && config.timingOverlayPositionY() != -1;
    }
    
    // Error handling method
    private void handleError(String message, Exception e)
    {
        PrayerManagerConfig.ErrorHandlingMode mode = config.errorHandlingMode();
        
        switch (mode)
        {
            case STRICT:
                log.log(Level.SEVERE, message, e);
                break;
            case GRACEFUL:
                if (e instanceof RuntimeException)
                {
                    log.log(Level.WARNING, message + ": " + e.getMessage());
                }
                break;
            case SILENT:
                // Only log to debug level
                log.log(Level.FINE, message, e);
                break;
        }
    }
}
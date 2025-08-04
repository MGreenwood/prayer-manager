# Prayer Manager Plugin Fixes - Design Document

## Overview

This design addresses the critical issues with the RuneLite Prayer Manager plugin by implementing proper mouse event handling, overlay positioning, and UI customization. The solution focuses on making overlays truly interactive while preventing click pass-through to the game world.

## Architecture

### Core Components

1. **Enhanced PrayerManagerOverlay** - Main interactive overlay with proper mouse event handling
2. **Independent PrayerTimingOverlay** - Separately draggable timing visualization
3. **MouseEventHandler** - Centralized click detection and consumption logic
4. **ConfigurationManager** - Extended settings for button customization and positioning
5. **OverlayPositionManager** - Handles independent positioning of multiple overlays

### Event Flow

```
Mouse Click → MouseEventHandler → Overlay Bounds Check → Action Execution → Event Consumption
```

## Components and Interfaces

### Enhanced Mouse Event Handling

**PrayerManagerOverlay Changes:**
- Override `mouseClicked()`, `mousePressed()`, `mouseReleased()` methods
- Implement proper bounds checking for all interactive elements
- Return consumed mouse events to prevent pass-through
- Add hover effects for better user feedback

**MouseAdapter Implementation:**
```java
public class PrayerMouseAdapter extends MouseAdapter {
    @Override
    public MouseEvent mouseClicked(MouseEvent e) {
        if (isWithinOverlayBounds(e.getPoint())) {
            handleOverlayClick(e);
            return null; // Consume event
        }
        return e; // Pass through
    }
}
```

### Improved Prayer Tile Design

**Tile Sizing:**
- Increase tile width from 24px to 60px to accommodate full prayer names
- Maintain 24px height for compact vertical layout
- Add 2px padding between tiles
- Use abbreviated names only when necessary

**Visual Feedback:**
- Selected prayers: Green background (#00FF00 with 120 alpha)
- Unselected prayers: Dark gray background (#404040 with 120 alpha)
- Hover state: Lighter border and slight brightness increase
- Click feedback: Brief color flash on successful toggle

### Customizable Quick Prayer Button

**Size Options:**
- Small: 24x24px
- Medium: 32x32px (default)
- Large: 40x40px

**Visual States:**
- Inactive: User-configured background color with 50% opacity
- Active: User-configured background color with 100% opacity + yellow border
- Hover: 10% brightness increase
- Click: Brief scale animation (95% → 100%)

**Configuration Interface:**
```java
public enum ButtonSize { SMALL(24), MEDIUM(32), LARGE(40); }

@ConfigItem(name = "Quick Prayer Button Size")
default ButtonSize quickPrayerButtonSize() { return ButtonSize.MEDIUM; }

@ConfigItem(name = "Quick Prayer Button Color")
default Color quickPrayerButtonColor() { return Color.BLUE; }
```

### Independent Overlay Positioning

**Position Storage:**
- Store overlay positions in RuneLite's configuration system
- Use separate keys for each overlay: `prayerGridPosition`, `timingOverlayPosition`
- Persist positions between sessions

**Drag Implementation:**
- Each overlay maintains its own drag state
- Use `setDragTargetable(true)` and override drag methods
- Implement visual drag feedback (semi-transparent overlay during drag)

## Data Models

### Prayer State Management

```java
public class PrayerState {
    private final Prayer prayer;
    private boolean isQuickPrayerSelected;
    private boolean isCurrentlyActive;
    private Rectangle bounds;
    
    public void toggle() {
        // Toggle quick prayer selection via client API
    }
}
```

### Overlay Configuration

```java
public class OverlayConfig {
    private Point position;
    private boolean isVisible;
    private boolean isDraggable;
    
    // Persistence methods
    public void savePosition(ConfigManager config, String key);
    public void loadPosition(ConfigManager config, String key);
}
```

## Error Handling

### Click Event Failures
- Log failed prayer toggles without breaking overlay functionality
- Provide visual feedback when actions cannot be completed
- Gracefully handle widget unavailability

### API Compatibility
- Wrap RuneLite API calls in try-catch blocks
- Provide fallback behavior for deprecated methods
- Version-specific handling for different RuneLite releases

### Configuration Errors
- Validate color values and size settings
- Provide sensible defaults for invalid configurations
- Handle missing or corrupted position data

## Testing Strategy

### Interactive Testing
- Verify click events are properly consumed and don't pass through
- Test drag functionality for both overlays independently
- Validate prayer state synchronization with game state
- Confirm visual feedback responds correctly to user actions

### Configuration Testing
- Test all button size and color combinations
- Verify position persistence across client restarts
- Validate configuration migration from previous versions

### Edge Case Testing
- Test behavior when prayer tab is not available
- Handle rapid clicking and double-click scenarios
- Verify overlay behavior during game state changes (logout/login)

### Performance Testing
- Ensure mouse event handling doesn't impact game performance
- Verify overlay rendering performance with large prayer grids
- Test memory usage with extended gameplay sessions

## Implementation Notes

### RuneLite API Considerations
- Use `client.invokeMenuAction()` for prayer toggles with proper parameters
- Handle widget ID changes between RuneLite versions
- Implement proper varbit monitoring for prayer state changes

### Mouse Event Consumption
- Return `null` from mouse event handlers to consume events
- Use proper event ordering to prevent conflicts with other plugins
- Implement event priority handling for overlay interactions

### Visual Polish
- Add smooth transitions for state changes
- Implement consistent color schemes across all components
- Ensure accessibility with high contrast options
- Provide visual feedback for all interactive elements
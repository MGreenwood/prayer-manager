# Prayer Manager Plugin Fixes - Requirements Document

## Introduction

This specification addresses critical issues with the RuneLite Prayer Manager plugin that prevent proper user interaction and positioning. The plugin currently has a prayer grid that displays but is not interactive, positioning issues with overlays, and sizing problems with prayer tiles.

## Requirements

### Requirement 1: Interactive Prayer Grid

**User Story:** As a player, I want to click on prayer tiles in the grid to toggle them in my quick prayer setup, so that I can efficiently manage my prayers without opening the prayer tab.

#### Acceptance Criteria

1. WHEN I click on a prayer tile in the grid THEN the system SHALL toggle that prayer's quick prayer selection state
2. WHEN I click on a prayer tile THEN the system SHALL prevent the click from passing through to the game world
3. WHEN a prayer is selected for quick prayers THEN the tile SHALL display with a green highlight
4. WHEN a prayer is not selected for quick prayers THEN the tile SHALL display with a darker background
5. WHEN I click on a prayer tile THEN the system SHALL provide immediate visual feedback of the state change

### Requirement 2: Proper Overlay Positioning and Dragging

**User Story:** As a player, I want to drag the prayer grid overlay to reposition it on my screen, so that I can place it where it doesn't interfere with my gameplay.

#### Acceptance Criteria

1. WHEN I drag the prayer grid overlay THEN all components (prayer points bar, quick prayer button, and grid) SHALL move together as one unit
2. WHEN I drag the timing overlay THEN it SHALL move independently from the prayer grid
3. WHEN I position overlays THEN they SHALL remember their positions between game sessions
4. WHEN overlays are dragged THEN they SHALL not interfere with game world interactions
5. WHEN I drag an overlay THEN it SHALL provide visual feedback during the drag operation

### Requirement 3: Improved Prayer Tile Sizing and Display

**User Story:** As a player, I want prayer tiles to be large enough to display the full prayer name clearly, so that I can easily identify which prayers I'm selecting.

#### Acceptance Criteria

1. WHEN prayer tiles are displayed THEN they SHALL be wide enough to show the complete prayer name
2. WHEN prayer names are displayed THEN they SHALL use a readable font size
3. WHEN prayer tiles are rendered THEN they SHALL maintain consistent spacing and alignment
4. WHEN the grid is displayed THEN it SHALL automatically adjust its width based on the number of columns configured
5. WHEN prayer names are too long for a tile THEN they SHALL be abbreviated in a consistent and recognizable way

### Requirement 4: Click Event Handling

**User Story:** As a player, I want my clicks on the prayer overlay to be properly captured and not pass through to the game world, so that I don't accidentally move my character when managing prayers.

#### Acceptance Criteria

1. WHEN I click anywhere on the prayer overlay THEN the click SHALL be consumed and not passed to the game world
2. WHEN I click on the prayer grid background THEN the click SHALL be consumed but no prayer action SHALL occur
3. WHEN I click on a prayer tile THEN the system SHALL execute the prayer toggle action and consume the click
4. WHEN I click on the quick prayer button THEN the system SHALL toggle quick prayers and consume the click
5. WHEN click events are consumed THEN the system SHALL return the appropriate mouse event to prevent pass-through

### Requirement 5: Customizable Quick Prayer Button

**User Story:** As a player, I want to customize the quick prayer button's size and appearance, so that it fits my UI preferences and is easily accessible during gameplay.

#### Acceptance Criteria

1. WHEN I configure the quick prayer button THEN I SHALL be able to choose between small, medium, and large sizes
2. WHEN I configure the quick prayer button THEN I SHALL be able to set a custom background color
3. WHEN I click the quick prayer button THEN it SHALL toggle my quick prayers exactly like the default prayer orb
4. WHEN quick prayers are active THEN the button SHALL display with a visual indicator (brighter color or border)
5. WHEN quick prayers are inactive THEN the button SHALL display in its normal configured appearance

### Requirement 6: Timing Overlay Independence

**User Story:** As a player, I want the tick timing overlay to be independently positionable from the prayer grid, so that I can place each overlay optimally for my gameplay style.

#### Acceptance Criteria

1. WHEN the timing overlay is displayed THEN it SHALL be draggable independently of the prayer grid
2. WHEN I configure overlay positions THEN each overlay SHALL maintain its own position settings
3. WHEN overlays are enabled/disabled THEN they SHALL not affect each other's positioning
4. WHEN the timing overlay is moved THEN it SHALL not interfere with the prayer grid's functionality
5. WHEN both overlays are active THEN they SHALL not overlap unless intentionally positioned to do so
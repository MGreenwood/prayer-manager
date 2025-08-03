#  Prayer Manager Plugin

A custom RuneLite plugin for Old School RuneScape that enhances prayer management through an intuitive overlay system. Designed for precision flicking, quick prayer setup, and real-time prayer point tracking.

---

##  Features

###  Quick Prayer Grid
- Persistent overlay showing all prayers in a grid format.
- Click to toggle prayers into your Quick Prayer group.
- Visual feedback (e.g., green highlight) for selected prayers.

###  Quick Prayer Toggle Button
- Overlay button to activate/deactivate your Quick Prayer setup.
- Mirrors the behavior of the prayer orb for seamless control.

### 1-Tick Flick Visualizer
- Timing bar synced with game ticks.
- Helps players manually flick prayers with perfect timing.

### Prayer Points Bar
- Horizontal bar showing current prayer points.
- Includes numeric overlay (e.g., `42 / 70`) and color gradient for clarity.

---

##  Technical Overview

- Built using RuneLite’s plugin API (`OverlayPanel`, `OverlayManager`)
- Hooks into `GameTick` and `VarbitChanged` events
- Uses `Prayer.values()` and `Varbits.QUICK_PRAYER_SELECTION_<id>` for grid logic
- Sideloadable via `.runelite/sideloaded-plugins` with proper `plugin.properties` and `@PluginDescriptor`

---

##  Compliance

- No automation or input simulation
- Fully manual prayer toggling and flicking
- Compliant with Jagex’s third-party client rules

---

##  Installation

1. Clone or download the plugin source.
2. Place it in your `.runelite/sideloaded-plugins` directory.
3. Ensure `plugin.properties` is correctly configured.
4. Restart RuneLite and enable the plugin.

---

## Testing

Use JUnit for plugin testing. Avoid running tests via `main()`—instead:

```bash
./gradlew test

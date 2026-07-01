package com.rapit.client.module;

/**
 * Top-level ClickGUI categories. Order here is the display order in
 * the GUI's category tab list.
 */
public enum ModuleCategory {
    HUD("HUD"),
    VISUAL("Visual"),
    PERFORMANCE("Performance"),
    COSMETIC("Cosmetics"),
    SETTINGS("Settings");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

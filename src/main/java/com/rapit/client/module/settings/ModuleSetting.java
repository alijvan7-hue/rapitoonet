package com.rapit.client.module.settings;

/**
 * Base type for a single configurable value inside a module's
 * right-click settings panel. Subclasses (SliderSetting,
 * ColorSetting, KeybindSetting) hold the actual value; this base
 * just carries the display name and its own AnimatedValue-driven
 * open/hover state is owned by the GUI, not the setting itself, so
 * settings stay pure data and stay easy to persist to config.
 */
public abstract class ModuleSetting {

    private final String name;

    protected ModuleSetting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

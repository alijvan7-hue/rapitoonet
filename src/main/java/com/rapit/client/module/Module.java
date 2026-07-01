package com.rapit.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

/**
 * Base class for every toggleable feature in Rapit Client.
 *
 * Subclasses override the on* hooks they care about. ModuleManager is
 * responsible for actually calling these hooks from the relevant
 * Forge events - modules themselves stay decoupled from the event bus.
 *
 * HUD-type modules additionally carry an (x, y) position so they can
 * be dragged around in the "move HUD" edit mode.
 */
public abstract class Module {

    /**
     * Sentinel "no keybind assigned" value. LWJGL's Keyboard class
     * has no KEY_NONE constant of its own (0 is simply unused by any
     * real key), so this is Rapit Client's own convention for it.
     */
    public static final int KEY_NONE = 0;

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final ModuleCategory category;

    private boolean enabled;
    private int keybind;

    // Position for HUD elements (top-left anchored, in scaled GUI coords).
    private float hudX;
    private float hudY;

    protected Module(String name, String description, ModuleCategory category, int defaultKeybind) {
        this(name, description, category, defaultKeybind, false);
    }

    protected Module(String name, String description, ModuleCategory category, int defaultKeybind, boolean enabledByDefault) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = defaultKeybind;
        this.enabled = enabledByDefault;
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /** Called once, the instant the module is switched on. */
    protected void onEnable() {
    }

    /** Called once, the instant the module is switched off. */
    protected void onDisable() {
    }

    /** Called every client tick while enabled. */
    public void onTick() {
    }

    /** Called every frame, during the HUD overlay pass, while enabled. */
    public void onRenderOverlay(ScaledResolution sr) {
    }

    /** Called every frame during 3D world render, while enabled. */
    public void onRenderWorld(float partialTicks) {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public String getKeybindName() {
        return keybind == KEY_NONE ? "NONE" : Keyboard.getKeyName(keybind);
    }

    public float getHudX() {
        return hudX;
    }

    public float getHudY() {
        return hudY;
    }

    public void setHudPosition(float x, float y) {
        this.hudX = x;
        this.hudY = y;
    }

    /** True for modules that draw something at a draggable HUD position. */
    public boolean isMovable() {
        return category == ModuleCategory.HUD;
    }
}

package com.rapit.client.modules.performance;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Forces "Fancy Graphics" off while enabled, which in vanilla 1.8.9
 * also governs several of the more expensive translucency/backdrop
 * effects used behind GUI screens (clouds, some particle blending),
 * and restores the previous setting on disable.
 */
public class FastGuiModule extends Module {

    private boolean previousFancy;

    public FastGuiModule() {
        super("Fast GUI", "Disables Fancy Graphics for lighter GUI/world rendering", ModuleCategory.PERFORMANCE, Keyboard.KEY_NONE, true);
    }

    @Override
    protected void onEnable() {
        previousFancy = mc.gameSettings.fancyGraphics;
        mc.gameSettings.fancyGraphics = false;
    }

    @Override
    protected void onDisable() {
        mc.gameSettings.fancyGraphics = previousFancy;
    }
}

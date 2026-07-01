package com.rapit.client.modules.performance;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Forces vanilla's particle setting down to MINIMAL while enabled and
 * restores the user's previous choice on disable, cutting one of the
 * heaviest FPS costs in crowded PvP fights.
 */
public class ParticleLimiterModule extends Module {

    private int previousSetting;

    public ParticleLimiterModule() {
        super("Particle Limiter", "Caps particle density for higher FPS", ModuleCategory.PERFORMANCE, Keyboard.KEY_NONE, true);
    }

    @Override
    protected void onEnable() {
        previousSetting = mc.gameSettings.particleSetting;
        mc.gameSettings.particleSetting = 2; // 0=all 1=decreased 2=minimal
    }

    @Override
    protected void onDisable() {
        mc.gameSettings.particleSetting = previousSetting;
    }
}

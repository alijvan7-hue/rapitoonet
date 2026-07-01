package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

/**
 * Sets gamma to a very high value while enabled so dark areas render
 * fully lit, and restores the user's original gamma setting on
 * disable so it doesn't leak into vanilla state.
 */
public class FullbrightModule extends Module {

    private float previousGamma;

    public FullbrightModule() {
        super("Fullbright", "Removes darkness by maxing out gamma", ModuleCategory.VISUAL, Keyboard.KEY_F);
    }

    @Override
    protected void onEnable() {
        GameSettings settings = mc.gameSettings;
        previousGamma = settings.gammaSetting;
        settings.gammaSetting = 1000.0F;
    }

    @Override
    protected void onDisable() {
        mc.gameSettings.gammaSetting = previousGamma;
    }
}

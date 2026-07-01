package com.rapit.client.modules.cosmetics;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Master switch for every cosmetic (cape, future accessories). Other
 * cosmetic renderers should check this module's isEnabled() state in
 * addition to their own, so players can hide all cosmetics at once
 * (e.g. before recording clean footage) without disabling each one.
 */
public class CosmeticsToggleModule extends Module {

    public CosmeticsToggleModule() {
        super("Cosmetics", "Master toggle for all cosmetic effects", ModuleCategory.COSMETIC, Keyboard.KEY_NONE, true);
    }
}

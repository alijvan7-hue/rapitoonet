package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Hold-to-zoom: while the configured key is held, temporarily
 * narrows the FOV for a sniper-scope-style zoom. Actual FOV
 * manipulation happens through the public gameSettings.fovSetting so
 * no ASM/mixin patch is required.
 */
public class ZoomModule extends Module {

    private static final float ZOOM_FOV = 30.0F;
    private float previousFov;
    private boolean wasKeyDown;

    public ZoomModule() {
        super("Zoom", "Hold to zoom in your field of view", ModuleCategory.VISUAL, Keyboard.KEY_C);
    }

    @Override
    public void onTick() {
        boolean keyDown = Keyboard.isKeyDown(getKeybind());
        if (keyDown && !wasKeyDown) {
            previousFov = mc.gameSettings.fovSetting;
            mc.gameSettings.fovSetting = ZOOM_FOV;
        } else if (!keyDown && wasKeyDown) {
            mc.gameSettings.fovSetting = previousFov;
        }
        wasKeyDown = keyDown;
    }

    @Override
    protected void onDisable() {
        if (wasKeyDown) {
            mc.gameSettings.fovSetting = previousFov;
            wasKeyDown = false;
        }
    }
}

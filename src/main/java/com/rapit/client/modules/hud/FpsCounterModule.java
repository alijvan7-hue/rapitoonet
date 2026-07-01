package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

/**
 * Displays the current frames-per-second. Reads Minecraft's internal
 * debugFPS counter via reflection since it isn't exposed publicly in
 * 1.8.9's obfuscated client.
 */
public class FpsCounterModule extends Module {

    private Field debugFpsField;

    public FpsCounterModule() {
        super("FPS Counter", "Shows current frames per second", ModuleCategory.HUD, Module.KEY_NONE, true);
        setHudPosition(4, 4);
        try {
            debugFpsField = Minecraft.class.getDeclaredField("debugFPS");
            debugFpsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            debugFpsField = null;
        }
    }

    private int readFps() {
        if (debugFpsField == null) {
            return -1;
        }
        try {
            return debugFpsField.getInt(mc);
        } catch (IllegalAccessException e) {
            return -1;
        }
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        int fps = readFps();
        String text = fps >= 0 ? fps + " FPS" : "FPS: ?";
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

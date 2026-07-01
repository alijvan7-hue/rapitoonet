package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Classic WASD + mouse-buttons keystroke display, drawn as a small
 * grid of key panels that light up yellow when pressed.
 */
public class KeystrokesModule extends Module {

    private static final int KEY_SIZE = 18;
    private static final int GAP = 2;

    public KeystrokesModule() {
        super("Keystrokes", "Shows WASD/mouse key presses on screen", ModuleCategory.VISUAL, Module.KEY_NONE, true);
        setHudPosition(4, 200);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        GameSettings s = mc.gameSettings;
        float x = getHudX();
        float y = getHudY();

        drawKey("W", x + KEY_SIZE + GAP, y, s.keyBindForward.isKeyDown());
        drawKey("A", x, y + KEY_SIZE + GAP, s.keyBindLeft.isKeyDown());
        drawKey("S", x + KEY_SIZE + GAP, y + KEY_SIZE + GAP, s.keyBindBack.isKeyDown());
        drawKey("D", x + (KEY_SIZE + GAP) * 2, y + KEY_SIZE + GAP, s.keyBindRight.isKeyDown());

        float clickY = y + (KEY_SIZE + GAP) * 2;
        drawKeyWide("LMB", x, clickY, Mouse.isButtonDown(0), (KEY_SIZE * 1.5F));
        drawKeyWide("RMB", x + KEY_SIZE * 1.5F + GAP, clickY, Mouse.isButtonDown(1), (KEY_SIZE * 1.5F));
    }

    private void drawKey(String label, float x, float y, boolean active) {
        drawKeyWide(label, x, y, active, KEY_SIZE);
    }

    private void drawKeyWide(String label, float x, float y, boolean active, float width) {
        int bg = active ? RenderUtils.THEME_YELLOW : RenderUtils.THEME_BLACK_TRANSLUCENT;
        int fg = active ? RenderUtils.THEME_BLACK : RenderUtils.THEME_YELLOW;
        RenderUtils.drawRoundedPanel(x, y, width, KEY_SIZE, bg, 2);
        RenderUtils.drawOutline(x, y, width, KEY_SIZE, RenderUtils.THEME_YELLOW);
        int textW = RenderUtils.getStringWidth(label);
        RenderUtils.drawString(label, x + (width - textW) / 2F, y + (KEY_SIZE - 8) / 2F, fg, false);
    }
}

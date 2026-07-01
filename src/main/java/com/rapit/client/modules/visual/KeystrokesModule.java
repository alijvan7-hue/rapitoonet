package com.rapit.client.modules.visual;

import com.rapit.client.animation.AnimatedValue;
import com.rapit.client.animation.Easing;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.Map;

/**
 * Classic WASD + mouse-buttons keystroke display. Each key panel has
 * its own AnimatedValue driving a small scale "bounce" and a glow
 * intensity that eases in on press and back out on release, instead
 * of instantly snapping between two flat colors.
 */
public class KeystrokesModule extends Module {

    private static final int KEY_SIZE = 18;
    private static final int GAP = 2;

    private final Map<String, AnimatedValue> pressAnim = new HashMap<>();

    public KeystrokesModule() {
        super("Keystrokes", "Shows WASD/mouse key presses with a press animation", ModuleCategory.VISUAL, Module.KEY_NONE, true);
        setHudPosition(4, 200);
    }

    private AnimatedValue animFor(String key) {
        return pressAnim.computeIfAbsent(key, k -> {
            AnimatedValue value = new AnimatedValue(0F);
            value.setEasing(Easing::easeOutBack);
            return value;
        });
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
        drawKeyWide("LMB", x, clickY, Mouse.isButtonDown(0), KEY_SIZE * 1.5F);
        drawKeyWide("RMB", x + KEY_SIZE * 1.5F + GAP, clickY, Mouse.isButtonDown(1), KEY_SIZE * 1.5F);
    }

    private void drawKey(String label, float x, float y, boolean active) {
        drawKeyWide(label, x, y, active, KEY_SIZE);
    }

    private void drawKeyWide(String label, float x, float y, boolean active, float width) {
        AnimatedValue anim = animFor(label);
        anim.animateTo(active ? 1F : 0F, active ? 120 : 200);
        float progress = anim.update();

        // Scale bounce: keys grow slightly past 1.0 on press (easeOutBack
        // overshoots), then settle - reads as a tactile "press" instead
        // of a flat color swap.
        float scale = 1.0F + progress * 0.08F;
        float glowAlpha = progress;

        float centerX = x + width / 2F;
        float centerY = y + KEY_SIZE / 2F;
        float scaledWidth = width * scale;
        float scaledHeight = KEY_SIZE * scale;
        float drawX = centerX - scaledWidth / 2F;
        float drawY = centerY - scaledHeight / 2F;

        // Glow: a soft oversized yellow rect behind the key that fades
        // in with press progress, then the solid key panel on top.
        if (glowAlpha > 0.01F) {
            int glowColor = ((int) (glowAlpha * 90) << 24) | 0x00FFD400;
            RenderUtils.drawRoundedPanel(drawX - 2, drawY - 2, scaledWidth + 4, scaledHeight + 4, glowColor, 4);
        }

        int bg = lerpColor(RenderUtils.THEME_BLACK_TRANSLUCENT, RenderUtils.THEME_YELLOW, glowAlpha);
        int fg = lerpColor(RenderUtils.THEME_YELLOW, RenderUtils.THEME_BLACK, glowAlpha);

        RenderUtils.drawRoundedPanel(drawX, drawY, scaledWidth, scaledHeight, bg, 2);
        RenderUtils.drawOutline(drawX, drawY, scaledWidth, scaledHeight, RenderUtils.THEME_YELLOW);

        int textW = RenderUtils.getStringWidth(label);
        RenderUtils.drawString(label, centerX - textW / 2F, centerY - 4, fg, false);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Easing.clamp01(t);
        int fa = (from >> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int ta = (to >> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int a = (int) (fa + (ta - fa) * t);
        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

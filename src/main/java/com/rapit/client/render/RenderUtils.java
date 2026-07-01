package com.rapit.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Small collection of GL11-based drawing helpers used by both the
 * HUD modules and the ClickGUI. Kept dependency-free (no external
 * shader libs) so it works out of the box on vanilla LWJGL that
 * ships with Forge 1.8.9.
 */
public final class RenderUtils {

    // Rapit Client theme colors.
    public static final int THEME_BLACK = 0xFF111111;
    public static final int THEME_YELLOW = 0xFFFFD400;
    public static final int THEME_BLACK_TRANSLUCENT = 0xB0111111;

    private RenderUtils() {
    }

    public static FontRenderer font() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    /** Flat rectangle, alpha-aware. */
    public static void drawRect(float x, float y, float width, float height, int color) {
        float a = (color >> 24 & 255) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Approximated rounded rectangle. True per-pixel rounding needs a
     * shader; this draws a flat panel plus small corner "notches" so
     * it reads as rounded at typical HUD/GUI sizes without requiring
     * a custom fragment shader.
     */
    public static void drawRoundedPanel(float x, float y, float width, float height, int color, float radius) {
        drawRect(x + radius, y, width - radius * 2, height, color);
        drawRect(x, y + radius, radius, height - radius * 2, color);
        drawRect(x + width - radius, y + radius, radius, height - radius * 2, color);
        drawRect(x + radius, y, width - radius * 2, radius, color);
        drawRect(x + radius, y + height - radius, width - radius * 2, radius, color);
        drawRect(x + radius, y + radius, width - radius * 2, height - radius * 2, color);
    }

    /** 1px outline, used for the yellow accent border on panels. */
    public static void drawOutline(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, 1, color);
        drawRect(x, y + height - 1, width, 1, color);
        drawRect(x, y, 1, height, color);
        drawRect(x + width - 1, y, 1, height, color);
    }

    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        if (shadow) {
            font().drawStringWithShadow(text, (int) x, (int) y, color);
        } else {
            font().drawString(text, (int) x, (int) y, color);
        }
    }

    public static int getStringWidth(String text) {
        return font().getStringWidth(text);
    }
}

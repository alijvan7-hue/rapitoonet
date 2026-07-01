package com.rapit.client.gui;

import com.rapit.client.RapitClient;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.module.ModuleManager;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * "UI Edit Mode": lets the player click-drag any movable (HUD
 * category) module's on-screen anchor to reposition it, and
 * click-drag a small handle at the bottom-right of its bounding box
 * to resize it (adjusts Module.hudScale). Reuses each module's own
 * onRenderOverlay so what you see while editing matches gameplay
 * exactly - no separate "preview" representation to keep in sync.
 * Positions, scale, and opacity all persist through ConfigManager
 * like any other module setting.
 */
public class HudEditorGui extends GuiScreen {

    private static final float BOX_WIDTH = 62;
    private static final float BOX_HEIGHT = 14;
    private static final float HANDLE_SIZE = 6;

    private Module dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private Module resizing;
    private float resizeStartDistance;
    private float resizeStartScale;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> hudModules = manager.getModulesByCategory(ModuleCategory.HUD);

        RenderUtils.drawString("UI Edit Mode - drag to move, corner handle to resize, Esc to exit",
                8, 8, RenderUtils.THEME_YELLOW, true);

        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
        for (Module module : hudModules) {
            if (!module.isEnabled()) {
                continue;
            }
            module.onRenderOverlay(sr);

            float x = module.getHudX();
            float y = module.getHudY();
            float w = BOX_WIDTH * module.getHudScale();
            float h = BOX_HEIGHT * module.getHudScale();

            boolean active = module == dragging || module == resizing;
            int outlineColor = active ? RenderUtils.THEME_YELLOW : 0x80FFD400;
            RenderUtils.drawOutline(x - 2, y - 2, w, h, outlineColor);

            // Resize handle: small filled square at the bottom-right corner.
            float handleX = x - 2 + w - HANDLE_SIZE;
            float handleY = y - 2 + h - HANDLE_SIZE;
            RenderUtils.drawRect(handleX, handleY, HANDLE_SIZE, HANDLE_SIZE, RenderUtils.THEME_YELLOW);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        ModuleManager manager = RapitClient.instance.getModuleManager();
        for (Module module : manager.getModulesByCategory(ModuleCategory.HUD)) {
            if (!module.isEnabled()) {
                continue;
            }
            float x = module.getHudX();
            float y = module.getHudY();
            float w = BOX_WIDTH * module.getHudScale();
            float h = BOX_HEIGHT * module.getHudScale();

            float handleX = x - 2 + w - HANDLE_SIZE;
            float handleY = y - 2 + h - HANDLE_SIZE;

            // Corner resize handle takes priority over the general drag area.
            if (mouseX >= handleX && mouseX <= handleX + HANDLE_SIZE
                    && mouseY >= handleY && mouseY <= handleY + HANDLE_SIZE) {
                resizing = module;
                resizeStartDistance = distanceFromAnchor(module, mouseX, mouseY);
                resizeStartScale = module.getHudScale();
                return;
            }

            if (mouseX >= x - 2 && mouseX <= x - 2 + w && mouseY >= y - 2 && mouseY <= y - 2 + h) {
                dragging = module;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return;
            }
        }
    }

    private float distanceFromAnchor(Module module, float mouseX, float mouseY) {
        float dx = mouseX - module.getHudX();
        float dy = mouseY - module.getHudY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
        resizing = null;
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int mouseX = org.lwjgl.input.Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - org.lwjgl.input.Mouse.getEventY() * height / mc.displayHeight - 1;

        if (dragging != null) {
            dragging.setHudPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        } else if (resizing != null && resizeStartDistance > 1F) {
            float currentDistance = distanceFromAnchor(resizing, mouseX, mouseY);
            float scale = resizeStartScale * (currentDistance / resizeStartDistance);
            resizing.setHudScale(scale);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

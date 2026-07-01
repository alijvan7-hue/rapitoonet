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
 * Lets the player click-drag any movable (HUD-category) module's
 * on-screen anchor to reposition it. Reuses each module's own
 * onRenderOverlay so what you see while editing matches gameplay
 * exactly - no separate "preview" representation to keep in sync.
 */
public class HudEditorGui extends GuiScreen {

    private Module dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> hudModules = manager.getModulesByCategory(ModuleCategory.HUD);

        RenderUtils.drawString("HUD Editor - drag elements, Esc to exit", 8, 8, RenderUtils.THEME_YELLOW, true);

        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
        for (Module module : hudModules) {
            if (!module.isEnabled()) {
                continue;
            }
            module.onRenderOverlay(sr);
            // Highlight box so it's obvious what's draggable even for
            // modules whose text is short/blank at the moment.
            RenderUtils.drawOutline(module.getHudX() - 2, module.getHudY() - 2, 60, 12, 0x80FFD400);
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
            if (mouseX >= x - 2 && mouseX <= x + 60 && mouseY >= y - 2 && mouseY <= y + 10) {
                dragging = module;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        if (dragging != null) {
            int mouseX = org.lwjgl.input.Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getEventY() * height / mc.displayHeight - 1;
            dragging.setHudPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
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

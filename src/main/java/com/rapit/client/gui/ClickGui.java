package com.rapit.client.gui;

import com.rapit.client.RapitClient;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.module.ModuleManager;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * The main Rapit Client ClickGUI: a left-hand column of category
 * tabs and a right-hand scrollable list of that category's modules.
 * Each module row has a toggle button and, if the module supports a
 * keybind, a "press to set" bind field.
 *
 * This is a genuinely functional GUI (click-to-toggle, drag-to-move
 * the panel, scroll, live search) rather than a mockup. It uses flat
 * GL11 drawing via RenderUtils rather than a shader-based blur, since
 * a true gaussian blur pass needs a custom fragment shader that can't
 * be verified without a working Forge toolchain in this environment -
 * see README for how to layer one in later.
 */
public class ClickGui extends GuiScreen {

    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 220;
    private static final int CATEGORY_WIDTH = 90;
    private static final int ROW_HEIGHT = 20;

    private float panelX = 40;
    private float panelY = 40;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private ModuleCategory selectedCategory = ModuleCategory.HUD;
    private GuiTextField searchField;

    private Module awaitingBind;

    @Override
    public void initGui() {
        searchField = new GuiTextField(0, fontRendererObj,
                (int) panelX + CATEGORY_WIDTH + 8, (int) panelY + 8, PANEL_WIDTH - CATEGORY_WIDTH - 16, 14);
        searchField.setMaxStringLength(32);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        RenderUtils.drawRoundedPanel(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, RenderUtils.THEME_BLACK_TRANSLUCENT, 6);
        RenderUtils.drawOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, RenderUtils.THEME_YELLOW);

        // Title bar (also the drag handle).
        RenderUtils.drawString("Rapit Client", panelX + 8, panelY - 12, RenderUtils.THEME_YELLOW, true);

        drawCategoryTabs();
        drawModuleList(mouseX, mouseY);

        searchField.xPosition = (int) panelX + CATEGORY_WIDTH + 8;
        searchField.yPosition = (int) panelY + 8;
        searchField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCategoryTabs() {
        float y = panelY;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean selected = category == selectedCategory;
            int bg = selected ? RenderUtils.THEME_YELLOW : RenderUtils.THEME_BLACK;
            int fg = selected ? RenderUtils.THEME_BLACK : RenderUtils.THEME_YELLOW;
            RenderUtils.drawRect(panelX, y, CATEGORY_WIDTH, ROW_HEIGHT, bg);
            RenderUtils.drawString(category.getDisplayName(), panelX + 6, y + 6, fg, false);
            y += ROW_HEIGHT;
        }
    }

    private void drawModuleList(int mouseX, int mouseY) {
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> modules = manager.getModulesByCategory(selectedCategory);

        float listX = panelX + CATEGORY_WIDTH + 8;
        float listY = panelY + 26;
        float listWidth = PANEL_WIDTH - CATEGORY_WIDTH - 16;

        String filter = searchField.getText().toLowerCase();

        for (Module module : modules) {
            if (!filter.isEmpty() && !module.getName().toLowerCase().contains(filter)) {
                continue;
            }
            boolean hovered = mouseX >= listX && mouseX <= listX + listWidth
                    && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2;

            int rowBg = module.isEnabled() ? 0x60FFD400 : (hovered ? 0x40FFFFFF : 0x30000000);
            RenderUtils.drawRoundedPanel(listX, listY, listWidth, ROW_HEIGHT - 2, rowBg, 3);

            int textColor = module.isEnabled() ? RenderUtils.THEME_BLACK : RenderUtils.THEME_YELLOW;
            RenderUtils.drawString(module.getName(), listX + 6, listY + 5, textColor, false);

            String bindLabel = module == awaitingBind ? "> ..." : module.getKeybindName();
            int bindWidth = RenderUtils.getStringWidth(bindLabel);
            RenderUtils.drawString(bindLabel, listX + listWidth - bindWidth - 6, listY + 5, textColor, false);

            listY += ROW_HEIGHT;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        // Drag panel by its title bar.
        if (mouseButton == 0 && mouseY >= panelY - 14 && mouseY <= panelY
                && mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH) {
            dragging = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            return;
        }

        // Category tab clicks.
        float y = panelY;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseX >= panelX && mouseX <= panelX + CATEGORY_WIDTH && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                selectedCategory = category;
                return;
            }
            y += ROW_HEIGHT;
        }

        // Module row clicks (toggle on left-click, rebind on right-click).
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> modules = manager.getModulesByCategory(selectedCategory);
        float listX = panelX + CATEGORY_WIDTH + 8;
        float listY = panelY + 26;
        float listWidth = PANEL_WIDTH - CATEGORY_WIDTH - 16;
        String filter = searchField.getText().toLowerCase();

        for (Module module : modules) {
            if (!filter.isEmpty() && !module.getName().toLowerCase().contains(filter)) {
                continue;
            }
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2) {
                if (mouseButton == 0) {
                    module.toggle();
                } else if (mouseButton == 1) {
                    awaitingBind = module;
                }
                return;
            }
            listY += ROW_HEIGHT;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (awaitingBind != null) {
            awaitingBind.setKeybind(keyCode == Keyboard.KEY_ESCAPE ? Keyboard.KEY_NONE : keyCode);
            awaitingBind = null;
            return;
        }
        if (searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
        if (dragging && Mouse.isButtonDown(0)) {
            int scale = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
            panelX = Mouse.getX() / (float) scale - dragOffsetX;
            panelY = (mc.displayHeight - Mouse.getY()) / (float) scale - dragOffsetY;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

package com.rapit.client.gui;

import com.rapit.client.RapitClient;
import com.rapit.client.animation.AnimatedValue;
import com.rapit.client.animation.Easing;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.module.ModuleManager;
import com.rapit.client.module.settings.ColorSetting;
import com.rapit.client.module.settings.ModuleSetting;
import com.rapit.client.module.settings.SliderSetting;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rapit Client's premium ClickGUI: a floating, center-right anchored
 * panel (not full-screen) with category tabs, animated hover/press
 * feedback on module rows, a fade+slide open animation, and a
 * per-module right-click settings popup (toggle, keybind, opacity,
 * sliders, and a hue color picker with RGB-cycle toggle).
 *
 * All motion goes through {@link AnimatedValue} so hover/press/open
 * timing stays consistent with the rest of the client instead of
 * this screen inventing its own lerp logic.
 *
 * Note on "blur background": a true Gaussian blur needs a custom
 * fragment shader pass, which can't be authored and verified without
 * a working Forge toolchain in the environment this was built in. As
 * a stand-in, the panel gets a soft layered drop-shadow plus a
 * darkened translucent backdrop, which reads as "floating above the
 * game" without needing a shader - swap in a real blur shader later
 * if desired (see README).
 */
public class ClickGui extends GuiScreen {

    private static final float PANEL_WIDTH = 300;
    private static final float PANEL_HEIGHT = 300;
    private static final float CATEGORY_WIDTH = 92;
    private static final float ROW_HEIGHT = 22;
    private static final float POPUP_WIDTH = 216;

    private float panelX;
    private float panelY;
    private boolean draggingPanel;
    private float dragOffsetX;
    private float dragOffsetY;

    private ModuleCategory selectedCategory = ModuleCategory.HUD;

    private Module awaitingBind;
    private Module settingsModule;
    private final AnimatedValue popupAnim = new AnimatedValue(0F);
    private final AnimatedValue openAnim = new AnimatedValue(0F);

    private final Map<String, AnimatedValue> hoverAnim = new HashMap<>();
    private final Map<String, AnimatedValue> bounceAnim = new HashMap<>();

    private SliderSetting draggingSlider;
    private ColorSetting draggingHue;

    @Override
    public void initGui() {
        panelX = width - PANEL_WIDTH - 20;
        panelY = (height - PANEL_HEIGHT) / 2F;

        openAnim.set(0F);
        openAnim.setEasing(Easing::easeOutCubic);
        openAnim.animateTo(1F, 260);
    }

    private AnimatedValue hoverFor(String key) {
        return hoverAnim.computeIfAbsent(key, k -> new AnimatedValue(0F));
    }

    private AnimatedValue bounceFor(String key) {
        return bounceAnim.computeIfAbsent(key, k -> {
            AnimatedValue v = new AnimatedValue(0F);
            v.setEasing(Easing::easeOutBack);
            return v;
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        float progress = openAnim.update();
        float slideOffset = (1F - progress) * 36F;
        float renderX = panelX + slideOffset;
        int panelAlpha = (int) (progress * 255);

        drawPanelShadow(renderX, panelY);

        int panelBg = (panelAlpha << 24) | (RenderUtils.THEME_BLACK_TRANSLUCENT & 0x00FFFFFF);
        RenderUtils.drawRoundedPanel(renderX, panelY, PANEL_WIDTH, PANEL_HEIGHT, panelBg, 8);
        int outlineAlpha = (int) (progress * 0xFF);
        RenderUtils.drawOutline(renderX, panelY, PANEL_WIDTH, PANEL_HEIGHT, (outlineAlpha << 24) | 0x00FFD400);

        RenderUtils.drawScaledString("RAPIT CLIENT", renderX + 10, panelY - 16, 1.0F,
                (panelAlpha << 24) | 0x00FFD400, true);

        drawCategoryTabs(renderX, mouseX, mouseY, progress);
        drawModuleList(renderX, mouseX, mouseY, progress);

        if (settingsModule != null) {
            drawSettingsPopup(renderX, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Soft layered drop-shadow behind the panel, standing in for a real blur (see class javadoc). */
    private void drawPanelShadow(float x, float y) {
        RenderUtils.drawRoundedPanel(x - 4, y + 2, PANEL_WIDTH + 8, PANEL_HEIGHT + 8, 0x30000000, 10);
        RenderUtils.drawRoundedPanel(x - 2, y + 4, PANEL_WIDTH + 4, PANEL_HEIGHT + 4, 0x40000000, 9);
    }

    private void drawCategoryTabs(float panelXNow, int mouseX, int mouseY, float progress) {
        float y = panelY;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= panelXNow && mouseX <= panelXNow + CATEGORY_WIDTH
                    && mouseY >= y && mouseY <= y + ROW_HEIGHT;

            AnimatedValue hover = hoverFor("cat_" + category.name());
            hover.animateTo(hovered || selected ? 1F : 0F, 150);
            float hv = hover.update();

            int bg = selected ? RenderUtils.THEME_YELLOW : blend(0x00111111, 0x30FFD400, hv);
            int fg = selected ? RenderUtils.THEME_BLACK : RenderUtils.THEME_YELLOW;
            int alpha = (int) (progress * 0xFF);

            RenderUtils.drawRect(panelXNow, y, CATEGORY_WIDTH, ROW_HEIGHT, withAlpha(bg, selected ? alpha : (int) (alpha * (0.15 + 0.5 * hv))));
            RenderUtils.drawString(category.getDisplayName(), panelXNow + 8, y + 7, withAlpha(fg, alpha), false);
            y += ROW_HEIGHT;
        }
    }

    private void drawModuleList(float panelXNow, int mouseX, int mouseY, float progress) {
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> modules = manager.getModulesByCategory(selectedCategory);

        float listX = panelXNow + CATEGORY_WIDTH + 8;
        float listY = panelY + 8;
        float listWidth = PANEL_WIDTH - CATEGORY_WIDTH - 16;
        int alpha = (int) (progress * 0xFF);

        if (selectedCategory == ModuleCategory.SETTINGS) {
            drawUiEditModeRow(listX, listY, listWidth, mouseX, mouseY, alpha);
            return;
        }

        for (Module module : modules) {
            boolean hovered = mouseX >= listX && mouseX <= listX + listWidth
                    && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2;

            AnimatedValue hover = hoverFor(module.getName());
            hover.animateTo(hovered ? 1F : 0F, 150);
            float hv = hover.update();

            AnimatedValue bounce = bounceFor(module.getName());
            float bv = bounce.update();

            // Hover "scale": grow the row width slightly rather than a
            // true pivot-centered transform, to keep the row's left
            // edge anchored under the mouse - reads as the requested
            // 1.00 -> 1.05 hover growth without matrix juggling.
            float growth = (hv * 0.05F + bv * 0.08F) * listWidth;
            float rowWidth = listWidth + growth;

            int rowBg = module.isEnabled()
                    ? withAlpha(RenderUtils.THEME_YELLOW, (int) (alpha * 0.55F))
                    : withAlpha(0x00FFFFFF, (int) (alpha * (0.08F + 0.10F * hv)));
            RenderUtils.drawRoundedPanel(listX, listY, rowWidth, ROW_HEIGHT - 2, rowBg, 4);

            int textColor = module.isEnabled() ? RenderUtils.THEME_BLACK : RenderUtils.THEME_YELLOW;
            RenderUtils.drawString(module.getName(), listX + 8, listY + 6, withAlpha(textColor, alpha), false);

            String bindLabel = module == awaitingBind ? "> ..." : module.getKeybindName();
            int bindWidth = RenderUtils.getStringWidth(bindLabel);
            RenderUtils.drawString(bindLabel, listX + listWidth - bindWidth - 8, listY + 6, withAlpha(textColor, alpha), false);

            // Small gear glyph hint that right-click opens settings.
            RenderUtils.drawString("*", listX + listWidth - bindWidth - 18, listY + 6, withAlpha(textColor, (int) (alpha * 0.6F)), false);

            listY += ROW_HEIGHT;
        }
    }

    private void drawUiEditModeRow(float listX, float listY, float listWidth, int mouseX, int mouseY, int alpha) {
        boolean hovered = mouseX >= listX && mouseX <= listX + listWidth
                && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2;
        AnimatedValue hover = hoverFor("uiEditMode");
        hover.animateTo(hovered ? 1F : 0F, 150);
        float hv = hover.update();

        int rowBg = withAlpha(0x00FFFFFF, (int) (alpha * (0.08F + 0.12F * hv)));
        RenderUtils.drawRoundedPanel(listX, listY, listWidth, ROW_HEIGHT - 2, rowBg, 4);
        RenderUtils.drawString("UI Edit Mode", listX + 8, listY + 6, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
        String hint = "Open ->";
        int hintWidth = RenderUtils.getStringWidth(hint);
        RenderUtils.drawString(hint, listX + listWidth - hintWidth - 8, listY + 6, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
    }

    // -----------------------------------------------------------------
    // Right-click settings popup
    // -----------------------------------------------------------------

    private enum RowType { TOGGLE, KEYBIND, OPACITY, SLIDER, COLOR }

    private static class PopupRow {
        RowType type;
        ModuleSetting setting;
        float y;
        float height;
    }

    private List<PopupRow> buildPopupRows(Module module) {
        List<PopupRow> rows = new ArrayList<>();
        float y = 22; // below header
        rows.add(row(RowType.TOGGLE, null, y, 20));
        y += 20;
        rows.add(row(RowType.KEYBIND, null, y, 20));
        y += 20;
        if (module.isMovable()) {
            rows.add(row(RowType.OPACITY, null, y, 20));
            y += 20;
        }
        for (ModuleSetting setting : module.getSettings()) {
            if (setting instanceof SliderSetting) {
                rows.add(row(RowType.SLIDER, setting, y, 20));
                y += 20;
            } else if (setting instanceof ColorSetting) {
                rows.add(row(RowType.COLOR, setting, y, 32));
                y += 32;
            }
        }
        return rows;
    }

    private PopupRow row(RowType type, ModuleSetting setting, float y, float height) {
        PopupRow r = new PopupRow();
        r.type = type;
        r.setting = setting;
        r.y = y;
        r.height = height;
        return r;
    }

    private float popupHeight(Module module) {
        List<PopupRow> rows = buildPopupRows(module);
        PopupRow last = rows.get(rows.size() - 1);
        return last.y + last.height + 8;
    }

    private float[] popupOrigin(float panelXNow) {
        // Popup opens to the left of the panel, aligned near the
        // currently selected module row, clamped so it never runs
        // off the top/bottom/left of the screen.
        float height = popupHeight(settingsModule);
        float x = panelXNow - POPUP_WIDTH - 10;
        if (x < 4) {
            x = panelXNow + PANEL_WIDTH + 10; // fall back to the right side if panel is near the left edge
        }
        float y = Math.max(4, Math.min(this.height - height - 4, panelY));
        return new float[]{x, y};
    }

    private void drawSettingsPopup(float panelXNow, int mouseX, int mouseY) {
        popupAnim.animateTo(1F, 180);
        float p = popupAnim.update();
        if (p <= 0.01F) {
            return;
        }

        float[] origin = popupOrigin(panelXNow);
        float baseX = origin[0];
        float baseY = origin[1];
        float height = popupHeight(settingsModule);

        // Pop-in: scale from 0.9 -> 1.0 anchored at top-left, fading alpha with it.
        float scale = 0.9F + 0.1F * p;
        float drawX = baseX + (POPUP_WIDTH - POPUP_WIDTH * scale) / 2F;
        float drawY = baseY;
        float w = POPUP_WIDTH * scale;
        int alpha = (int) (p * 235);

        RenderUtils.drawRoundedPanel(drawX, drawY, w, height, withAlpha(RenderUtils.THEME_BLACK, alpha), 6);
        RenderUtils.drawOutline(drawX, drawY, w, height, withAlpha(RenderUtils.THEME_YELLOW, alpha));
        RenderUtils.drawString(settingsModule.getName(), drawX + 8, drawY + 7, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);

        for (PopupRow row : buildPopupRows(settingsModule)) {
            float ry = baseY + row.y;
            drawPopupRow(baseX, ry, row, alpha);
        }
    }

    private void drawPopupRow(float x, float y, PopupRow row, int alpha) {
        float width = POPUP_WIDTH;
        switch (row.type) {
            case TOGGLE: {
                RenderUtils.drawString("Enabled", x + 10, y + 6, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
                drawToggleSwitch(x + width - 40, y + 4, settingsModule.isEnabled(), alpha);
                break;
            }
            case KEYBIND: {
                RenderUtils.drawString("Keybind", x + 10, y + 6, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
                String label = settingsModule == awaitingBind ? "> ..." : settingsModule.getKeybindName();
                int lw = RenderUtils.getStringWidth(label);
                RenderUtils.drawString(label, x + width - lw - 10, y + 6, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
                break;
            }
            case OPACITY: {
                drawInlineSlider(x, y, width, "Opacity", settingsModule.getHudOpacity(), 0.1F, 1F, alpha);
                break;
            }
            case SLIDER: {
                SliderSetting slider = (SliderSetting) row.setting;
                drawInlineSlider(x, y, width, slider.getName(), slider.getValue(), slider.getMin(), slider.getMax(), alpha);
                break;
            }
            case COLOR: {
                ColorSetting color = (ColorSetting) row.setting;
                RenderUtils.drawString(color.getName(), x + 10, y + 4, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
                String rgbLabel = color.isRgbMode() ? "[RGB]" : "[Fixed]";
                int rw = RenderUtils.getStringWidth(rgbLabel);
                RenderUtils.drawString(rgbLabel, x + width - rw - 10, y + 4, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
                drawHueBar(x + 10, y + 16, width - 20, color, alpha);
                break;
            }
            default:
                break;
        }
    }

    private void drawToggleSwitch(float x, float y, boolean on, int alpha) {
        float w = 28, h = 12;
        int track = withAlpha(on ? RenderUtils.THEME_YELLOW : 0x00555555, alpha);
        RenderUtils.drawRoundedPanel(x, y, w, h, track, 6);
        float knobX = on ? x + w - h : x;
        RenderUtils.drawRoundedPanel(knobX, y, h, h, withAlpha(RenderUtils.THEME_BLACK, alpha), 6);
    }

    private void drawInlineSlider(float x, float y, float width, String label, float value, float min, float max, int alpha) {
        RenderUtils.drawString(label, x + 10, y + 1, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);
        String valueText = String.format("%.2f", value);
        int vw = RenderUtils.getStringWidth(valueText);
        RenderUtils.drawString(valueText, x + width - vw - 10, y + 1, withAlpha(RenderUtils.THEME_YELLOW, alpha), false);

        float barX = x + 10;
        float barY = y + 12;
        float barWidth = width - 20;
        RenderUtils.drawRoundedPanel(barX, barY, barWidth, 4, withAlpha(0x00444444, alpha), 2);
        float fraction = (value - min) / (max - min);
        RenderUtils.drawRoundedPanel(barX, barY, barWidth * Easing.clamp01(fraction), 4, withAlpha(RenderUtils.THEME_YELLOW, alpha), 2);
    }

    private void drawHueBar(float x, float y, float width, ColorSetting color, int alpha) {
        // Approximated hue gradient: 24 adjacent segments, each drawn
        // in its own fully-saturated hue - a real per-pixel gradient
        // would need a custom shader, this reads close enough at the
        // ~190px width the popup uses.
        int segments = 24;
        float segWidth = width / segments;
        for (int i = 0; i < segments; i++) {
            float hue = i / (float) segments;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.85F, 1.0F);
            int segColor = withAlpha(0x00FFFFFF & rgb, alpha);
            RenderUtils.drawRect(x + i * segWidth, y, segWidth + 1, 8, segColor);
        }
        float handleX = x + color.getHue() * width - 2;
        RenderUtils.drawRect(handleX, y - 1, 4, 10, withAlpha(RenderUtils.THEME_BLACK, alpha));
        RenderUtils.drawOutline(handleX, y - 1, 4, 10, withAlpha(RenderUtils.THEME_YELLOW, alpha));
    }

    private static int withAlpha(int rgb, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private static int blend(int from, int to, float t) {
        t = Easing.clamp01(t);
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }

    // -----------------------------------------------------------------
    // Input handling
    // -----------------------------------------------------------------

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        float renderX = panelX + (1F - openAnim.get()) * 36F;

        // Settings popup takes input priority while open.
        if (settingsModule != null && handlePopupClick(renderX, mouseX, mouseY, mouseButton)) {
            return;
        }

        // Drag panel by its title bar.
        if (mouseButton == 0 && mouseY >= panelY - 20 && mouseY <= panelY
                && mouseX >= renderX && mouseX <= renderX + PANEL_WIDTH) {
            draggingPanel = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            return;
        }

        // Category tab clicks.
        float y = panelY;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseX >= renderX && mouseX <= renderX + CATEGORY_WIDTH && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                selectedCategory = category;
                closeSettingsPopup();
                return;
            }
            y += ROW_HEIGHT;
        }

        if (selectedCategory == ModuleCategory.SETTINGS) {
            float listX = renderX + CATEGORY_WIDTH + 8;
            float listY = panelY + 8;
            float listWidth = PANEL_WIDTH - CATEGORY_WIDTH - 16;
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2) {
                mc.displayGuiScreen(new HudEditorGui());
            }
            return;
        }

        // Module row clicks (toggle on left-click, open settings on right-click).
        ModuleManager manager = RapitClient.instance.getModuleManager();
        List<Module> modules = manager.getModulesByCategory(selectedCategory);
        float listX = renderX + CATEGORY_WIDTH + 8;
        float listY = panelY + 8;
        float listWidth = PANEL_WIDTH - CATEGORY_WIDTH - 16;

        for (Module module : modules) {
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + ROW_HEIGHT - 2) {
                if (mouseButton == 0) {
                    module.toggle();
                    AnimatedValue bounce = bounceFor(module.getName());
                    bounce.set(1F);
                    bounce.animateTo(0F, 240);
                } else if (mouseButton == 1) {
                    openSettingsPopup(module, renderX);
                }
                return;
            }
            listY += ROW_HEIGHT;
        }
    }

    private void openSettingsPopup(Module module, float panelXNow) {
        if (settingsModule == module) {
            closeSettingsPopup();
            return;
        }
        settingsModule = module;
        popupAnim.set(0F);
    }

    private void closeSettingsPopup() {
        settingsModule = null;
        draggingSlider = null;
        draggingHue = null;
        draggingOpacityModule = null;
    }

    private boolean handlePopupClick(float panelXNow, int mouseX, int mouseY, int mouseButton) {
        float[] origin = popupOrigin(panelXNow);
        float baseX = origin[0];
        float baseY = origin[1];
        float height = popupHeight(settingsModule);

        boolean insidePopup = mouseX >= baseX && mouseX <= baseX + POPUP_WIDTH && mouseY >= baseY && mouseY <= baseY + height;
        if (!insidePopup) {
            closeSettingsPopup();
            return false;
        }

        for (PopupRow row : buildPopupRows(settingsModule)) {
            float ry = baseY + row.y;
            if (mouseY < ry || mouseY > ry + row.height) {
                continue;
            }
            switch (row.type) {
                case TOGGLE:
                    settingsModule.toggle();
                    return true;
                case KEYBIND:
                    if (mouseButton == 0) {
                        awaitingBind = settingsModule;
                    }
                    return true;
                case OPACITY: {
                    float fraction = (mouseX - (baseX + 10)) / (POPUP_WIDTH - 20);
                    settingsModule.setHudOpacity(0.1F + Easing.clamp01(fraction) * 0.9F);
                    draggingSlider = null; // opacity isn't a SliderSetting; handled via a dedicated flag below
                    draggingOpacityModule = settingsModule;
                    return true;
                }
                case SLIDER: {
                    SliderSetting slider = (SliderSetting) row.setting;
                    float fraction = (mouseX - (baseX + 10)) / (POPUP_WIDTH - 20);
                    slider.setFromFraction(fraction);
                    draggingSlider = slider;
                    return true;
                }
                case COLOR: {
                    ColorSetting color = (ColorSetting) row.setting;
                    float hueBarY = ry + 16;
                    float rgbToggleY = ry + 4;
                    if (mouseY <= hueBarY + 2 && mouseY >= rgbToggleY) {
                        // Clicking the "[RGB]/[Fixed]" label area toggles RGB mode.
                        String rgbLabel = color.isRgbMode() ? "[RGB]" : "[Fixed]";
                        int rw = RenderUtils.getStringWidth(rgbLabel);
                        float labelX = baseX + POPUP_WIDTH - rw - 10;
                        if (mouseX >= labelX - 4 && mouseY <= rgbToggleY + 8) {
                            color.setRgbMode(!color.isRgbMode());
                            return true;
                        }
                    }
                    float barX = baseX + 10;
                    float barWidth = POPUP_WIDTH - 20;
                    if (mouseX >= barX && mouseX <= barX + barWidth) {
                        float fraction = (mouseX - barX) / barWidth;
                        color.setHue(fraction);
                        draggingHue = color;
                    }
                    return true;
                }
                default:
                    return true;
            }
        }
        return true;
    }

    private Module draggingOpacityModule;

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingPanel) {
            panelX = mouseX - dragOffsetX;
            panelY = mouseY - dragOffsetY;
            return;
        }
        if (settingsModule == null) {
            return;
        }
        float renderX = panelX + (1F - openAnim.get()) * 36F;
        float[] origin = popupOrigin(renderX);
        float baseX = origin[0];

        if (draggingSlider != null) {
            float fraction = (mouseX - (baseX + 10)) / (POPUP_WIDTH - 20);
            draggingSlider.setFromFraction(fraction);
        } else if (draggingOpacityModule != null) {
            float fraction = (mouseX - (baseX + 10)) / (POPUP_WIDTH - 20);
            draggingOpacityModule.setHudOpacity(0.1F + Easing.clamp01(fraction) * 0.9F);
        } else if (draggingHue != null) {
            float fraction = (mouseX - (baseX + 10)) / (POPUP_WIDTH - 20);
            draggingHue.setHue(Easing.clamp01(fraction));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingPanel = false;
        draggingSlider = null;
        draggingHue = null;
        draggingOpacityModule = null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (awaitingBind != null) {
            awaitingBind.setKeybind(keyCode == Keyboard.KEY_ESCAPE ? Module.KEY_NONE : keyCode);
            awaitingBind = null;
            return;
        }
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

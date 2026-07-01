package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

/**
 * Draws the exact stack count in the currently held item's hotbar
 * slot in large yellow text, easier to read at a glance than
 * vanilla's small default number during fast PvP item swaps.
 */
public class ItemCounterModule extends Module {

    public ItemCounterModule() {
        super("Item Counter", "Large readable count for the held item stack", ModuleCategory.VISUAL, Keyboard.KEY_NONE, true);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        if (mc.thePlayer == null) {
            return;
        }
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null || held.stackSize <= 1) {
            return;
        }
        String text = String.valueOf(held.stackSize);
        int x = sr.getScaledWidth() / 2 + 20;
        int y = sr.getScaledHeight() - 22;
        RenderUtils.drawString(text, x, y, RenderUtils.THEME_YELLOW, true);
    }
}

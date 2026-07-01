package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

/**
 * Renders a horizontal row of the player's four armor pieces near the
 * HUD, PvP-client style, instead of relying on vanilla's small armor
 * icons above the hotbar.
 *
 * Uses EntityPlayer.inventory.armorInventory (1.8.9 API) rather than
 * the EntityEquipmentSlot-based getItemStackFromSlot, which was only
 * introduced in Minecraft 1.9.
 */
public class ArmorHudModule extends Module {

    public ArmorHudModule() {
        super("Armor HUD", "Shows equipped armor pieces on screen", ModuleCategory.HUD, Keyboard.KEY_NONE, true);
        setHudPosition(4, 100);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        if (mc.thePlayer == null) {
            return;
        }

        // armorInventory is ordered [boots, leggings, chestplate, helmet];
        // reverse it so the row reads head-to-feet, left to right.
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;

        float x = getHudX();
        float y = getHudY();

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = armor.length - 1; i >= 0; i--) {
            ItemStack stack = armor[i];
            if (stack != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) x, (int) y);
            }
            x += 18;
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}

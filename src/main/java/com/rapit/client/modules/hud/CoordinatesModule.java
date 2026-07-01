package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

/** Shows the local player's rounded X/Y/Z position. */
public class CoordinatesModule extends Module {

    public CoordinatesModule() {
        super("Coordinates", "Shows your X/Y/Z position", ModuleCategory.HUD, Module.KEY_NONE, true);
        setHudPosition(4, 34);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        if (mc.thePlayer == null) {
            return;
        }
        String text = String.format("XYZ: %d / %d / %d",
                (int) Math.floor(mc.thePlayer.posX),
                (int) Math.floor(mc.thePlayer.posY),
                (int) Math.floor(mc.thePlayer.posZ));
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

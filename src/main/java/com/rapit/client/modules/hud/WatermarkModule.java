package com.rapit.client.modules.hud;

import com.rapit.client.RapitClient;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

/** Small branded "Rapit Client" watermark, top-left by default. */
public class WatermarkModule extends Module {

    public WatermarkModule() {
        super("Watermark", "Displays the Rapit Client watermark", ModuleCategory.HUD, Keyboard.KEY_NONE, true);
        setHudPosition(4, 84);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        String text = "Rapit Client " + RapitClient.VERSION;
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Shows the player's real-world local time (HH:mm:ss). */
public class ClockModule extends Module {

    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    public ClockModule() {
        super("Clock", "Shows the current real-world time", ModuleCategory.HUD, Module.KEY_NONE, true);
        setHudPosition(4, 54);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        String text = format.format(new Date());
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

/** Shows compass direction (N/NE/E/...) derived from player yaw. */
public class DirectionModule extends Module {

    private static final String[] DIRECTIONS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionModule() {
        super("Direction", "Shows the direction you're facing", ModuleCategory.HUD, Keyboard.KEY_NONE, true);
        setHudPosition(4, 44);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        if (mc.thePlayer == null) {
            return;
        }
        float yaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
        int index = MathHelper.floor_double((yaw / 45.0) + 0.5) & 7;
        String text = "Facing: " + DIRECTIONS[index] + " (" + Math.round(yaw) + ")";
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

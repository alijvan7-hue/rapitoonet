package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.NetHandlerPlayClient;
import org.lwjgl.input.Keyboard;

/**
 * Shows the local player's ping to the current server, read from the
 * tab-list network handler. Shows "N/A" in singleplayer.
 */
public class PingModule extends Module {

    public PingModule() {
        super("Ping", "Shows current server ping", ModuleCategory.HUD, Keyboard.KEY_NONE, true);
        setHudPosition(4, 24);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        String text = "Ping: N/A";
        if (mc.thePlayer != null && mc.getNetHandler() != null) {
            NetHandlerPlayClient handler = mc.getNetHandler();
            // Ping-per-player is exposed via NetworkPlayerInfo in the
            // player list; look it up for the local player.
            if (handler.getPlayerInfo(mc.thePlayer.getUniqueID()) != null) {
                int ping = handler.getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
                text = "Ping: " + ping + "ms";
            }
        }
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

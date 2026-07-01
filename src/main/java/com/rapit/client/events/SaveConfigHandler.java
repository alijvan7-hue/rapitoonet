package com.rapit.client.events;

import com.rapit.client.RapitClient;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Persists module state (enabled flags, keybinds, HUD positions)
 * whenever a world is unloaded - covers quitting to title, changing
 * worlds/servers, and disconnecting alike. This is used instead of a
 * client-side "game shutting down" event because 1.8.9's FML only
 * exposes FMLServerStoppingEvent, which never fires for the
 * integrated client on its own.
 */
public class SaveConfigHandler {

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        RapitClient.instance.saveConfig();
    }
}

package com.rapit.client.events;

import com.rapit.client.gui.ClickGui;
import com.rapit.client.gui.HudEditorGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Owns the two "meta" keybindings that aren't tied to any single
 * module: opening the ClickGUI and opening the HUD position editor.
 * Registered as real Forge KeyBindings (shows up in Controls options,
 * remappable there too) rather than raw Keyboard polling.
 */
public class KeybindHandler {

    public static final KeyBinding OPEN_CLICKGUI =
            new KeyBinding("key.rapitclient.clickgui", Keyboard.KEY_RSHIFT, "Rapit Client");
    public static final KeyBinding OPEN_HUD_EDITOR =
            new KeyBinding("key.rapitclient.hudeditor", Keyboard.KEY_H, "Rapit Client");

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_CLICKGUI);
        ClientRegistry.registerKeyBinding(OPEN_HUD_EDITOR);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) {
            return;
        }
        if (OPEN_CLICKGUI.isPressed()) {
            mc.displayGuiScreen(new ClickGui());
        } else if (OPEN_HUD_EDITOR.isPressed()) {
            mc.displayGuiScreen(new HudEditorGui());
        }
    }
}

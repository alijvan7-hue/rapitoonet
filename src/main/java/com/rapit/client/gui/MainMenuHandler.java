package com.rapit.client.gui;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Swaps vanilla's GuiMainMenu for RapitMainMenu the moment it would
 * otherwise open, so the whole game boots straight into the branded
 * menu without touching Minecraft's own class.
 */
public class MainMenuHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass() == GuiMainMenu.class) {
            event.gui = new RapitMainMenu();
        }
    }
}

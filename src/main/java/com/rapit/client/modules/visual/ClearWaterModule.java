package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Cuts underwater fog density to near-zero so water reads as clear
 * instead of murky. Registers/unregisters itself on the Forge event
 * bus directly since FogDensity is a Forge-specific event outside the
 * generic Module render hooks.
 */
public class ClearWaterModule extends Module {

    public ClearWaterModule() {
        super("Clear Water", "Removes underwater fog for clear visibility", ModuleCategory.VISUAL, Keyboard.KEY_NONE);
    }

    @Override
    protected void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (mc.thePlayer != null && mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water)) {
            event.setDensity(0.01F);
            event.setCanceled(true);
        }
    }
}

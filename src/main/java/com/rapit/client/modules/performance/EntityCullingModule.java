package com.rapit.client.modules.performance;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Skips render calls for living entities far outside a reasonable
 * distance of the camera, reducing GPU/CPU load on servers with many
 * mobs or players clumped together. Distance is intentionally
 * generous so it never culls anything the player could plausibly see
 * or fight.
 */
public class EntityCullingModule extends Module {

    private static final double MAX_RENDER_DISTANCE_SQ = 96 * 96;

    public EntityCullingModule() {
        super("Entity Culling", "Skips rendering far-away entities to save FPS", ModuleCategory.PERFORMANCE, Module.KEY_NONE, true);
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
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (shouldCull(event.entity)) {
            event.setCanceled(true);
        }
    }

    private boolean shouldCull(Entity entity) {
        if (mc.thePlayer == null || entity == mc.thePlayer) {
            return false;
        }
        double dx = entity.posX - mc.thePlayer.posX;
        double dy = entity.posY - mc.thePlayer.posY;
        double dz = entity.posZ - mc.thePlayer.posZ;
        return (dx * dx + dy * dy + dz * dz) > MAX_RENDER_DISTANCE_SQ;
    }
}

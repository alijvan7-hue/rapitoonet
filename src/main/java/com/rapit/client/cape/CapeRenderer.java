package com.rapit.client.cape;

import com.rapit.client.RapitClient;
import com.rapit.client.module.ModuleManager;
import com.rapit.client.modules.cosmetics.CapeModule;
import com.rapit.client.modules.cosmetics.CosmeticsToggleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Draws a purely client-side cosmetic cape on the local player,
 * independent of the vanilla Mojang cape system (which requires a
 * server-side account flag we don't control). Other players never
 * see this cape unless they also run Rapit Client - it's a local
 * visual layer only.
 *
 * Default texture is a plain dark cape (no logo). Ships at
 * assets/rapitclient/textures/cape/cape.png - swap the PNG to change
 * the design, no code changes required.
 *
 * Motion smoothing: rotation is driven off the entity's own
 * interpolated body yaw/pitch (same values vanilla uses for its own
 * player model) rather than a free-running timer, which is what was
 * causing visible jitter/drift relative to the player model before.
 */
public class CapeRenderer {

    private static final ResourceLocation CAPE_TEXTURE =
            new ResourceLocation(RapitClient.MODID, "textures/cape/cape.png");

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Entity rawEntity = event.entity;
        if (!(rawEntity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) rawEntity;
        Minecraft mc = Minecraft.getMinecraft();

        // Only the local player wears the Rapit cosmetic cape.
        if (mc.thePlayer == null || player != mc.thePlayer) {
            return;
        }

        ModuleManager modules = RapitClient.instance.getModuleManager();
        CapeModule cape = (CapeModule) modules.getByName("Cape");
        CosmeticsToggleModule master = (CosmeticsToggleModule) modules.getByName("Cosmetics");
        if (cape == null || master == null || !cape.isEnabled() || !master.isEnabled()) {
            return;
        }

        if (!(player instanceof AbstractClientPlayer)) {
            return;
        }

        float partialTicks = event.partialRenderTick;

        // Interpolated body yaw, matching how vanilla positions the
        // player model itself this frame - this is what keeps the
        // cape locked to the shoulders instead of swimming/jittering
        // relative to the body during turns.
        float bodyYaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        float headYaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float relativeYaw = headYaw - bodyYaw;

        // Gentle motion-driven sway: proportional to actual movement
        // speed (limbSwingAmount), not a free-running clock, so it
        // settles to a still hang the instant the player stops.
        float limbSwing = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
        float sway = MathHelper.clamp_float(limbSwing, 0F, 1F) * 6F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y, event.z);
        GlStateManager.rotate(-bodyYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, 1.36D, 0.16D);
        GlStateManager.rotate(sway, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F - relativeYaw * 0.2F, 0.0F, 1.0F, 0.0F);

        mc.getTextureManager().bindTexture(CAPE_TEXTURE);
        drawCapeQuad();

        GlStateManager.popMatrix();
    }

    /**
     * Draws a simple flat 10x16 cape plane (in 1/16th-block units)
     * using the 1.8.9 Tessellator/WorldRenderer API (VertexBuffer
     * did not exist yet; that's a 1.9+ rename of WorldRenderer).
     */
    private void drawCapeQuad() {
        float width = 0.625F;  // 10px / 16
        float height = 1.0F;   // 16px / 16

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-width / 2, 0, 0).tex(0, 0).endVertex();
        buffer.pos(width / 2, 0, 0).tex(1, 0).endVertex();
        buffer.pos(width / 2, height, 0).tex(1, 1).endVertex();
        buffer.pos(-width / 2, height, 0).tex(0, 1).endVertex();
        tessellator.draw();
    }
}

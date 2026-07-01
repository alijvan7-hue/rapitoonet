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
 * Texture lives at assets/rapitclient/textures/cape/cape.png. Ship
 * your own artwork there; see CapeModule's javadoc for the copyright
 * note on why no Garfield artwork is bundled.
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
        CapeModule cape = (CapeModule) modules.getByName("Garfield Cape");
        CosmeticsToggleModule master = (CosmeticsToggleModule) modules.getByName("Cosmetics");
        if (cape == null || master == null || !cape.isEnabled() || !master.isEnabled()) {
            return;
        }

        if (!(player instanceof AbstractClientPlayer)) {
            return;
        }

        float partialTicks = event.partialRenderTick;

        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y, event.z);

        // Position roughly at the shoulders, matching vanilla cape
        // anchor points, then let it swing slightly with movement.
        double swing = Math.sin((player.ticksExisted + partialTicks) * 0.2) * 0.05;
        GlStateManager.translate(0.0D, 1.35D, 0.15D);
        GlStateManager.rotate((float) Math.toDegrees(swing), 1.0F, 0.0F, 0.0F);

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

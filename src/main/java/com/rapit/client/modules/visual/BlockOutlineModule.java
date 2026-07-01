package com.rapit.client.modules.visual;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.module.settings.ColorSetting;
import com.rapit.client.module.settings.SliderSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Draws a customizable outline around whichever block the player is
 * currently looking at. Color is fully configurable via the
 * right-click settings panel (including an RGB cycling mode) and
 * line thickness via a slider. Uses depth-tested GL_LINE_STRIP boxes
 * (the same technique vanilla uses for its own selection outline) so
 * there's no flicker from z-fighting.
 */
public class BlockOutlineModule extends Module {

    private final ColorSetting color = new ColorSetting("Color", 0xFFFFD400);
    private final SliderSetting thickness = new SliderSetting("Thickness", 1F, 6F, 2F, 0.5F);

    public BlockOutlineModule() {
        super("Block Outline", "Highlights the block you're looking at", ModuleCategory.VISUAL, Module.KEY_NONE);
        addSetting(color);
        addSetting(thickness);
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
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        BlockPos pos = mc.objectMouseOver.blockPos;
        if (mc.theWorld.getBlockState(pos).getBlock().getMaterial()
                == net.minecraft.block.material.Material.air) {
            return;
        }

        double dx = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double dy = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double dz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(0.002, 0.002, 0.002);

        int argb = color.getAnimatedColor();
        float a = (argb >> 24 & 255) / 255F;
        float r = (argb >> 16 & 255) / 255F;
        float g = (argb >> 8 & 255) / 255F;
        float b = (argb & 255) / 255F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-dx, -dy, -dz);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableDepth();
        GL11.glLineWidth(thickness.getValue());
        GlStateManager.color(r, g, b, a);

        drawBoxOutline(box);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawBoxOutline(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glEnd();
    }
}

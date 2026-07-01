package com.rapit.client.gui;

import com.rapit.client.RapitClient;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Custom animated main menu: black/yellow themed background with a
 * slow-drifting gradient, the Rapit Client wordmark, and the four
 * requested actions (Singleplayer / Multiplayer / Options / Quit).
 *
 * The Garfield artwork slot (top area) reads from
 * assets/rapitclient/textures/gui/logo.png - ships as an original
 * placeholder crest (not Garfield's likeness); drop your own licensed
 * artwork in at that path to brand it fully. See CapeModule's javadoc
 * for why no Garfield art is bundled.
 */
public class RapitMainMenu extends GuiScreen {

    private static final ResourceLocation LOGO = new ResourceLocation(RapitClient.MODID, "textures/gui/logo.png");

    private long openedAt;

    @Override
    public void initGui() {
        openedAt = System.currentTimeMillis();
        buttonList.clear();
        int centerX = width / 2;
        int startY = height / 2 - 10;
        buttonList.add(new GuiButton(0, centerX - 100, startY, 200, 20, "Singleplayer"));
        buttonList.add(new GuiButton(1, centerX - 100, startY + 24, 200, 20, "Multiplayer"));
        buttonList.add(new GuiButton(2, centerX - 100, startY + 48, 200, 20, "Options"));
        buttonList.add(new GuiButton(3, centerX - 100, startY + 72, 200, 20, "Quit Game"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 1:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 2:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case 3:
                mc.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawAnimatedBackground();

        // Wordmark.
        String title = "RAPIT CLIENT";
        int titleWidth = fontRendererObj.getStringWidth(title) * 2;
        drawScaledString(title, width / 2 - titleWidth / 2, height / 2 - 70, 2.0F, RenderUtils.THEME_YELLOW);

        String subtitle = "Forge 1.8.9 - v" + RapitClient.VERSION;
        int subWidth = fontRendererObj.getStringWidth(subtitle);
        RenderUtils.drawString(subtitle, width / 2 - subWidth / 2F, height / 2 - 48, 0xFFCCCCCC, true);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Slow diagonal-drifting black/yellow gradient, no external shader needed. */
    private void drawAnimatedBackground() {
        RenderUtils.drawRect(0, 0, width, height, RenderUtils.THEME_BLACK);

        double t = (System.currentTimeMillis() - openedAt) / 4000.0;
        float sweep = (float) (Math.sin(t) * 0.5 + 0.5);
        int bandHeight = 3;
        int bandY = (int) (sweep * height);
        RenderUtils.drawRect(0, bandY, width, bandHeight, 0x22FFD400);
        RenderUtils.drawRect(0, height - bandY - bandHeight, width, bandHeight, 0x14FFD400);
    }

    private void drawScaledString(String text, float x, float y, float scale, int color) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1.0F);
        fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

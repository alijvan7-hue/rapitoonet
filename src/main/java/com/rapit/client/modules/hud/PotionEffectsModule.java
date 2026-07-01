package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;

import java.util.Collection;

/**
 * Compact text-based active-potion-effect list (name + remaining
 * seconds), positioned independently of vanilla's icon HUD so it can
 * be placed anywhere.
 */
public class PotionEffectsModule extends Module {

    public PotionEffectsModule() {
        super("Potion Effects", "Lists active potion effects with time left", ModuleCategory.HUD, Keyboard.KEY_NONE, true);
        setHudPosition(4, 64);
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        if (mc.thePlayer == null) {
            return;
        }
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects.isEmpty()) {
            return;
        }

        float y = getHudY();
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = potion != null ? potion.getName() : "Effect";
            int seconds = effect.getDuration() / 20;
            String text = name + " " + (effect.getAmplifier() + 1) + " (" + seconds + "s)";
            RenderUtils.drawString(text, getHudX(), y, RenderUtils.THEME_YELLOW, true);
            y += 10;
        }
    }
}

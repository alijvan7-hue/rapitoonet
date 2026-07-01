package com.rapit.client.module;

import com.rapit.client.config.ConfigManager;
import com.rapit.client.modules.cosmetics.CapeModule;
import com.rapit.client.modules.cosmetics.CosmeticsToggleModule;
import com.rapit.client.modules.hud.ArmorHudModule;
import com.rapit.client.modules.hud.ClockModule;
import com.rapit.client.modules.hud.CoordinatesModule;
import com.rapit.client.modules.hud.CpsCounterModule;
import com.rapit.client.modules.hud.DirectionModule;
import com.rapit.client.modules.hud.FpsCounterModule;
import com.rapit.client.modules.hud.PingModule;
import com.rapit.client.modules.hud.PotionEffectsModule;
import com.rapit.client.modules.hud.ToggleSprintModule;
import com.rapit.client.modules.hud.WatermarkModule;
import com.rapit.client.modules.performance.EntityCullingModule;
import com.rapit.client.modules.performance.FastGuiModule;
import com.rapit.client.modules.performance.ParticleLimiterModule;
import com.rapit.client.modules.visual.ClearWaterModule;
import com.rapit.client.modules.visual.FullbrightModule;
import com.rapit.client.modules.visual.ItemCounterModule;
import com.rapit.client.modules.visual.KeystrokesModule;
import com.rapit.client.modules.visual.ZoomModule;
import com.rapit.client.modules.visual.BlockOutlineModule;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owns the full list of modules and is the single point that listens
 * to Forge's event bus and fans events out to every registered
 * module's on* hooks. Also handles keybind presses.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void registerDefaultModules() {
        // --- HUD ---
        register(new FpsCounterModule());
        register(new CpsCounterModule());
        register(new PingModule());
        register(new CoordinatesModule());
        register(new DirectionModule());
        register(new ClockModule());
        register(new ArmorHudModule());
        register(new PotionEffectsModule());
        register(new ToggleSprintModule());
        register(new WatermarkModule());

        // --- Visual ---
        register(new FullbrightModule());
        register(new ZoomModule());
        register(new KeystrokesModule());
        register(new ClearWaterModule());
        register(new ItemCounterModule());
        register(new BlockOutlineModule());

        // --- Performance ---
        register(new EntityCullingModule());
        register(new ParticleLimiterModule());
        register(new FastGuiModule());

        // --- Cosmetics ---
        register(new CapeModule());
        register(new CosmeticsToggleModule());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) {
                result.add(m);
            }
        }
        return result;
    }

    public Module getByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public void applySavedStates(ConfigManager configManager) {
        configManager.applyTo(modules);
    }

    // ---------------------------------------------------------------
    // Forge event dispatch
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiIngameMenu) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module m : modules) {
            if (m.isEnabled()) {
                RenderUtils.setGlobalAlpha(m.getHudOpacity());
                float scale = m.getHudScale();
                if (scale != 1.0F) {
                    org.lwjgl.opengl.GL11.glPushMatrix();
                    // Scale around the element's own anchor point so
                    // resizing in UI Edit Mode grows/shrinks it in
                    // place instead of drifting toward the corner.
                    org.lwjgl.opengl.GL11.glTranslatef(m.getHudX(), m.getHudY(), 0F);
                    org.lwjgl.opengl.GL11.glScalef(scale, scale, 1F);
                    org.lwjgl.opengl.GL11.glTranslatef(-m.getHudX(), -m.getHudY(), 0F);
                    m.onRenderOverlay(sr);
                    org.lwjgl.opengl.GL11.glPopMatrix();
                } else {
                    m.onRenderOverlay(sr);
                }
            }
        }
        RenderUtils.setGlobalAlpha(1.0F);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        for (Module m : modules) {
            int bind = m.getKeybind();
            if (bind != Module.KEY_NONE && Keyboard.getEventKey() == bind && Keyboard.getEventKeyState()) {
                m.toggle();
            }
        }
    }
}

package com.rapit.client;

import com.rapit.client.cape.CapeRenderer;
import com.rapit.client.config.ConfigManager;
import com.rapit.client.events.KeybindHandler;
import com.rapit.client.events.SaveConfigHandler;
import com.rapit.client.gui.MainMenuHandler;
import com.rapit.client.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Rapit Client - Main mod entry point.
 *
 * Boots the module system, config manager, GUI hooks, and cosmetics
 * layer. Kept intentionally thin: all real logic lives in the
 * dedicated manager classes so this class stays readable.
 */
@Mod(modid = RapitClient.MODID, name = RapitClient.NAME, version = RapitClient.VERSION, clientSideOnly = true)
public class RapitClient {

    public static final String MODID = "rapitclient";
    public static final String NAME = "Rapit Client";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static RapitClient instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Config manager must exist before modules register so modules
        // can pull their saved keybind/enabled state on construction.
        configManager = new ConfigManager(event.getSuggestedConfigurationFile());
        configManager.load();
        KeybindHandler.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        moduleManager.registerDefaultModules();
        moduleManager.applySavedStates(configManager);

        // Register everything that listens to Forge's event bus.
        MinecraftForge.EVENT_BUS.register(moduleManager);
        MinecraftForge.EVENT_BUS.register(new MainMenuHandler());
        MinecraftForge.EVENT_BUS.register(new CapeRenderer());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new SaveConfigHandler());

        RapitLog.info("Rapit Client v" + VERSION + " initialized with "
                + moduleManager.getModules().size() + " modules.");
    }

    /**
     * Persists module state whenever a world is exited. There is no
     * clean client-side "game is shutting down" event in 1.8.9's FML
     * (FMLStoppingEvent only fires on the dedicated/integrated
     * server side, not for the client mod), so world-unload is the
     * most reliable point that fires on quit-to-title, world change,
     * and disconnect alike.
     */
    public void saveConfig() {
        if (moduleManager != null && configManager != null) {
            configManager.save(moduleManager);
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

package com.rapit.client.config;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleManager;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists each module's enabled/disabled state, keybind, and (for
 * HUD modules) screen position between game launches.
 *
 * Deliberately hand-rolled instead of pulling in Gson: Forge 1.8.9
 * already ships Gson on the classpath at runtime, but avoiding the
 * compile-time dependency keeps this class buildable even before the
 * ForgeGradle deobfuscated jar is available, and the format needed
 * here is trivial (flat key=value lines).
 */
public class ConfigManager {

    private final File configFile;
    private final Map<String, ModuleState> savedStates = new HashMap<>();

    public ConfigManager(File suggestedConfigFile) {
        // FML gives us "<config>/rapitclient.cfg" - reuse that path but
        // with our own simple format rather than Forge's Configuration API,
        // since we don't need categories/comments, just fast round-tripping.
        this.configFile = new File(suggestedConfigFile.getParentFile(), "rapitclient.properties");
    }

    public void load() {
        savedStates.clear();
        if (!configFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Format: ModuleName|enabled|keybind|hudX|hudY
                String[] parts = line.split("\\|");
                if (parts.length < 5) {
                    continue;
                }
                ModuleState state = new ModuleState();
                state.enabled = Boolean.parseBoolean(parts[1]);
                state.keybind = Integer.parseInt(parts[2]);
                state.hudX = Float.parseFloat(parts[3]);
                state.hudY = Float.parseFloat(parts[4]);
                savedStates.put(parts[0], state);
            }
        } catch (IOException e) {
            // Corrupt/missing config just falls back to module defaults.
        }
    }

    public void applyTo(List<Module> modules) {
        for (Module module : modules) {
            ModuleState state = savedStates.get(module.getName());
            if (state == null) {
                continue;
            }
            module.setKeybind(state.keybind);
            module.setHudPosition(state.hudX, state.hudY);
            module.setEnabled(state.enabled);
        }
    }

    public void save(ModuleManager moduleManager) {
        configFile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write("# Rapit Client config - auto-generated, safe to delete to reset.");
            writer.newLine();
            for (Module module : moduleManager.getModules()) {
                writer.write(module.getName() + "|"
                        + module.isEnabled() + "|"
                        + module.getKeybind() + "|"
                        + module.getHudX() + "|"
                        + module.getHudY());
                writer.newLine();
            }
        } catch (IOException e) {
            // Best-effort save; failing silently here is preferable to
            // crashing the client on world exit.
        }
    }

    private static class ModuleState {
        boolean enabled;
        int keybind;
        float hudX;
        float hudY;
    }
}

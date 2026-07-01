package com.rapit.client.config;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleManager;
import com.rapit.client.module.settings.ColorSetting;
import com.rapit.client.module.settings.ModuleSetting;
import com.rapit.client.module.settings.SliderSetting;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists each module's enabled/disabled state, keybind, HUD
 * position/opacity, and every right-click-panel setting (slider
 * values, color hue + RGB mode) between game launches.
 *
 * Deliberately hand-rolled instead of pulling in Gson: Forge 1.8.9
 * already ships Gson on the classpath at runtime, but avoiding the
 * compile-time dependency keeps this class buildable even before the
 * ForgeGradle deobfuscated jar is available, and the format needed
 * here is trivial (flat key=value lines).
 *
 * Line format:
 *   ModuleName|enabled|keybind|hudX|hudY|opacity|scale|setting1=value1;setting2=value2;...
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
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) {
                    continue;
                }
                ModuleState state = new ModuleState();
                state.enabled = Boolean.parseBoolean(parts[1]);
                state.keybind = Integer.parseInt(parts[2]);
                state.hudX = Float.parseFloat(parts[3]);
                state.hudY = Float.parseFloat(parts[4]);
                state.opacity = parts.length > 5 && !parts[5].isEmpty() ? Float.parseFloat(parts[5]) : 1.0F;
                state.scale = parts.length > 6 && !parts[6].isEmpty() ? Float.parseFloat(parts[6]) : 1.0F;
                if (parts.length > 7 && !parts[7].isEmpty()) {
                    for (String pair : parts[7].split(";")) {
                        int eq = pair.indexOf('=');
                        if (eq > 0) {
                            state.settingValues.put(pair.substring(0, eq), pair.substring(eq + 1));
                        }
                    }
                }
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
            module.setHudOpacity(state.opacity);
            module.setHudScale(state.scale);
            module.setEnabled(state.enabled);

            for (ModuleSetting setting : module.getSettings()) {
                String raw = state.settingValues.get(setting.getName());
                if (raw == null) {
                    continue;
                }
                try {
                    if (setting instanceof SliderSetting) {
                        ((SliderSetting) setting).setValue(Float.parseFloat(raw));
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting cs = (ColorSetting) setting;
                        String[] hueAndRgb = raw.split(",");
                        cs.setHue(Float.parseFloat(hueAndRgb[0]));
                        if (hueAndRgb.length > 1) {
                            cs.setRgbMode(Boolean.parseBoolean(hueAndRgb[1]));
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // Corrupt single value - keep the setting's default instead of crashing.
                }
            }
        }
    }

    public void save(ModuleManager moduleManager) {
        configFile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write("# Rapit Client config - auto-generated, safe to delete to reset.");
            writer.newLine();
            for (Module module : moduleManager.getModules()) {
                StringBuilder settingsPart = new StringBuilder();
                for (ModuleSetting setting : module.getSettings()) {
                    if (settingsPart.length() > 0) {
                        settingsPart.append(';');
                    }
                    if (setting instanceof SliderSetting) {
                        settingsPart.append(setting.getName()).append('=').append(((SliderSetting) setting).getValue());
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting cs = (ColorSetting) setting;
                        settingsPart.append(setting.getName()).append('=')
                                .append(cs.getHue()).append(',').append(cs.isRgbMode());
                    }
                }
                writer.write(module.getName() + "|"
                        + module.isEnabled() + "|"
                        + module.getKeybind() + "|"
                        + module.getHudX() + "|"
                        + module.getHudY() + "|"
                        + module.getHudOpacity() + "|"
                        + module.getHudScale() + "|"
                        + settingsPart);
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
        float opacity = 1.0F;
        float scale = 1.0F;
        final Map<String, String> settingValues = new HashMap<>();
    }
}

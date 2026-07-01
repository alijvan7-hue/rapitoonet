package com.rapit.client.modules.cosmetics;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Toggles the client-side cosmetic cape layer (see
 * {@link com.rapit.client.cape.CapeRenderer}). Enabled by default per
 * spec so the local player wears the cape out of the box.
 *
 * NOTE ON ARTWORK: the shipped texture
 * (assets/rapitclient/textures/cape/cape.png) is an original
 * placeholder, not the copyrighted Garfield character. Swap in your
 * own licensed artwork at that path if you want a specific design -
 * the renderer just reads whatever PNG lives there.
 */
public class CapeModule extends Module {

    public CapeModule() {
        super("Garfield Cape", "Displays a cosmetic cape on your local player", ModuleCategory.COSMETIC, Module.KEY_NONE, true);
    }
}

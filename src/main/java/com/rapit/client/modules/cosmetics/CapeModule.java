package com.rapit.client.modules.cosmetics;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;

/**
 * Toggles the client-side cosmetic cape layer (see
 * {@link com.rapit.client.cape.CapeRenderer}). Ships with a plain
 * dark cape and no logo by default - swap the PNG at
 * assets/rapitclient/textures/cape/cape.png for your own design.
 */
public class CapeModule extends Module {

    public CapeModule() {
        super("Cape", "Displays a cosmetic cape on your local player", ModuleCategory.COSMETIC, Module.KEY_NONE, true);
    }
}

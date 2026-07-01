package com.rapit.client.modules.hud;

import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * Makes sprint "sticky": once you start sprinting it stays on without
 * holding the sprint key, until you stop moving or toggle it off.
 * This module itself is always-on when enabled (no HUD text needed,
 * but it's a HUD/utility-category convenience toggle per spec).
 */
public class ToggleSprintModule extends Module {

    public ToggleSprintModule() {
        super("Toggle Sprint", "Sprint stays on without holding the key", ModuleCategory.HUD, Keyboard.KEY_R);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) {
            return;
        }
        boolean moving = mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;
        if (moving && !mc.thePlayer.isSprinting() && !mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.setSprinting(true);
        } else if (!moving && mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(false);
        }
    }

    @Override
    public boolean isMovable() {
        return false;
    }
}

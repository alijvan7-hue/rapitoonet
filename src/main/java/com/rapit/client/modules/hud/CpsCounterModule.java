package com.rapit.client.modules.hud;

import com.rapit.client.animation.AnimatedValue;
import com.rapit.client.module.Module;
import com.rapit.client.module.ModuleCategory;
import com.rapit.client.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

/**
 * Tracks left/right click rate over the last second ("clicks per
 * second"), the classic PvP-client HUD element. The displayed number
 * eases toward the real count via AnimatedValue instead of snapping
 * instantly, so it doesn't flicker distractingly during fast clicking.
 */
public class CpsCounterModule extends Module {

    private final long[] leftClickTimes = new long[64];
    private final long[] rightClickTimes = new long[64];
    private int leftIndex;
    private int rightIndex;
    private boolean lastLeftDown;
    private boolean lastRightDown;

    private final AnimatedValue displayedLeft = new AnimatedValue(0F);
    private final AnimatedValue displayedRight = new AnimatedValue(0F);

    public CpsCounterModule() {
        super("CPS Counter", "Shows left/right clicks per second", ModuleCategory.HUD, Module.KEY_NONE, true);
        setHudPosition(4, 14);
    }

    @Override
    public void onTick() {
        boolean leftDown = Mouse.isButtonDown(0);
        boolean rightDown = Mouse.isButtonDown(1);

        if (leftDown && !lastLeftDown) {
            leftClickTimes[leftIndex % leftClickTimes.length] = System.currentTimeMillis();
            leftIndex++;
        }
        if (rightDown && !lastRightDown) {
            rightClickTimes[rightIndex % rightClickTimes.length] = System.currentTimeMillis();
            rightIndex++;
        }
        lastLeftDown = leftDown;
        lastRightDown = rightDown;
    }

    private int countRecent(long[] times) {
        long now = System.currentTimeMillis();
        int count = 0;
        for (long t : times) {
            if (now - t <= 1000) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onRenderOverlay(ScaledResolution sr) {
        int lcps = countRecent(leftClickTimes);
        int rcps = countRecent(rightClickTimes);

        displayedLeft.animateTo(lcps, 150);
        displayedRight.animateTo(rcps, 150);
        int shownLeft = Math.round(displayedLeft.update());
        int shownRight = Math.round(displayedRight.update());

        String text = "CPS: " + shownLeft + " | " + shownRight;
        RenderUtils.drawString(text, getHudX(), getHudY(), RenderUtils.THEME_YELLOW, true);
    }
}

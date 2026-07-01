package com.rapit.client.animation;

/**
 * Central easing-function library used by every animated UI element
 * in Rapit Client (ClickGUI panels, module rows, keystrokes, HUD
 * fade/opacity). Keeping every easing curve in one place means
 * hover/press/fade animations across the whole client feel
 * consistent instead of each screen inventing its own timing.
 *
 * All functions take t in [0, 1] and return an eased value, also
 * generally in [0, 1] (bounce/back curves briefly overshoot past 1,
 * which is intentional - that's what gives the "bounce" feel).
 */
public final class Easing {

    private Easing() {
    }

    public static float linear(float t) {
        return t;
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5F ? 2F * t * t : 1F - (float) Math.pow(-2F * t + 2F, 2) / 2F;
    }

    public static float easeOutCubic(float t) {
        float f = t - 1F;
        return f * f * f + 1F;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5F ? 4F * t * t * t : 1F - (float) Math.pow(-2F * t + 2F, 3) / 2F;
    }

    /** Small overshoot-then-settle curve, used for button/press "bounce". */
    public static float easeOutBack(float t) {
        float c1 = 1.70158F;
        float c3 = c1 + 1F;
        float f = t - 1F;
        return 1F + c3 * f * f * f + c1 * f * f;
    }

    public static float clamp01(float value) {
        if (value < 0F) return 0F;
        if (value > 1F) return 1F;
        return value;
    }
}

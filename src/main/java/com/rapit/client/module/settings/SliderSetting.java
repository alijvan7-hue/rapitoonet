package com.rapit.client.module.settings;

/**
 * A numeric setting with a min/max/step range, rendered as a
 * draggable slider in the module settings popup (e.g. Block Outline
 * thickness, HUD element opacity).
 */
public class SliderSetting extends ModuleSetting {

    private final float min;
    private final float max;
    private final float step;
    private float value;

    public SliderSetting(String name, float min, float max, float defaultValue, float step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = defaultValue;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    public float getValue() {
        return value;
    }

    /** Sets the value from a raw 0..1 slider fraction, snapping to step. */
    public void setFromFraction(float fraction) {
        float raw = min + (max - min) * clamp01(fraction);
        if (step > 0) {
            raw = Math.round(raw / step) * step;
        }
        this.value = Math.max(min, Math.min(max, raw));
    }

    public float getFraction() {
        return (value - min) / (max - min);
    }

    public void setValue(float value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    private static float clamp01(float v) {
        return v < 0F ? 0F : (v > 1F ? 1F : v);
    }
}

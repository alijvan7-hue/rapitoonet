package com.rapit.client.module.settings;

/**
 * An ARGB color setting shown as a hue slider + RGB-cycle toggle in
 * the module settings popup. Manual mode picks a fully-saturated hue
 * from the slider; RGB mode ignores the manual hue and cycles
 * automatically over time. Alpha is fixed by the module's default
 * and not user-editable (keeps the picker to one control instead of
 * a full 4-channel picker).
 */
public class ColorSetting extends ModuleSetting {

    private final int alpha;
    private float manualHue;
    private boolean rgbMode;
    private float rgbPhase;

    public ColorSetting(String name, int defaultArgb) {
        super(name);
        this.alpha = (defaultArgb >>> 24) & 0xFF;
        float[] hsb = java.awt.Color.RGBtoHSB(
                (defaultArgb >> 16) & 0xFF, (defaultArgb >> 8) & 0xFF, defaultArgb & 0xFF, null);
        this.manualHue = hsb[0];
    }

    public float getHue() {
        return manualHue;
    }

    public void setHue(float hue) {
        this.manualHue = hue < 0F ? 0F : (hue > 1F ? 1F : hue);
    }

    public boolean isRgbMode() {
        return rgbMode;
    }

    public void setRgbMode(boolean rgbMode) {
        this.rgbMode = rgbMode;
    }

    /** Fixed color for the current mode, without advancing the RGB animation. */
    public int getColor() {
        float hue = rgbMode ? rgbPhase : manualHue;
        int rgb = java.awt.Color.HSBtoRGB(hue, 0.85F, 1.0F);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    /** Advances and returns the color; call once per frame from render code. */
    public int getAnimatedColor() {
        if (rgbMode) {
            rgbPhase += 0.004F;
            if (rgbPhase > 1F) {
                rgbPhase -= 1F;
            }
        }
        return getColor();
    }
}

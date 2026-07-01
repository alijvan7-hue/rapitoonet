package com.rapit.client.animation;

/**
 * A single float value that smoothly animates toward a target over a
 * given duration, using wall-clock time (not frame count) so motion
 * stays consistent regardless of FPS. This is the building block
 * every animated UI piece in the client uses: hover scale, panel
 * fade-in, slide-in offset, CPS counter smoothing, opacity sliders,
 * etc. Rather than hand-rolling lerp/timer logic per screen, widgets
 * just hold one of these per animated property and call update() +
 * get() each frame.
 */
public class AnimatedValue {

    private float from;
    private float to;
    private float current;
    private long startTime;
    private long durationMs;
    private EasingFunction easingFunction = Easing::easeOutCubic;

    public AnimatedValue(float initial) {
        this.from = initial;
        this.to = initial;
        this.current = initial;
        this.durationMs = 200;
        this.startTime = System.currentTimeMillis();
    }

    /** Begins animating from the current value to a new target. */
    public void animateTo(float target, long durationMs) {
        if (this.to == target) {
            return;
        }
        this.from = current;
        this.to = target;
        this.durationMs = Math.max(1, durationMs);
        this.startTime = System.currentTimeMillis();
    }

    public void setEasing(EasingFunction function) {
        this.easingFunction = function;
    }

    /** Recomputes the current value from elapsed time. Call once per frame before get(). */
    public float update() {
        long elapsed = System.currentTimeMillis() - startTime;
        float t = Easing.clamp01(elapsed / (float) durationMs);
        float eased = easingFunction.apply(t);
        current = from + (to - from) * eased;
        return current;
    }

    public float get() {
        return current;
    }

    public float getTarget() {
        return to;
    }

    /** Immediately snaps to a value with no animation (e.g. on GUI init). */
    public void set(float value) {
        this.from = value;
        this.to = value;
        this.current = value;
    }

    public boolean isAnimating() {
        return current != to;
    }

    @FunctionalInterface
    public interface EasingFunction {
        float apply(float t);
    }
}

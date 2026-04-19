package com.yourname.weather.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized animation and rendering utilities.
 * All Swing painting setup and animation timers go through here.
 */
public final class AnimationHelper {

    private AnimationHelper() {}

    /**
     * Returns a Graphics2D with high-quality rendering hints applied.
     * Always call g2.dispose() after use.
     */
    public static Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,      RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }

    /**
     * Creates a fade animation timer that adjusts a float alpha value.
     * The component's "customAlpha" client property is updated each tick.
     */
    public static Timer createFade(JComponent component, float from, float to,
                                   int delayMs, int steps, Runnable onFinish) {
        float[] current = {from};
        float step = (to - from) / Math.max(steps, 1);
        Timer timer = new Timer(delayMs, null);
        timer.addActionListener(e -> {
            current[0] += step;
            boolean done = step > 0 ? current[0] >= to : current[0] <= to;
            if (done) {
                current[0] = to;
                timer.stop();
                if (onFinish != null) onFinish.run();
            }
            component.putClientProperty("customAlpha", current[0]);
            component.repaint();
        });
        return timer;
    }

    /** Fade a component from 0 to 1 alpha over ~300ms. */
    public static void fadeIn(JComponent component) {
        Timer t = createFade(component, 0f, 1f, 14, 22, null);
        t.start();
    }

    /**
     * Horizontal shake animation — used for invalid input feedback.
     */
    public static void shake(Component component) {
        Point original = component.getLocation();
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        Timer timer = new Timer(25, null);
        int[] idx = {0};
        timer.addActionListener(e -> {
            if (idx[0] >= offsets.length) {
                timer.stop();
                component.setLocation(original);
                return;
            }
            component.setLocation(original.x + offsets[idx[0]++], original.y);
        });
        timer.start();
    }

    /**
     * Smooth hover color interpolation timer.
     * Returns a timer that interpolates hoverAlpha[0] toward target.
     */
    public static Timer createHoverFade(float[] hoverAlpha, boolean fadeIn,
                                        float step, JComponent repaintTarget) {
        Timer timer = new Timer(14, null);
        timer.addActionListener(e -> {
            if (fadeIn) {
                hoverAlpha[0] = Math.min(1f, hoverAlpha[0] + step);
                if (hoverAlpha[0] >= 1f) timer.stop();
            } else {
                hoverAlpha[0] = Math.max(0f, hoverAlpha[0] - step);
                if (hoverAlpha[0] <= 0f) timer.stop();
            }
            repaintTarget.repaint();
        });
        return timer;
    }

    /**
     * Draws a soft drop shadow behind a rounded rect.
     */
    public static void drawSoftShadow(Graphics2D g2, int x, int y, int w, int h,
                                      int radius, int layers) {
        for (int i = layers; i > 0; i--) {
            int alpha = (int)(18.0 * i / layers);
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRoundRect(x - i, y + i, w + i * 2, h + i * 2, radius + i, radius + i);
        }
    }
}
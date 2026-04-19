package com.yourname.weather.utils;

import com.yourname.weather.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable custom-painting helpers. Keeps paintComponent() methods clean and DRY.
 */
public final class RenderUtils {

    private RenderUtils() {}

    /** Fills a rounded rectangle with a solid color. */
    public static void fillRounded(Graphics2D g2, Color color, int x, int y, int w, int h, int r) {
        g2.setColor(color);
        g2.fillRoundRect(x, y, w, h, r, r);
    }

    /** Strokes a rounded rectangle border. */
    public static void strokeRounded(Graphics2D g2, Color color, int x, int y, int w, int h, int r) {
        g2.setColor(color);
        g2.drawRoundRect(x, y, w - 1, h - 1, r, r);
    }

    /** Draws a standard card: shadow + fill + border. */
    public static void paintCard(Graphics2D g2, int x, int y, int w, int h, int r, Color fill, Color border) {
        AnimationHelper.drawSoftShadow(g2, x, y, w, h, r, 5);
        fillRounded(g2, fill, x, y, w, h, r);
        strokeRounded(g2, border, x, y, w, h, r);
    }

    /**
     * Paints a vertical gradient, useful for atmosphere-inspired weather card backgrounds.
     */
    public static void paintGradient(Graphics2D g2, Color top, Color bottom,
                                     int x, int y, int w, int h, int r) {
        GradientPaint gp = new GradientPaint(x, y, top, x, y + h, bottom);
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, r, r);
    }

    /**
     * Centers text horizontally within bounds.
     */
    public static int centeredX(Graphics2D g2, String text, int containerWidth) {
        FontMetrics fm = g2.getFontMetrics();
        return (containerWidth - fm.stringWidth(text)) / 2;
    }

    /**
     * Creates a label with the given font, color, and horizontal alignment.
     */
    public static JLabel makeLabel(String text, Font font, Color color, int align) {
        JLabel label = new JLabel(text, align);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates a centered label.
     */
    public static JLabel makeCenteredLabel(String text, Font font, Color color) {
        return makeLabel(text, font, color, SwingConstants.CENTER);
    }

    /**
     * Applies a composite alpha to the graphics context (non-destructively).
     * Caller must restore with g2.setComposite(original) if needed.
     */
    public static void applyAlpha(Graphics2D g2, float alpha) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
    }
}
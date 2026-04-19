package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Compact, animated window control button (minimize / close).
 */
public class WindowButton extends JButton {

    public enum IconType { MINIMIZE, CLOSE }

    private final IconType iconType;
    private final Color hoverColor;
    private final float[] hoverAlpha = {0f};
    private Timer fadeTimer;
    private boolean inside = false;

    public WindowButton(IconType iconType, Color hoverColor, ActionListener action) {
        this.iconType  = iconType;
        this.hoverColor = hoverColor;

        Dimension d = new Dimension(UITheme.WIN_BTN_SIZE, UITheme.WIN_BTN_SIZE);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(action);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { inside = true;  fade(true);  }
            @Override public void mouseExited(MouseEvent e)  { inside = false; fade(false); }
        });
    }

    private void fade(boolean fadeIn) {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        fadeTimer = AnimationHelper.createHoverFade(hoverAlpha, fadeIn, 0.13f, this);
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = AnimationHelper.prepareGraphics(g);
        int w = getWidth(), h = getHeight();

        if (hoverAlpha[0] > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverAlpha[0]));
            g2.setColor(hoverColor);
            g2.fill(new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 6, 6));
            g2.setComposite(AlphaComposite.SrcOver);
        }

        g2.setColor(inside ? Color.WHITE : UITheme.TEXT_MUTED);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = w / 2, cy = h / 2;

        if (iconType == IconType.MINIMIZE) {
            g2.drawLine(cx - 4, cy + 1, cx + 4, cy + 1);
        } else {
            g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
            g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
        }
        g2.dispose();
    }
}
package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Single day forecast tile. Fixed 110×160 preferred size.
 * Hover triggers a subtle scale + border glow via repaint.
 */
public class ForecastCard extends JPanel {

    private final float[] hoverAlpha = {0f};
    private Timer hoverTimer;
    private boolean inside = false;

    private final JLabel dayLabel;
    private final JLabel iconLabel;
    private final JLabel tempRangeLabel;
    private final JLabel descLabel;
    private final JLabel popLabel;

    public ForecastCard() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(108, 158));
        setMaximumSize(new Dimension(108, 158));
        setMinimumSize(new Dimension(90, 140));

        dayLabel      = centered("",  UITheme.FONT_DAY,        UITheme.TEXT_PRIMARY);
        iconLabel     = centered("",  UITheme.FONT_DAY,        UITheme.TEXT_PRIMARY); // icon, no text
        tempRangeLabel = centered("", UITheme.FONT_FORECAST_T, UITheme.TEXT_PRIMARY);
        descLabel     = centered("",  new Font("Segoe UI", Font.PLAIN, 10), UITheme.TEXT_MUTED);
        popLabel      = centered("",  new Font("Segoe UI", Font.PLAIN, 10), UITheme.ACCENT);

        iconLabel.setPreferredSize(new Dimension(48, 48));
        iconLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        add(dayLabel);
        add(Box.createVerticalStrut(2));
        add(iconLabel);
        add(Box.createVerticalStrut(4));
        add(tempRangeLabel);
        add(Box.createVerticalStrut(2));
        add(descLabel);
        add(Box.createVerticalStrut(2));
        add(popLabel);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { inside = true;  fade(true);  }
            @Override public void mouseExited(MouseEvent e)  { inside = false; fade(false); }
        });
    }

    public void setForecast(WeatherService.DailyForecast f) {
        dayLabel.setText(f.dayOfWeek().substring(0, Math.min(3, f.dayOfWeek().length())));
        tempRangeLabel.setText(f.minTemp() + "° / " + f.maxTemp() + "°");
        descLabel.setText(cap(f.description()));
        popLabel.setText(f.pop() > 0 ? "☔ " + f.pop() + "%" : "");
        iconLabel.setIcon(f.icon() != null ? new ImageIcon(f.icon()) : null);
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = AnimationHelper.prepareGraphics(g);
        int w = getWidth(), h = getHeight();
        int r = UITheme.INNER_RADIUS;

        // Background
        g2.setColor(UITheme.BG_CARD);
        g2.fillRoundRect(0, 0, w, h, r, r);

        // Hover: accent border glow
        if (hoverAlpha[0] > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverAlpha[0] * 0.5f));
            g2.setColor(UITheme.ACCENT_GLOW);
            g2.fillRoundRect(0, 0, w, h, r, r);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // Border
        g2.setColor(inside && hoverAlpha[0] > 0.5f ? UITheme.ACCENT_DIM : UITheme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
        g2.dispose();
        super.paintComponent(g);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void fade(boolean in) {
        if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();
        hoverTimer = AnimationHelper.createHoverFade(hoverAlpha, in, 0.10f, this);
        hoverTimer.start();
    }

    private static JLabel centered(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
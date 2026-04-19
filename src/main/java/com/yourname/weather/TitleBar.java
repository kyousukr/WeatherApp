package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Undecorated window title bar. Handles drag-to-move and window controls.
 */
public class TitleBar extends JPanel {

    private final JFrame parent;
    private int dragOffsetX, dragOffsetY;

    public TitleBar(JFrame parent) {
        this.parent = parent;
        setOpaque(false);
        setPreferredSize(new Dimension(0, 36));
        setBorder(new EmptyBorder(4, 14, 0, 6));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("WEATHER");
        title.setFont(UITheme.FONT_WINDOW_TITLE);
        title.setForeground(UITheme.TEXT_MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controls.setOpaque(false);
        controls.add(new WindowButton(WindowButton.IconType.MINIMIZE, UITheme.BTN_MINIMIZE,
                e -> parent.setState(Frame.ICONIFIED)));
        controls.add(new WindowButton(WindowButton.IconType.CLOSE, UITheme.BTN_CLOSE,
                e -> parent.dispose()));

        add(title,    BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);

        // Drag-to-move
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragOffsetX = e.getXOnScreen() - parent.getX();
                dragOffsetY = e.getYOnScreen() - parent.getY();
            }
            @Override public void mouseDragged(MouseEvent e) {
                parent.setLocation(e.getXOnScreen() - dragOffsetX,
                        e.getYOnScreen() - dragOffsetY);
            }
        };
        addMouseListener(drag);
        addMouseMotionListener(drag);
        title.addMouseListener(drag);
        title.addMouseMotionListener(drag);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = AnimationHelper.prepareGraphics(g);
        g2.setColor(UITheme.TITLEBAR_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight() + UITheme.CORNER_RADIUS,
                UITheme.CORNER_RADIUS * 2, UITheme.CORNER_RADIUS * 2);
        g2.setColor(UITheme.BORDER);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
    }
}
package com.yourname.weather;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Single-line status indicator at the bottom of the window.
 * Shows loading, error, success, or default idle text.
 */
public class StatusBar extends JPanel {

    private final JLabel statusLabel;

    public StatusBar() {
        setOpaque(false);
        setBorder(new EmptyBorder(4, 0, 10, 0));
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        statusLabel = new JLabel("Введите название города");
        statusLabel.setFont(UITheme.FONT_STATUS);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        add(statusLabel);
    }

    public void setLoading()                { set("Загрузка...",    UITheme.ACCENT);   }
    public void setError(String msg)        { set(msg,              UITheme.ERROR);    }
    public void setSuccess(String msg)      { set(msg,              UITheme.SUCCESS);  }
    public void setDefault()                { set("Введите название города", UITheme.TEXT_MUTED); }

    private void set(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }
}
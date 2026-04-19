package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * Search row: rounded text field + accent search button.
 * Height is pinned so surrounding BoxLayout never distorts it.
 */
public class SearchPanel extends JPanel {

    private final RoundTextField cityField;
    private final JButton searchButton;

    public SearchPanel(ActionListener searchAction) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(4, 0, 10, 0));

        cityField = new RoundTextField();
        cityField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchAction.actionPerformed(null);
            }
        });

        searchButton = createSearchButton(searchAction);

        GridBagConstraints gcF = new GridBagConstraints();
        gcF.gridx = 0; gcF.gridy = 0;
        gcF.weightx = 1.0;
        gcF.fill = GridBagConstraints.HORIZONTAL;
        gcF.insets = new Insets(0, 0, 0, 6);
        add(cityField, gcF);

        GridBagConstraints gcB = new GridBagConstraints();
        gcB.gridx = 1; gcB.gridy = 0;
        gcB.anchor = GridBagConstraints.CENTER;
        add(searchButton, gcB);

        // Pin height so BoxLayout never squashes or stretches this panel
        int h = UITheme.FIELD_HEIGHT + 14;
        setMinimumSize(new Dimension(0, h));
        setPreferredSize(new Dimension(Short.MAX_VALUE, h));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    // ── Public ────────────────────────────────────────────────────────────────

    public String getCityText() {
        return cityField.getText().trim();
    }

    @Override
    public void setEnabled(boolean enabled) {
        cityField.setEnabled(enabled);
        searchButton.setEnabled(enabled);
    }

    public void requestFocusOnField() {
        cityField.requestFocusInWindow();
    }

    // ── Button builder ────────────────────────────────────────────────────────

    private JButton createSearchButton(ActionListener action) {
        // Resolve the icon once at construction time.
        // loadSearchIcon() returns null if the PNG is missing — the button
        // then draws a clean magnifier glyph instead of showing any text.
        final Image searchIcon = loadSearchIcon();

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = AnimationHelper.prepareGraphics(g);
                int w = getWidth(), h = getHeight();

                // Background
                boolean pressed  = getModel().isPressed();
                boolean rollover = getModel().isRollover();
                boolean disabled = !isEnabled();
                Color bg = disabled ? UITheme.ACCENT_DIM
                        : pressed  ? UITheme.ACCENT.darker()
                        : rollover ? UITheme.ACCENT_HOVER
                        : UITheme.ACCENT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, UITheme.SMALL_RADIUS, UITheme.SMALL_RADIUS);

                // Subtle top sheen
                if (!disabled) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(0, 0, w, h / 2, UITheme.SMALL_RADIUS, UITheme.SMALL_RADIUS);
                }

                // PNG icon, or hand-drawn magnifier when PNG is unavailable
                if (searchIcon != null) {
                    int iconSize = 18;
                    int ix = (w - iconSize) / 2;
                    int iy = (h - iconSize) / 2;
                    g2.drawImage(searchIcon, ix, iy, iconSize, iconSize, null);
                } else {
                    drawMagnifier(g2, w, h, disabled);
                }

                g2.dispose();
            }

            /** Pixel-perfect magnifier drawn with Java2D — no text, no artifacts. */
            private void drawMagnifier(Graphics2D g2, int w, int h, boolean disabled) {
                g2.setColor(disabled ? UITheme.TEXT_MUTED : Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = w / 2 - 1, cy = h / 2 - 2, r = 5;
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                // Handle
                g2.drawLine(cx + r - 1, cy + r - 1, cx + r + 3, cy + r + 3);
            }
        };

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        Dimension d = new Dimension(UITheme.SEARCH_BTN_SIZE, UITheme.SEARCH_BTN_SIZE);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
        return btn;
    }

    /**
     * Loads /icons/search.png from the classpath.
     *
     * Maven places src/main/resources on the classpath root, so the file at
     *   src/main/resources/icons/search.png
     * is addressable as /icons/search.png via Class.getResource() or
     * as icons/search.png via ClassLoader.getResource().
     *
     * Both forms are tried so the same code works whether running from the IDE,
     * via `mvn exec:java`, or from a packaged fat-jar.
     *
     * Returns null (never throws) — callers must handle the missing-icon case.
     */
    private static Image loadSearchIcon() {
        String[] candidates = {
                "/icons/search.png",   // Class.getResource — works in IDE + fat-jar
                "icons/search.png"     // ClassLoader.getResource — fallback
        };
        for (String path : candidates) {
            try {
                URL url = SearchPanel.class.getResource(path);
                if (url == null) {
                    url = SearchPanel.class.getClassLoader().getResource(
                            path.startsWith("/") ? path.substring(1) : path);
                }
                if (url != null) {
                    Image img = ImageIO.read(url);
                    if (img != null) return img;
                }
            } catch (Exception ignored) {
                // Try next candidate
            }
        }
        return null;
    }

    // ── Inner: custom text field ──────────────────────────────────────────────

    private static class RoundTextField extends JTextField {

        RoundTextField() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            setBackground(UITheme.BG_INPUT);
            setForeground(UITheme.TEXT_PRIMARY);
            setCaretColor(UITheme.ACCENT);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setToolTipText("Введите название города");
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = UITheme.FIELD_HEIGHT;
            return d;
        }
        @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        @Override public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, UITheme.FIELD_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = AnimationHelper.prepareGraphics(g);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.SMALL_RADIUS, UITheme.SMALL_RADIUS);
            g2.setColor(isFocusOwner() ? UITheme.ACCENT_DIM : UITheme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    UITheme.SMALL_RADIUS, UITheme.SMALL_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
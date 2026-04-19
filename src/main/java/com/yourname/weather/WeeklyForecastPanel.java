package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Horizontal scrollable row of ForecastCard tiles.
 *
 * LAYOUT STABILITY CONTRACT
 * ─────────────────────────
 * The panel lives inside a BoxLayout (Y_AXIS) column in MainWindow.
 * BoxLayout calls getPreferredSize() / getMaximumSize() on every child
 * during each revalidate(). If those sizes change after data loads the
 * entire column shifts — which is why the forecast appeared wrong on the
 * first search and only corrected itself on the second.
 *
 * Fix: pin a fixed preferred/min/max height here and in the JScrollPane,
 * so BoxLayout always allocates the same vertical space regardless of
 * whether forecast cards are present or not.
 *
 * The fade animation operates only on the alpha composite — it never
 * touches the component's bounds, so it cannot affect layout.
 */
public class WeeklyForecastPanel extends JPanel {

    /** Total fixed height: section label + cards row + scrollbar room. */
    private static final int PANEL_HEIGHT = 196;

    /** Height reserved for the ForecastCard row + scroll bar. */
    private static final int SCROLL_HEIGHT = 168;

    private final JPanel     cardsPanel;
    private final JScrollPane scroll;
    private float alpha = 1f;

    public WeeklyForecastPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(8, 0, 0, 0));

        // Pin this panel's height so BoxLayout never changes its allocation
        setMinimumSize(new Dimension(0,               PANEL_HEIGHT));
        setPreferredSize(new Dimension(Short.MAX_VALUE, PANEL_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, PANEL_HEIGHT));

        // Section label
        JLabel title = new JLabel("ПРОГНОЗ НА НЕДЕЛЮ");
        title.setFont(UITheme.FONT_SECTION_HDR);
        title.setForeground(UITheme.TEXT_MUTED);
        title.setBorder(new EmptyBorder(0, 2, 6, 0));
        add(title, BorderLayout.NORTH);

        // Cards row
        cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));

        scroll = buildScrollPane();
        add(scroll, BorderLayout.CENTER);
    }

    // ── Public ────────────────────────────────────────────────────────────────

    public void setForecasts(List<WeatherService.DailyForecast> forecasts) {
        cardsPanel.removeAll();

        for (WeatherService.DailyForecast f : forecasts) {
            ForecastCard card = new ForecastCard();
            card.setForecast(f);
            cardsPanel.add(card);
            cardsPanel.add(Box.createHorizontalStrut(6));
        }

        // Revalidate cardsPanel first so it computes its new preferred width,
        // then revalidate the scroll viewport, then the scroll pane itself.
        // This ordering guarantees the scrollable content is fully measured
        // before the parent layout sees the updated sizes — eliminating the
        // "shift on first render" bug caused by an incomplete layout pass.
        cardsPanel.revalidate();
        cardsPanel.repaint();
        scroll.getViewport().revalidate();
        scroll.revalidate();
        scroll.repaint();

        // Fade in — operates purely via alpha composite, never changes bounds
        alpha = 0f;
        AnimationHelper.createFade(this, 0f, 1f, 14, 22, null).start();
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        // Apply alpha composite to the copy only — never to g directly.
        // super.paintComponent is called on the copy so children inherit alpha.
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2);
        g2.dispose();
    }

    // ── Scroll pane ───────────────────────────────────────────────────────────

    private JScrollPane buildScrollPane() {
        JScrollPane sp = new JScrollPane(cardsPanel);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.getHorizontalScrollBar().setUnitIncrement(20);

        // Pin the scroll pane height so it never grows/shrinks and pushes siblings
        sp.setMinimumSize(new Dimension(0,               SCROLL_HEIGHT));
        sp.setPreferredSize(new Dimension(Short.MAX_VALUE, SCROLL_HEIGHT));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, SCROLL_HEIGHT));

        // Dark-theme scrollbar — no arrow buttons, subtle thumb
        sp.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = UITheme.BORDER_BRIGHT;
                this.trackColor = UITheme.BG_SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        return sp;
    }
}
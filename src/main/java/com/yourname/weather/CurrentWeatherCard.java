package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;
import com.yourname.weather.utils.RenderUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Main weather card. Two-section layout:
 *  • Top section  — gradient bg, icon, temperature, condition, city/date
 *  • Bottom strip — humidity | wind | pressure stats in pill cards
 */
public class CurrentWeatherCard extends JPanel {

    private float alpha = 1f;

    // Top section labels — initialised inline so there is exactly one assignment each
    private final JLabel iconLabel      = makeIconLabel();
    private final JLabel tempLabel      = new JLabel("—", SwingConstants.CENTER);
    private final JLabel conditionLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel cityLabel      = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel dateLabel      = new JLabel(formatToday(), SwingConstants.CENTER);
    private final JLabel feelsLabel     = new JLabel(" ", SwingConstants.CENTER);

    // Stat labels
    private final JLabel humidityValue  = statValue("—");
    private final JLabel windValue      = statValue("—");
    private final JLabel pressureValue  = statValue("—");

    public CurrentWeatherCard() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        add(buildTopSection(), BorderLayout.CENTER);
        add(buildStatsStrip(), BorderLayout.SOUTH);
    }

    private static JLabel makeIconLabel() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setPreferredSize(new Dimension(64, 64));
        return l;
    }

    // ── Section builders ──────────────────────────────────────────────────────

    private JPanel buildTopSection() {
        JPanel top = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = AnimationHelper.prepareGraphics(g);
                // Layered atmosphere gradient — deep navy → midnight blue
                RenderUtils.paintGradient(g2,
                        new Color(0x0C1628), new Color(0x0A1020),
                        0, 0, getWidth(), getHeight(), UITheme.CARD_RADIUS);
                // Subtle blue glow in upper-right
                g2.setPaint(new RadialGradientPaint(
                        getWidth() * 0.75f, getHeight() * 0.2f,
                        getWidth() * 0.55f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0x0D3A7A, true), new Color(0x00000000, true)}
                ));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.CARD_RADIUS, UITheme.CARD_RADIUS);
                // Border
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.CARD_RADIUS, UITheme.CARD_RADIUS);
                g2.dispose();
            }
        };
        top.setOpaque(false);
        top.setLayout(new GridBagLayout());
        top.setBorder(new EmptyBorder(20, 18, 18, 18));

        // Apply fonts / colors
        cityLabel.setFont(UITheme.FONT_CITY);
        cityLabel.setForeground(UITheme.TEXT_PRIMARY);
        dateLabel.setFont(UITheme.FONT_DATE);
        dateLabel.setForeground(UITheme.TEXT_MUTED);
        tempLabel.setFont(UITheme.FONT_TEMP);
        tempLabel.setForeground(UITheme.TEXT_PRIMARY);
        conditionLabel.setFont(UITheme.FONT_CONDITION);
        conditionLabel.setForeground(UITheme.TEXT_SECONDARY);
        feelsLabel.setFont(UITheme.FONT_FEELS);
        feelsLabel.setForeground(UITheme.TEXT_MUTED);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;

        addRow(top, gc, 0, iconLabel,      new Insets(0,  0, 2, 0));
        addRow(top, gc, 1, tempLabel,      new Insets(0,  0, 0, 0));
        addRow(top, gc, 2, conditionLabel, new Insets(0,  0, 2, 0));
        addRow(top, gc, 3, feelsLabel,     new Insets(0,  0, 10, 0));
        addRow(top, gc, 4, cityLabel,      new Insets(4,  0, 0, 0));
        addRow(top, gc, 5, dateLabel,      new Insets(0,  0, 0, 0));

        return top;
    }

    private JPanel buildStatsStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 8, 0));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(8, 0, 0, 0));
        strip.add(statCard("ВЛАЖНОСТЬ", humidityValue));
        strip.add(statCard("ВЕТЕР",     windValue));
        strip.add(statCard("ДАВЛЕНИЕ",  pressureValue));
        return strip;
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = AnimationHelper.prepareGraphics(g);
                g2.setColor(UITheme.BG_STAT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.INNER_RADIUS, UITheme.INNER_RADIUS);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.INNER_RADIUS, UITheme.INNER_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(8, 6, 8, 6));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_STAT_LABEL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void updateWeather(MainWindow.WeatherData data) {
        cityLabel.setText(data.cityName().toUpperCase());
        tempLabel.setText(data.temperature() + "°");
        conditionLabel.setText(data.description());
        feelsLabel.setText("Ощущается как " + data.feelsLike() + "°");
        humidityValue.setText(data.humidity() + "%");
        windValue.setText(String.format("%.1f м/с", data.windSpeed()));
        pressureValue.setText(data.pressure() + " гПа");
        dateLabel.setText(formatToday());
        iconLabel.setIcon(new ImageIcon(data.icon()));
        revalidate();
        repaint();
    }

    public void setAlpha(float a) {
        this.alpha = a;
        repaint();
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = AnimationHelper.prepareGraphics(g);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        // Outer shadow
        AnimationHelper.drawSoftShadow(g2, 0, 0, getWidth(), getHeight(), UITheme.CARD_RADIUS, 6);
        g2.dispose();
        super.paintComponent(g);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JLabel statValue(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_STAT_VALUE);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private static void addRow(JPanel p, GridBagConstraints gc, int row,
                               Component comp, Insets insets) {
        gc.gridy  = row;
        gc.insets = insets;
        p.add(comp, gc);
    }

    private static String formatToday() {
        return LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM", new Locale("ru")));
    }
}
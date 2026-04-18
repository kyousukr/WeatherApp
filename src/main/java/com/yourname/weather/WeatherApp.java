package com.yourname.weather;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class WeatherApp extends JFrame {

    private static final Color BG_DARK = new Color(0x0D1117);
    private static final Color BG_CARD = new Color(0x161B22);
    private static final Color BG_INPUT = new Color(0x1C2128);
    private static final Color ACCENT_BLUE = new Color(0x58A6FF);
    private static final Color TEXT_PRIMARY = new Color(0xF0F6FC);
    private static final Color TEXT_MUTED = new Color(0x8B949E);
    private static final Color BORDER_COLOR = new Color(0x30363D);
    private static final Color SUCCESS_GREEN = new Color(0x3FB950);
    private static final Color ERROR_RED = new Color(0xF85149);
    private static final Color TITLEBAR_BG = new Color(0x0A0E14);

    private static final int CORNER_RADIUS = 20;

    private final WeatherService weatherService;

    private JTextField cityField;
    private JButton searchButton;

    private JLabel cityNameLabel;
    private JLabel tempLabel;
    private JLabel conditionLabel;
    private JLabel feelsLikeLabel;
    private JLabel humidityLabel;
    private JLabel windLabel;
    private JLabel pressureLabel;
    private JLabel weatherIconLabel;
    private JLabel statusLabel;

    private JPanel weatherCard;

    private int dragOffsetX;
    private int dragOffsetY;
    private float cardAlpha = 1f;
    private Timer fadeTimer;

    public WeatherApp() {
        this.weatherService = new WeatherService();
        initFrame();
        initUI();
        pack();
        setSize(460, 640);
        setLocationRelativeTo(null);
        applyRoundedShape();
        setVisible(true);
    }

    private void initFrame() {
        setUndecorated(true);
        setMinimumSize(new Dimension(460, 580));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyRoundedShape();
            }
        });
    }

    private void applyRoundedShape() {
        try {
            setShape(new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight(),
                    CORNER_RADIUS * 2, CORNER_RADIUS * 2
            ));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS * 2, CORNER_RADIUS * 2);
                g2.dispose();
            }
        };
        root.setOpaque(true);
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(TITLEBAR_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + CORNER_RADIUS, CORNER_RADIUS * 2, CORNER_RADIUS * 2);
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 46));
        bar.setBorder(new EmptyBorder(0, 18, 0, 10));

        JLabel title = new JLabel("WEATHER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(TEXT_MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        controls.add(buildWindowButton("—", new Color(0x388BFD), e -> setState(Frame.ICONIFIED)));
        controls.add(buildWindowButton("×", new Color(0xE74C3C), e -> dispose()));

        bar.add(title, BorderLayout.CENTER);
        bar.add(controls, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(bar, e.getPoint(), WeatherApp.this);
                dragOffsetX = p.x;
                dragOffsetY = p.y;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point screen = e.getLocationOnScreen();
                setLocation(screen.x - dragOffsetX, screen.y - dragOffsetY);
            }
        };

        bar.addMouseListener(drag);
        bar.addMouseMotionListener(drag);
        title.addMouseListener(drag);
        title.addMouseMotionListener(drag);

        return bar;
    }

    private JButton buildWindowButton(String symbol, Color hoverColor, ActionListener action) {
        JButton btn = new JButton(symbol);
        btn.setPreferredSize(new Dimension(38, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(new Color(0xB0B8C2));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e) { btn.setForeground(new Color(0xB0B8C2)); btn.repaint(); }
        });

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = gfx(g);
                if (btn.getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(new Color(0x2D333B));
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);

                g2.setColor(btn.getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(symbol)) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(symbol, x, y);
                g2.dispose();
            }
        });

        return btn;
    }

    private JPanel buildCenter() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 20, 8, 20));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(0, 0, 16, 0));

        cityField = new RoundTextField();
        cityField.setBackground(BG_INPUT);
        cityField.setForeground(TEXT_PRIMARY);
        cityField.setCaretColor(ACCENT_BLUE);
        cityField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cityField.setToolTipText("Enter city");
        cityField.addActionListener(e -> searchWeather());

        searchButton = buildSearchButton();
        searchButton.addActionListener(e -> searchWeather());

        searchRow.add(cityField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        weatherCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardAlpha));
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        weatherCard.setOpaque(false);
        weatherCard.setLayout(new GridBagLayout());
        weatherCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        weatherIconLabel = new JLabel("", SwingConstants.CENTER);
        cityNameLabel = label("", new Font("Segoe UI", Font.BOLD, 16), TEXT_PRIMARY);
        tempLabel = label("", new Font("Segoe UI", Font.BOLD, 76), TEXT_PRIMARY);
        conditionLabel = label("", new Font("Segoe UI", Font.BOLD, 15), TEXT_MUTED);
        feelsLikeLabel = label("", new Font("Segoe UI", Font.BOLD, 13), TEXT_MUTED);

        JPanel statsPanel = buildStatsPanel();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;

        addCardRow(weatherCard, gc, 0, weatherIconLabel, new Insets(0, 0, 2, 0));
        addCardRow(weatherCard, gc, 1, cityNameLabel, new Insets(0, 0, 2, 0));
        addCardRow(weatherCard, gc, 2, tempLabel, new Insets(2, 0, 0, 0));
        addCardRow(weatherCard, gc, 3, conditionLabel, new Insets(0, 0, 2, 0));
        addCardRow(weatherCard, gc, 4, feelsLikeLabel, new Insets(0, 0, 0, 0));
        addCardRow(weatherCard, gc, 5, statsPanel, new Insets(18, 0, 0, 0));

        wrap.add(searchRow, BorderLayout.NORTH);
        wrap.add(weatherCard, BorderLayout.CENTER);
        return wrap;
    }

    private void addCardRow(JPanel card, GridBagConstraints gc, int row, Component component, Insets insets) {
        gc.gridy = row;
        gc.insets = insets;
        card.add(component, gc);
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);

        humidityLabel = label("—", new Font("Segoe UI", Font.BOLD, 16), TEXT_PRIMARY);
        windLabel = label("—", new Font("Segoe UI", Font.BOLD, 16), TEXT_PRIMARY);
        pressureLabel = label("—", new Font("Segoe UI", Font.BOLD, 16), TEXT_PRIMARY);

        panel.add(statCard("HUMIDITY", humidityLabel));
        panel.add(statCard("WIND", windLabel));
        panel.add(statCard("PRESSURE", pressureLabel));

        return panel;
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(new Color(0x21262D));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 8, 10, 8));

        JLabel titleLabel = label(title, new Font("Segoe UI", Font.BOLD, 10), TEXT_MUTED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        statusLabel = label("Введите город", new Font("Segoe UI", Font.BOLD, 12), TEXT_MUTED);
        bar.add(statusLabel);
        return bar;
    }

    private JButton buildSearchButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                Color bg = getModel().isPressed()
                        ? ACCENT_BLUE.darker()
                        : getModel().isRollover()
                        ? new Color(0x388BFD)
                        : ACCENT_BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        try {
            java.net.URL iconUrl = getClass().getResource("/icons/search.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            } else {
                btn.setText("↻");
            }
        } catch (Exception e) {
            btn.setText("↻");
        }

        btn.setPreferredSize(new Dimension(52, 52));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void searchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            shake(cityField);
            return;
        }

        searchButton.setEnabled(false);
        statusLabel.setForeground(ACCENT_BLUE);
        statusLabel.setText("Загрузка...");

        new SwingWorker<WeatherData, Void>() {
            @Override
            protected WeatherData doInBackground() {
                return weatherService.fetch(city);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                try {
                    WeatherData data = get();
                    if (data == null) {
                        statusLabel.setForeground(ERROR_RED);
                        statusLabel.setText("Город не найден или ошибка сети");
                        return;
                    }

                    updateWeather(data);
                    statusLabel.setForeground(SUCCESS_GREEN);
                    statusLabel.setText("Обновлено только что");
                    animateCardIn();
                } catch (Exception ex) {
                    statusLabel.setForeground(ERROR_RED);
                    statusLabel.setText("Ошибка: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateWeather(WeatherData d) {
        cityNameLabel.setText(d.cityName.toUpperCase());
        tempLabel.setText(d.temperature + "°");
        conditionLabel.setText(d.description);
        feelsLikeLabel.setText("Ощущается как " + d.feelsLike + "°");
        humidityLabel.setText(d.humidity + "%");
        windLabel.setText(String.format("%.1f м/с %s", d.windSpeed, windArrow(d.windDeg)));
        pressureLabel.setText(d.pressure + " гПа");

        weatherIconLabel.setIcon(new ImageIcon(d.icon));
        weatherCard.revalidate();
        weatherCard.repaint();
    }

    private void animateCardIn() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        cardAlpha = 0f;
        fadeTimer = new Timer(14, null);
        fadeTimer.addActionListener(e -> {
            cardAlpha = Math.min(1f, cardAlpha + 0.08f);
            weatherCard.repaint();
            if (cardAlpha >= 1f) {
                fadeTimer.stop();
            }
        });
        fadeTimer.start();
    }

    private void shake(Component c) {
        Point original = c.getLocation();
        int[] steps = {-9, 9, -7, 7, -4, 4, -2, 2, 0};
        int[] idx = {0};

        Timer timer = new Timer(28, null);
        timer.addActionListener(e -> {
            if (idx[0] >= steps.length) {
                timer.stop();
                c.setLocation(original);
                return;
            }
            c.setLocation(original.x + steps[idx[0]++], original.y);
        });
        timer.start();
    }

    private static Graphics2D gfx(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    private String windArrow(int deg) {
        String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
        int index = (int) Math.round((deg % 360) / 45.0) % 8;
        return arrows[index];
    }

    static class RoundTextField extends JTextField {
        RoundTextField() {
            super(20);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = gfx(g);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(new Color(0x30363D));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = 52;
            return d;
        }
    }

    public record WeatherData(
            String cityName,
            String description,
            int temperature,
            int feelsLike,
            int humidity,
            double windSpeed,
            int windDeg,
            int pressure,
            Image image,
            Image icon
    ) {}

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new WeatherApp();
        });
    }
}
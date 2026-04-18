package com.yourname.weather;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class WeatherApp extends JFrame {

    private static final Color BG_DARK = new Color(0x0D1117);
    private static final Color BG_CARD = new Color(0x161B22);
    private static final Color BG_INPUT = new Color(0x1C2128);
    private static final Color ACCENT_BLUE = new Color(0x58A6FF);
    private static final Color TEXT_PRIMARY = new Color(0xF0F6FC);
    private static final Color TEXT_MUTED = new Color(0x8B949E);
    private static final Color BORDER_COLOR = new Color(0x30363D);
    private static final Color SUCCESS_GREEN = new Color(0x3FB950);
    private static final Color TITLEBAR_BG = new Color(0x0A0E14);

    private static final int CORNER_RADIUS = 20;
    private static final Font FONT_TEMP = new Font("Segoe UI", Font.BOLD, 76);
    private static final Font FONT_CITY = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_COND = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_FEELS = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_STAT_VAL = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_TITLEBAR = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_STATUS = new Font("Segoe UI", Font.BOLD, 12);

    private JTextField cityField;
    private JButton searchButton;
    private JLabel cityNameLabel, tempLabel, conditionLabel, feelsLikeLabel;
    private JLabel humidityLabel, windLabel, pressureLabel;
    private JLabel weatherIconLabel;
    private JPanel weatherCard, statsPanel;
    private JLabel statusLabel;

    private int dragOffsetX, dragOffsetY;
    private float cardAlpha = 0f;
    private Timer fadeTimer;

    private String apiKey;

    public WeatherApp() {
        loadApiKey();
        setUndecorated(true);
        initUI();
        pack();
        setMinimumSize(new Dimension(460, 580));
        setSize(460, 640);
        setLocationRelativeTo(null);
        applyRoundedShape();
        setVisible(true);
    }

    private void loadApiKey() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            Properties props = new Properties();
            props.load(is);
            apiKey = props.getProperty("openweathermap.api.key");
            if (apiKey == null || apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Не найден API ключ в config.properties", "Ошибка", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки config.properties", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void applyRoundedShape() {
        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    CORNER_RADIUS * 2, CORNER_RADIUS * 2));
        } catch (UnsupportedOperationException ignored) {}
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        CORNER_RADIUS * 2, CORNER_RADIUS * 2);
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

    static Graphics2D gfx(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(TITLEBAR_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + CORNER_RADIUS,
                        CORNER_RADIUS * 2, CORNER_RADIUS * 2);
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 46));
        bar.setBorder(new EmptyBorder(0, 18, 0, 10));

        JLabel title = new JLabel("WEATHER");
        title.setFont(FONT_TITLEBAR);
        title.setForeground(TEXT_MUTED);

        JPanel winControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        winControls.setOpaque(false);
        winControls.add(buildWinBtn(WinBtnType.MINIMIZE, new Color(0x388BFD),
                e -> setState(Frame.ICONIFIED)));
        winControls.add(buildWinBtn(WinBtnType.CLOSE, new Color(0xE74C3C),
                e -> System.exit(0)));

        bar.add(title, BorderLayout.CENTER);
        bar.add(winControls, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point inWindow = SwingUtilities.convertPoint(bar, e.getPoint(), WeatherApp.this);
                dragOffsetX = inWindow.x;
                dragOffsetY = inWindow.y;
            }
            @Override public void mouseDragged(MouseEvent e) {
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

    enum WinBtnType { MINIMIZE, CLOSE }

    private JButton buildWinBtn(WinBtnType type, Color hoverBg, ActionListener action) {
        JButton btn = new JButton() {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                int w = getWidth(), h = getHeight();
                int arc = 8;
                if (hov) g2.setColor(hoverBg);
                else g2.setColor(new Color(0x2D333B));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                Color iconColor = hov ? Color.WHITE : new Color(0xB0B8C2);
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = w / 2, cy = h / 2, r = 5;
                if (type == WinBtnType.MINIMIZE) {
                    g2.drawLine(cx - r, cy + 2, cx + r, cy + 2);
                } else {
                    g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                    g2.drawLine(cx + r, cy - r, cx - r, cy + r);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(38, 26));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
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
        cityField.setFont(FONT_INPUT);
        cityField.addActionListener(e -> searchWeather());

        searchButton = buildSearchButton();
        searchButton.addActionListener(e -> searchWeather());

        searchRow.add(cityField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        weatherCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardAlpha));
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        weatherCard.setOpaque(false);
        weatherCard.setLayout(new GridBagLayout());
        weatherCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        weatherIconLabel = new JLabel("", SwingConstants.CENTER);
        cityNameLabel    = lbl("", FONT_CITY,  TEXT_PRIMARY);
        tempLabel        = lbl("", FONT_TEMP,  TEXT_PRIMARY);
        conditionLabel   = lbl("", FONT_COND,  TEXT_MUTED);
        feelsLikeLabel   = lbl("", FONT_FEELS, TEXT_MUTED);
        statsPanel       = buildStatsPanel();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        int[][] ins = {{0,0,2,0},{0,0,2,0},{2,0,0,0},{0,0,2,0},{0,0,0,0},{18,0,0,0}};
        Component[] rows = {weatherIconLabel, cityNameLabel, tempLabel,
                conditionLabel, feelsLikeLabel, statsPanel};
        for (int i = 0; i < rows.length; i++) {
            gc.gridy = i;
            gc.insets = new Insets(ins[i][0], ins[i][1], ins[i][2], ins[i][3]);
            weatherCard.add(rows[i], gc);
        }

        wrap.add(searchRow, BorderLayout.NORTH);
        wrap.add(weatherCard, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 0));
        p.setOpaque(false);
        humidityLabel = lbl("—", FONT_STAT_VAL, TEXT_PRIMARY);
        windLabel     = lbl("—", FONT_STAT_VAL, TEXT_PRIMARY);
        pressureLabel = lbl("—", FONT_STAT_VAL, TEXT_PRIMARY);
        p.add(buildStatCard("ВЛАЖНОСТЬ", humidityLabel));
        p.add(buildStatCard("ВЕТЕР",     windLabel));
        p.add(buildStatCard("ДАВЛЕНИЕ",  pressureLabel));
        return p;
    }

    private JPanel buildStatCard(String title, JLabel valLabel) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                g2.setColor(new Color(0x21262D));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 8, 10, 8));

        JLabel titleLbl = lbl(title, FONT_STAT_LBL, TEXT_MUTED);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        valLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valLabel);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));
        statusLabel = lbl("Введите город", FONT_STATUS, TEXT_MUTED);
        bar.add(statusLabel);
        return bar;
    }

    private JButton buildSearchButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = gfx(g);
                Color bg = getModel().isPressed()  ? ACCENT_BLUE.darker()
                        : getModel().isRollover() ? new Color(0x388BFD)
                        : ACCENT_BLUE;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(52, 52));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadSearchIcon(btn);
        return btn;
    }

    private void loadSearchIcon(JButton btn) {
        for (String path : new String[]{"/icons/search.png", "/com/yourname/weather/icons/search.png"}) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null) {
                        btn.setIcon(new ImageIcon(toWhite(img).getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
                        btn.setText("");
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
        btn.setIcon(new Icon() {
            @Override public int getIconWidth() { return 22; }
            @Override public int getIconHeight() { return 22; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = gfx(g);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 2, y + 2, 13, 13);
                g2.drawLine(x + 14, y + 14, x + 20, y + 20);
                g2.dispose();
            }
        });
        btn.setText("");
    }

    private BufferedImage toWhite(BufferedImage src) {
        BufferedImage r = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++) {
                int a = (src.getRGB(x, y) >> 24) & 0xFF;
                r.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        return r;
    }

    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font); l.setForeground(color);
        return l;
    }

    private void animateCardIn() {
        if (fadeTimer != null) fadeTimer.stop();
        cardAlpha = 0f;
        fadeTimer = new Timer(14, null);
        fadeTimer.addActionListener(e -> {
            cardAlpha = Math.min(1f, cardAlpha + 0.07f);
            weatherCard.repaint();
            if (cardAlpha >= 1f) fadeTimer.stop();
        });
        fadeTimer.start();
    }

    private void shake(Component c) {
        Point orig = c.getLocation();
        int[] steps = {-9, 9, -7, 7, -4, 4, -2, 2, 0};
        int[] idx = {0};
        Timer t = new Timer(28, null);
        t.addActionListener(e -> {
            if (idx[0] >= steps.length) { t.stop(); c.setLocation(orig); return; }
            c.setLocation(orig.x + steps[idx[0]++], orig.y);
        });
        t.start();
    }

    private void searchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) { shake(cityField); return; }
        searchButton.setEnabled(false);
        statusLabel.setForeground(ACCENT_BLUE);
        statusLabel.setText("Загрузка...");

        new SwingWorker<WeatherData, Void>() {
            @Override protected WeatherData doInBackground() { return fetchWeather(city); }
            @Override protected void done() {
                searchButton.setEnabled(true);
                try {
                    WeatherData d = get();
                    if (d == null) {
                        statusLabel.setForeground(new Color(0xF85149));
                        statusLabel.setText("Город не найден или ошибка сети");
                    } else {
                        updateUI(d);
                        statusLabel.setForeground(SUCCESS_GREEN);
                        statusLabel.setText("Обновлено только что");
                        animateCardIn();
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(new Color(0xF85149));
                    statusLabel.setText("Ошибка: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private WeatherData fetchWeather(String city) {
        try {
            String enc = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + enc + "&appid=" + apiKey + "&units=metric&lang=ru";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "JavaWeatherApp/3.0");
            int code = conn.getResponseCode();
            InputStream stream = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) return null;
            StringBuilder sb = new StringBuilder();
            try (Scanner sc = new Scanner(stream, StandardCharsets.UTF_8)) {
                while (sc.hasNextLine()) sb.append(sc.nextLine());
            }
            if (code != 200) return null;
            return parseJson(sb.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private WeatherData parseJson(String json) {
        WeatherData d = new WeatherData();
        d.cityName    = extractStr(json, "\"name\":");
        d.temp        = (int) Math.round(extractDbl(json, "\"temp\":"));
        d.feelsLike   = (int) Math.round(extractDbl(json, "\"feels_like\":"));
        d.humidity    = extractInt(json, "\"humidity\":");
        d.windMps     = extractDbl(json, "\"speed\":");
        d.windDeg     = extractInt(json, "\"deg\":");
        d.pressure    = extractInt(json, "\"pressure\":");
        d.description = cap(extractStr(json, "\"description\":"));
        d.iconCode    = extractStr(json, "\"icon\":");
        return d;
    }

    private double extractDbl(String j, String key) {
        int i = j.indexOf(key); if (i < 0) return 0;
        int s = i + key.length();
        while (s < j.length() && j.charAt(s) == ' ') s++;
        int e = s;
        while (e < j.length() && (Character.isDigit(j.charAt(e)) || j.charAt(e) == '.' || j.charAt(e) == '-')) e++;
        try { return Double.parseDouble(j.substring(s, e)); }
        catch (Exception x) { return 0; }
    }
    private int extractInt(String j, String k) { return (int) Math.round(extractDbl(j, k)); }
    private String extractStr(String j, String key) {
        int i = j.indexOf(key); if (i < 0) return "";
        int s = j.indexOf('"', i + key.length()); if (s < 0) return "";
        s++; int e = j.indexOf('"', s); return e < 0 ? "" : j.substring(s, e);
    }
    private String cap(String s) {
        return (s == null || s.isEmpty()) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void updateUI(WeatherData d) {
        cityNameLabel.setText(d.cityName.toUpperCase());
        tempLabel.setText(d.temp + "°");
        conditionLabel.setText(d.description);
        feelsLikeLabel.setText("Ощущается как " + d.feelsLike + "°");
        humidityLabel.setText(d.humidity + "%");
        windLabel.setText(String.format("%.1f м/с %s", d.windMps, windArrow(d.windDeg)));
        pressureLabel.setText(d.pressure + " гПа");
        if (!d.iconCode.isEmpty()) loadWeatherIcon(d.iconCode);
    }

    private void loadWeatherIcon(String code) {
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                BufferedImage img = ImageIO.read(new URL("https://openweathermap.org/img/wn/" + code + "@2x.png"));
                return img == null ? null : new ImageIcon(img.getScaledInstance(72, 72, Image.SCALE_SMOOTH));
            }
            @Override protected void done() {
                try { ImageIcon ic = get(); if (ic != null) weatherIconLabel.setIcon(ic); }
                catch (Exception ignored) {}
            }
        }.execute();
    }

    private String windArrow(int deg) {
        return new String[]{"↑","↗","→","↘","↓","↙","←","↖"}[(int) Math.round((deg % 360) / 45) % 8];
    }

    static class RoundTextField extends JTextField {
        RoundTextField() {
            super(20);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = WeatherApp.gfx(g);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
            g2.setColor(new Color(0x30363D));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize(); d.height = 52; return d;
        }
    }

    static class WeatherData {
        String cityName = "", description = "", iconCode = "";
        int temp, feelsLike, humidity, windDeg, pressure;
        double windMps;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
                catch (Exception ignored) {}
            }
            new WeatherApp();
        });
    }
}
package com.yourname.weather;

import com.yourname.weather.utils.AnimationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Application root window.
 * Composes TitleBar / SearchPanel / CurrentWeatherCard / WeeklyForecastPanel / StatusBar
 * and delegates weather fetching to WeatherService via SwingWorker.
 */
public class MainWindow extends JFrame {

    private final WeatherService weatherService;

    private SearchPanel          searchPanel;
    private CurrentWeatherCard   currentWeatherCard;
    private WeeklyForecastPanel  weeklyForecastPanel;
    private StatusBar            statusBar;

    // Reference kept so we can call revalidate() on it after data loads
    private JPanel centerPanel;

    public MainWindow() {
        weatherService = new WeatherService();
        initFrame();
        initUI();
        setSize(480, 740);
        setLocationRelativeTo(null);
        applyShape();
        setVisible(true);
    }

    // ── Frame setup ───────────────────────────────────────────────────────────

    private void initFrame() {
        setUndecorated(true);
        setMinimumSize(new Dimension(440, 660));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createEmptyBorder());
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                applyShape();
            }
        });
    }

    private void applyShape() {
        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    UITheme.CORNER_RADIUS * 2, UITheme.CORNER_RADIUS * 2));
        } catch (UnsupportedOperationException ignored) {}
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = AnimationHelper.prepareGraphics(g);
                g2.setColor(UITheme.BG_BASE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        UITheme.CORNER_RADIUS * 2, UITheme.CORNER_RADIUS * 2);
                g2.dispose();
            }
        };
        root.setOpaque(true);
        root.setBackground(UITheme.BG_BASE);
        setContentPane(root);

        root.add(new TitleBar(this), BorderLayout.NORTH);
        root.add(buildCenterPanel(),  BorderLayout.CENTER);

        statusBar = new StatusBar();
        root.add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel buildCenterPanel() {
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(8, UITheme.H_PAD, 2, UITheme.H_PAD));

        searchPanel         = new SearchPanel(this::onSearch);
        currentWeatherCard  = new CurrentWeatherCard();
        weeklyForecastPanel = new WeeklyForecastPanel();

        centerPanel.add(searchPanel);
        centerPanel.add(Box.createVerticalStrut(UITheme.V_GAP));
        centerPanel.add(currentWeatherCard);
        centerPanel.add(Box.createVerticalStrut(UITheme.V_GAP));
        centerPanel.add(weeklyForecastPanel);

        return centerPanel;
    }

    // ── Search flow ───────────────────────────────────────────────────────────

    private void onSearch(ActionEvent e) {
        String city = searchPanel.getCityText();
        if (city.isEmpty()) {
            AnimationHelper.shake(searchPanel);
            return;
        }
        searchPanel.setEnabled(false);
        statusBar.setLoading();

        new SwingWorker<WeatherData, Void>() {
            @Override protected WeatherData doInBackground() {
                return weatherService.fetchCurrent(city);
            }
            @Override protected void done() {
                searchPanel.setEnabled(true);
                try {
                    WeatherData data = get();
                    if (data == null) {
                        statusBar.setError("Город не найден или ошибка сети");
                        return;
                    }
                    currentWeatherCard.updateWeather(data);

                    // Flush the full layout hierarchy BEFORE starting the fade,
                    // so the card occupies its final position on the very first
                    // paint frame — no jump, no second-search correction needed.
                    centerPanel.revalidate();
                    centerPanel.repaint();

                    AnimationHelper.createFade(currentWeatherCard, 0f, 1f, 14, 20,
                            () -> currentWeatherCard.setAlpha(1f)).start();
                    statusBar.setSuccess("Обновлено только что");
                    loadForecast(city);
                } catch (Exception ex) {
                    statusBar.setError("Ошибка: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadForecast(String city) {
        new SwingWorker<java.util.List<WeatherService.DailyForecast>, Void>() {
            @Override protected java.util.List<WeatherService.DailyForecast> doInBackground() {
                return weatherService.fetchWeeklyForecast(city);
            }
            @Override protected void done() {
                try {
                    var forecasts = get();
                    if (forecasts != null && !forecasts.isEmpty()) {
                        weeklyForecastPanel.setForecasts(forecasts);
                        // Same flush after forecast cards are added
                        centerPanel.revalidate();
                        centerPanel.repaint();
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }



    public record WeatherData(
            String cityName,
            String description,
            int    temperature,
            int    feelsLike,
            int    humidity,
            double windSpeed,
            int    windDeg,
            int    pressure,
            Image  image,
            Image  icon,
            double lat,
            double lon
    ) {}

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainWindow();
        });
    }
}
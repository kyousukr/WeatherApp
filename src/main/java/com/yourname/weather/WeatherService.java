package com.yourname.weather;

import com.google.gson.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Properties;

public class WeatherService {
    private final String apiKey;
    private final Gson gson = new Gson();

    public WeatherService() {
        this.apiKey = loadApiKey();
    }

    public MainWindow.WeatherData fetchCurrent(String city) {
        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + encoded + "&appid=" + apiKey + "&units=metric&lang=ru";
            JsonObject root = fetchJson(url);
            if (root == null) return null;
            return parseCurrentWeather(root);
        } catch (Exception e) {
            return null;
        }
    }

    public List<DailyForecast> fetchWeeklyForecast(String city) {
        try {
            // 1. Получаем координаты через текущую погоду
            MainWindow.WeatherData current = fetchCurrent(city);
            if (current == null) return List.of();

            // 2. Запрашиваем 5-дневный прогноз (бесплатный)
            String url = "https://api.openweathermap.org/data/2.5/forecast?lat="
                    + current.lat() + "&lon=" + current.lon()
                    + "&units=metric&lang=ru&appid=" + apiKey;
            JsonObject root = fetchJson(url);
            if (root == null) return List.of();

            return parseForecast(root);
        } catch (Exception e) {
            return List.of();
        }
    }

    private JsonObject fetchJson(String urlStr) {
        try {
            HttpURLConnection con = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            con.setRequestProperty("User-Agent", "WeatherApp/1.0");
            int code = con.getResponseCode();
            InputStream is = code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream();
            if (is == null) return null;
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private MainWindow.WeatherData parseCurrentWeather(JsonObject root) {
        JsonObject main = root.getAsJsonObject("main");
        JsonObject wind = root.getAsJsonObject("wind");
        JsonArray weatherArray = root.getAsJsonArray("weather");
        JsonObject weather = weatherArray.get(0).getAsJsonObject();

        String city = root.get("name").getAsString();
        String desc = weather.get("description").getAsString();
        int temp = main.get("temp").getAsInt();
        int feels = main.get("feels_like").getAsInt();
        int humidity = main.get("humidity").getAsInt();
        int pressure = main.get("pressure").getAsInt();
        double windSpeed = wind.get("speed").getAsDouble();
        int windDeg = wind.has("deg") ? wind.get("deg").getAsInt() : 0;
        String iconCode = weather.get("icon").getAsString();

        double lat = root.getAsJsonObject("coord").get("lat").getAsDouble();
        double lon = root.getAsJsonObject("coord").get("lon").getAsDouble();

        Image icon = loadIcon(iconCode);
        return new MainWindow.WeatherData(
                city,
                capitalize(desc),
                temp,
                feels,
                humidity,
                windSpeed,
                windDeg,
                pressure,
                icon,
                icon,
                lat,
                lon
        );
    }

    private List<DailyForecast> parseForecast(JsonObject root) {
        JsonArray list = root.getAsJsonArray("list");
        // Группируем по дате (день)
        Map<LocalDate, List<JsonObject>> grouped = new LinkedHashMap<>();
        ZoneId zone = ZoneId.systemDefault();

        for (JsonElement elem : list) {
            JsonObject item = elem.getAsJsonObject();
            long dt = item.get("dt").getAsLong();
            LocalDate date = Instant.ofEpochSecond(dt).atZone(zone).toLocalDate();
            grouped.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        List<DailyForecast> forecasts = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE", new Locale("ru"));
        LocalDate today = LocalDate.now(zone);

        int daysToShow = 7;
        int added = 0;
        for (Map.Entry<LocalDate, List<JsonObject>> entry : grouped.entrySet()) {
            if (added >= daysToShow) break;
            LocalDate date = entry.getKey();
            List<JsonObject> items = entry.getValue();

            // Определяем день недели
            String dayOfWeek;
            if (date.equals(today)) {
                dayOfWeek = "Сегодня";
            } else if (date.equals(today.plusDays(1))) {
                dayOfWeek = "Завтра";
            } else {
                dayOfWeek = dayFmt.format(date);
            }

            // Мин/макс температура за день
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;
            String mainIcon = "";
            String mainDesc = "";
            double popSum = 0;
            int popCount = 0;

            for (JsonObject item : items) {
                JsonObject main = item.getAsJsonObject("main");
                double temp = main.get("temp").getAsDouble();
                if (temp < minTemp) minTemp = temp;
                if (temp > maxTemp) maxTemp = temp;

                // Берём описание и иконку из первого элемента (дневного)
                if (mainIcon.isEmpty()) {
                    JsonArray weatherArr = item.getAsJsonArray("weather");
                    JsonObject weather = weatherArr.get(0).getAsJsonObject();
                    mainIcon = weather.get("icon").getAsString();
                    mainDesc = weather.get("description").getAsString();
                }

                // Вероятность осадков (pop)
                if (item.has("pop")) {
                    popSum += item.get("pop").getAsDouble();
                    popCount++;
                }
            }

            int min = (int) Math.round(minTemp);
            int max = (int) Math.round(maxTemp);
            int pop = popCount > 0 ? (int) Math.round((popSum / popCount) * 100) : 0;
            Image icon = loadIcon(mainIcon);

            forecasts.add(new DailyForecast(dayOfWeek, min, max, capitalize(mainDesc), pop, icon));
            added++;
        }
        return forecasts;
    }

    private Image loadIcon(String iconCode) {
        try {
            URL url = URI.create("https://openweathermap.org/img/wn/" + iconCode + "@2x.png").toURL();
            BufferedImage img = ImageIO.read(url);
            return img.getScaledInstance(56, 56, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            return createPlaceholder();
        }
    }

    private Image createPlaceholder() {
        BufferedImage img = new BufferedImage(56, 56, BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    private String loadApiKey() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                String key = props.getProperty("openweathermap.api.key", "").trim();
                if (!key.isBlank()) return key;
            }
        } catch (Exception ignored) {}
        String sysKey = System.getProperty("openweathermap.api.key", "").trim();
        if (!sysKey.isBlank()) return sysKey;
        throw new IllegalStateException("API key not found");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public record DailyForecast(String dayOfWeek, int minTemp, int maxTemp,
                                String description, int pop, Image icon) {}
}
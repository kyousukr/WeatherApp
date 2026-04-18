package com.yourname.weather;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WeatherService {

    private final String apiKey;

    public WeatherService() {
        this.apiKey = loadApiKey();
    }

    public WeatherApp.WeatherData fetch(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                    + encodedCity + "&appid=" + apiKey + "&units=metric&lang=ru";

            HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "WeatherApp/1.0");

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) {
                return null;
            }

            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (code < 200 || code >= 300) {
                return null;
            }

            return parseWeather(json);

        } catch (Exception e) {
            return null;
        }
    }

    private WeatherApp.WeatherData parseWeather(String json) throws IOException {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        String cityName = getString(root, "name", "");
        JsonObject main = root.getAsJsonObject("main");
        JsonObject wind = root.getAsJsonObject("wind");
        JsonArray weatherArray = root.getAsJsonArray("weather");

        JsonObject weather = (weatherArray != null && !weatherArray.isEmpty())
                ? weatherArray.get(0).getAsJsonObject()
                : new JsonObject();

        String description = getString(weather, "description", "");
        int temperature = getInt(main, "temp", 0);
        int feelsLike = getInt(main, "feels_like", 0);
        int humidity = getInt(main, "humidity", 0);
        int pressure = getInt(main, "pressure", 0);
        double windSpeed = getDouble(wind, "speed", 0.0);
        int windDeg = getInt(wind, "deg", 0);
        String iconCode = getString(weather, "icon", "");

        Image icon = iconCode.isBlank() ? null : loadIcon(iconCode);
        if (icon == null) {
            icon = createPlaceholderIcon();
        }

        return new WeatherApp.WeatherData(
                cityName,
                capitalize(description),
                temperature,
                feelsLike,
                humidity,
                windSpeed,
                windDeg,
                pressure,
                icon,
                icon
        );
    }

    private Image loadIcon(String iconCode) {
        try {
            URL url = URI.create("https://openweathermap.org/img/wn/" + iconCode + "@2x.png").toURL();
            BufferedImage image = ImageIO.read(url);
            if (image == null) {
                return null;
            }
            return image.getScaledInstance(72, 72, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            return null;
        }
    }

    private Image createPlaceholderIcon() {
        BufferedImage img = new BufferedImage(72, 72, BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    private String loadApiKey() {
        Properties props = new Properties();

        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                String key = props.getProperty("openweathermap.api.key", "").trim();
                if (!key.isBlank()) {
                    return key;
                }
            }
        } catch (Exception ignored) {
        }

        String systemKey = System.getProperty("openweathermap.api.key", "").trim();
        if (!systemKey.isBlank()) {
            return systemKey;
        }

        throw new IllegalStateException(
                "API key not found. Put it into src/main/resources/config.properties or pass -Dopenweathermap.api.key=..."
        );
    }

    private String getString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        return obj.get(key).getAsString();
    }

    private int getInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double getDouble(JsonObject obj, String key, double defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
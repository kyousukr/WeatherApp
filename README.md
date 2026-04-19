# Weather App

A desktop weather application built with Java Swing.

## Features
- Current weather display
- Weekly forecast
- City search
- Modern dark UI

## Requirements
- Java 17+
- Maven

## Setup
1. Get OpenWeatherMap API key
2. Add to src/main/resources/config.properties:
   ```
   openweathermap.api.key=your_api_key_here
   ```
3. Build and run:
   ```
   mvn clean package
   java -jar target/weather-app-1.0.0-shaded.jar
   ```

## Project Structure
```
src/main/java/com/yourname/weather/
├── MainWindow.java          # Main application class & entry point
├── SearchPanel.java          # Search functionality
├── CurrentWeatherCard.java   # Current weather display
├── WeeklyForecastPanel.java  # Weekly forecast
├── ForecastCard.java         # Single forecast card
├── TitleBar.java             # Custom title bar
├── StatusBar.java            # Status bar
├── WindowButton.java          # Window controls
├── UITheme.java              # UI theme constants
├── WeatherService.java        # API integration
└── utils/
    ├── AnimationHelper.java   # Animation utilities
    └── RenderUtils.java       # Rendering helpers
```

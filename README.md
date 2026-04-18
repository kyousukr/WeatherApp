## 🚀 Getting started

### Requirements

- JDK 17 or newer
- Maven 3.6+
- An OpenWeatherMap API key

### Run locally

```bash
git clone https://github.com/kyousukr/WeatherApp.git
cd WeatherApp
mvn clean package
mvn exec:java -Dexec.mainClass="com.kyousukr.weatherapp.WeatherApp"
```

🔐 API key setup

The app looks for your OpenWeatherMap key in one of these places:

src/main/resources/config.properties
JVM system property: -Dopenweathermap.api.key=...

Example config.properties:

openweathermap.api.key=PUT_YOUR_API_KEY

Important: do not commit your real API key to GitHub.

📁 Project structure
WeatherApp/
├── pom.xml
├── README.md
├── .gitignore
├── screenshots/
│   └── screenshot.png
└── src/
    └── main/
        ├── java/
        │   └── com/kyousukr/weatherapp/
        │       ├── WeatherApp.java
        │       └── WeatherService.java
        └── resources/
            ├── config.properties
            └── icons/
                └── search.png
🛠️ Notes
Weather data is parsed with Gson.
API errors and missing keys are handled gracefully.
If the API key is missing, the app shows a clear startup error.
The project is intentionally lightweight and easy to extend.
📌 Future improvements
Move API settings fully into external config
Add forecast support
Add city history / favorites
Improve icon caching
Split UI into smaller components
🧑‍💻 About this project

This is my first GitHub project and one of my first Java desktop apps.
Built to learn Swing, API integration, and clean project structure.

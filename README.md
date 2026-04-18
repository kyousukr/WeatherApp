# ☁️ Modern Java Weather App

![Java Version](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java_Swing-007396?style=for-the-badge&logo=java)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

Современное десктопное приложение погоды, полностью написанное на **чистой Java + Swing**.  
Кастомный безрамочный тёмный интерфейс, плавные анимации, перетаскивание окна мышкой и данные в реальном времени из **OpenWeatherMap API**.

*(Добавь сюда GIF или скриншот приложения в действии — как выглядит окно, анимации и поиск)*

## ✨ Возможности
- Реальное время: температура, ощущается как, влажность, ветер (скорость + направление), давление.
- Полностью кастомный UI: безрамочное окно, закруглённые углы, drag-to-move.
- Тёмная минималистичная тема.
- Плавные анимации: fade-in при загрузке данных, «shake» при ошибке.
- Иконки погоды загружаются напрямую с CDN OpenWeatherMap.
- Минимальные зависимости — ручной парсинг JSON и отрисовка через `Graphics2D`.

## 🚀 Как запустить

### Требования
- **JDK 17** или новее
- **Maven 3.6+**
- Бесплатный API-ключ от [OpenWeatherMap](https://openweathermap.org/api)

### Установка и запуск

1. **Склонируй репозиторий:**
   ```bash
   git clone https://github.com/kyousukr/WeatherApp.git
   cd WeatherApp
2.Настрой API-ключ:
Открой файл src/main/java/com/yourname/weather/WeatherApp.java
Найди строку с apiKey (или YOUR_API_KEY_HERE) и вставь свой ключ.Важно: Никогда не заливай реальный ключ в GitHub! Позже мы вынесем его в config.properties.
Собери и запусти:Bashmvn clean compile
mvn exec:java -Dexec.mainClass="com.yourname.weather.WeatherApp"Или просто открой проект в IntelliJ IDEA и запусти главный класс WeatherApp.java.

📂 Структура проекта
textWeatherApp/
├── pom.xml
├── README.md
├── .gitignore
└── src/
└── main/
├── java/com/yourname/weather/
│   └── WeatherApp.java          # Главный класс + весь UI
└── resources/
└── icons/
└── search.png            # Иконка поиска (если используется)

🛠️ Известные ограничения и планы

Сейчас используется простой ручной парсинг JSON (через indexOf). Работает, но в будущем добавим Gson.
API-ключ пока зашит в код → скоро вынесем в properties-файл.
Только текущая погода (позже добавим прогноз на 5 дней).


Сделано с ❤️ первым проектом на GitHub
kyousukr

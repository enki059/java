Weather Backend Service (только на Java)

Учебное backend-приложение на Java демонстрирующее работу с
- публичными HTTP API,
- потоками ввода-вывода,
- объектно-ориентированным проектированием,
- классической многопоточностью,
- собственным HTTP-сервером (без фреймворков)

Проект реализован только на стандартной библиотеке Java.

---

Используемые технологии
- Java SE (без фреймворков)
- HttpURLConnection
- ServerSocket / Socket
- ExecutorService
- ConcurrentHashMap
- InputStream / OutputStream

---

Общая идея

Приложение представляет собой **простейший backend-сервис погоды**, который:
1. Получает данные о погоде из публичного Weather API
2. Параллельно обновляет погоду для нескольких городов
3. Хранит актуальные данные в памяти
4. Предоставляет HTTP API для получения погоды
Архитектура приближена к реальной backend-разработке, но упрощена для учебных целей.

---

Архитектура

HTTP Client
     ↓
WeatherHttpServer
     ↓
WeatherService (multi-threaded, cache)
     ↓
WeatherClient
     ↓
Public Weather API (Open-Meteo)

---

Основные компоненты

1. WeatherClient
- Отвечает за HTTP-запросы к публичному API (Open-Meteo)
- Использует `HttpURLConnection`
- Работает с `InputStream` / `BufferedReader`
- Выполняет ручной парсинг JSON без сторонних библиотек
- Преобразует ответы API в Java-объекты (`WeatherData`)

2. WeatherService
- Управляет списком городов
- Параллельно обновляет данные о погоде
- Использует `ExecutorService` и пул потоков
- Хранит данные в `ConcurrentHashMap`
- Обеспечивает потокобезопасный доступ к кэшу

3. WeatherHttpServer
- Реализует собственный HTTP-сервер на `ServerSocket`
- Каждый HTTP-запрос обрабатывается в отдельном потоке
- Поддерживает простые REST-эндпоинты
- Формирует HTTP-ответы вручную

---

HTTP API

Получить погоду для всех городов:
GET /weather


Ответ:
{
  "city": "Berlin",
  "temperature": 18.3,
  "windSpeed": 12.4,
  "weatherCode": 3
}
---

Получить погоду для одного города:
GET /weather?city=Berlin

Ответ:
{
  "city": "Berlin",
  "temperature": 18.3,
  "windSpeed": 12.4,
  "weatherCode": 3
}

---

Запуск приложения

1. Скомпилировать проект
2. Запустить Main
HTTP-сервер стартует на порту 8080

Примеры запросов:
http://localhost:8080/weather
http://localhost:8080/weather?city=Berlin

---

Ограничения и упрощения

- Данные хранятся только в памяти (без БД)
- JSON парсится вручную (учебное ограничение)
- Нет аутентификации и HTTPS
- Нет graceful shutdown HTTP-сервера

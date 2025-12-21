import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService {

    private final WeatherClient weatherClient;
    private final List<String> cities;

    // Потокобезопасный кэш
    private final Map<String, WeatherData> cache =
            new ConcurrentHashMap<>();

    // Пул потоков
    private final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    public WeatherService(WeatherClient weatherClient,
                          List<String> cities) {
        this.weatherClient = weatherClient;
        this.cities = cities;
    }

    // Параллельное обновление всех городов
    public void refresh() {
        for (String city : cities) {
            executor.submit(() -> updateCity(city));
        }
    }

    // Обновление одного города в отдельном потоке
    private void updateCity(String city) {
        try {
            System.out.println(
                    Thread.currentThread().getName()
                            + " updating " + city
            );

            WeatherData data = weatherClient.getWeather(city);
            cache.put(city, data);

        } catch (Exception e) {
            System.err.println(
                    "Failed to update " + city + ": " + e.getMessage()
            );
        }
    }

    // Получить погоду для одного города
    public WeatherData getWeather(String city) {
        return cache.get(city);
    }

    // Получить погоду для всех городов
    public Map<String, WeatherData> getAll() {
        return Map.copyOf(cache);
    }

    // Корректное завершение сервиса
    public void shutdown() {
        executor.shutdown();
    }
}

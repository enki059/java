import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        WeatherClient client = new WeatherClient();

        WeatherService weatherService =
                new WeatherService(
                        client,
                        List.of("Berlin", "Paris", "London", "Rome")
                );

        // загрузка данных при старте
        weatherService.refresh();

        WeatherHttpServer server =
                new WeatherHttpServer(weatherService, 8080);

        server.start();
    }
}

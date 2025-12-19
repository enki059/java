import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class WeatherAPI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("API from OpenWeatherMap: ");
        String apiKey = scanner.nextLine().trim();

        if (apiKey.isEmpty()) {
            System.err.println("API is necessary");
            return;
        }

        System.out.print("city: ");
        String input = scanner.nextLine().trim();

        String[] cities = input.isEmpty()
                ? new String[]{"Berlin", "London"}
                : input.split("\\s*,\\s*");

        for (String city : cities) {
            try {

                String weatherJson = getWeather(city, apiKey);

                System.out.println("\n------- погода -------");
                printSimpleInfo(weatherJson);

                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static String getWeather(String city, String apiKey) throws Exception {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=ru",
                city, apiKey
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("error API: " + response.statusCode() +
                    "\n" + response.body());
        }

        return response.body();
    }

    private static String extractValue(String json, String startKey, String endKey) {
        try {
            int startIndex = json.indexOf(startKey);
            if (startIndex == -1) return "N/A";

            startIndex += startKey.length();
            int endIndex = json.indexOf(endKey, startIndex);

            if (endIndex == -1) return "N/A";

            return json.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static void printSimpleInfo(String json) {
        try {
            String temp = extractValue(json, "\"temp\":", ",");
            String windSpeed = extractValue(json, "\"speed\":", ",");
            String humidity = extractValue(json, "\"humidity\":", ",");
            String city = extractValue(json, "\"name\":\"", "\"");

            System.out.println(city.toUpperCase() + "\n");
            System.out.println("температура: " + String.format("%.1f", Double.parseDouble(temp)) + " C");
            System.out.println("скорость ветра: " + String.format("%.1f", Double.parseDouble(windSpeed)) + " м/с");
            System.out.println("влажность: " + humidity + " %\n");

        } catch (Exception e) {
            System.out.println("ошибка обработки данных");
        }
    }
}

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherHttpServer {

    private final WeatherService weatherService;
    private final int port;

    private final ExecutorService executor =
            Executors.newCachedThreadPool();

    public WeatherHttpServer(WeatherService weatherService, int port) {
        this.weatherService = weatherService;
        this.port = port;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket) {
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(),
                                    StandardCharsets.UTF_8
                            )
                    );

            OutputStream outputStream = socket.getOutputStream();

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            // Пример: GET /weather?city=Berlin HTTP/1.1
            String[] parts = requestLine.split(" ");
            String path = parts[1];

            String responseBody;
            int statusCode = 200;

            if (path.startsWith("/weather")) {
                responseBody = handleWeatherRequest(path);
            } else {
                statusCode = 404;
                responseBody = "{\"error\":\"Not found\"}";
            }

            writeResponse(outputStream, statusCode, responseBody);

        } catch (Exception e) {
            System.err.println("Request handling error: " + e.getMessage());
        }
    }

    // ---------- Routing ----------

    private String handleWeatherRequest(String path) {
        if (path.contains("?city=")) {
            String city = path.substring(path.indexOf("?city=") + 6);
            WeatherData data = weatherService.getWeather(city);

            if (data == null) {
                return "{\"error\":\"City not found\"}";
            }

            return weatherToJson(data);
        }

        return allWeatherToJson(weatherService.getAll());
    }

    // ---------- JSON ----------

    private String weatherToJson(WeatherData data) {
        return "{"
                + "\"city\":\"" + data.getCity() + "\","
                + "\"temperature\":" + data.getTemperature() + ","
                + "\"windSpeed\":" + data.getWindSpeed() + ","
                + "\"weatherCode\":" + data.getWeatherCode()
                + "}";
    }

    private String allWeatherToJson(Map<String, WeatherData> all) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (WeatherData data : all.values()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(weatherToJson(data));
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    // ---------- HTTP ----------

    private void writeResponse(OutputStream out,
                               int statusCode,
                               String body) throws Exception {

        String statusText =
                (statusCode == 200) ? "OK" : "ERROR";

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String headers =
                "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                        + "Content-Type: application/json; charset=utf-8\r\n"
                        + "Content-Length: " + bodyBytes.length + "\r\n"
                        + "\r\n";

        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }
}

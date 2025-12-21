import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherClient {

    public WeatherData getWeather(String city) throws Exception {
        Coordinates coordinates = fetchCoordinates(city);
        return fetchWeather(city, coordinates);
    }


    // ---------- Coordinates ----------
    private Coordinates fetchCoordinates(String city) throws Exception {
        String urlString =
                "https://geocoding-api.open-meteo.com/v1/search?name="
                        + city + "&count=1";

        HttpURLConnection connection = createConnection(urlString);
        String response = readResponse(connection);

        // первый объект из массива results
        String resultsBlock = extractBlock(response, "\"results\"");

        double latitude = extractDouble(resultsBlock, "\"latitude\":");
        double longitude = extractDouble(resultsBlock, "\"longitude\":");

        return new Coordinates(latitude, longitude);
    }


    // ---------- Weather ----------
    private String extractBlock(String text, String blockName) {
        int start = text.indexOf(blockName);
        if (start == -1) {
            throw new IllegalArgumentException("Block not found: " + blockName);
        }

        int braceStart = text.indexOf("{", start);
        int braceCount = 0;

        for (int i = braceStart; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            }

            if (braceCount == 0) {
                return text.substring(braceStart, i + 1);
            }
        }

        throw new IllegalStateException("Closing brace not found for block: " + blockName);
    }


    private WeatherData fetchWeather(String city, Coordinates coordinates) throws Exception {
        String urlString =
                "https://api.open-meteo.com/v1/forecast?latitude="
                        + coordinates.latitude
                        + "&longitude="
                        + coordinates.longitude
                        + "&current_weather=true";

        HttpURLConnection connection = createConnection(urlString);
        String response = readResponse(connection);

        String currentWeatherBlock =
                extractBlock(response, "\"current_weather\"");

        double temperature =
                extractDouble(currentWeatherBlock, "\"temperature\":");

        double windSpeed =
                extractDouble(currentWeatherBlock, "\"windspeed\":");

        int weatherCode =
                extractInt(currentWeatherBlock, "\"weathercode\":");



        return new WeatherData(city, temperature, windSpeed, weatherCode);
    }


    // ---------- HTTP ----------
    private HttpURLConnection createConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        int status = connection.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        return StreamUtils.readAll(stream);
    }

    // ---------- primitive JSON parsing ----------
    private double extractDouble(String text, String key) {
        int start = text.indexOf(key);
        if (start == -1) {
            throw new IllegalArgumentException("Key not found: " + key);
        }

        start += key.length();

        int commaIndex = text.indexOf(",", start);
        int braceIndex = text.indexOf("}", start);

        int end;
        if (commaIndex == -1) {
            end = braceIndex;
        } else if (braceIndex == -1) {
            end = commaIndex;
        } else {
            end = Math.min(commaIndex, braceIndex);
        }

        return Double.parseDouble(text.substring(start, end).trim());
    }

    private int extractInt(String text, String key) {
        int start = text.indexOf(key);
        if (start == -1) {
            throw new IllegalArgumentException("Key not found: " + key);
        }

        start += key.length();

        int commaIndex = text.indexOf(",", start);
        int braceIndex = text.indexOf("}", start);

        int end;
        if (commaIndex == -1) {
            end = braceIndex;
        } else if (braceIndex == -1) {
            end = commaIndex;
        } else {
            end = Math.min(commaIndex, braceIndex);
        }

        return Integer.parseInt(text.substring(start, end).trim());
    }


    // ----------  helper ----------
    private static class Coordinates {
        final double latitude;
        final double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}

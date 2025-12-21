public class WeatherData {

    private final String city;
    private final double temperature;
    private final double windSpeed;
    private final int weatherCode;

    public WeatherData(String city, double temperature, double windSpeed, int weatherCode) {
        this.city = city;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "city='" + city + '\'' +
                ", temperature=" + temperature +
                ", windSpeed=" + windSpeed +
                ", weatherCode=" + weatherCode +
                '}';
    }
}

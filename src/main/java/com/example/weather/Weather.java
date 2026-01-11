package com.example.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Weather {
    private static final String API_KEY = "069a0eb757c030eccb5e9fe27fa8b8fd";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) {
        String city = "Bengaluru"; // You can change this to test invalid cities
        getWeatherForCity(city);
    }

    public static void getWeatherForCity(String city) {
        try {
            String url = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Success Case
                ObjectMapper mapper = new ObjectMapper();
                WeatherResponse wr = mapper.readValue(response.body(), WeatherResponse.class);
                System.out.println("City: " + wr.name + " | Temp: " + wr.main.temp + "°C");
            } else if (response.statusCode() == 404) {
                // Error Case
                System.out.println("Error: Invalid City '" + city + "'");
            } else {
                // Other Errors
                System.out.println("Error: API request failed with status code " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // POJO Classes for JSON Mapping
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherResponse {
        public String name;
        public MainData main;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainData {
        public double temp;
    }
}

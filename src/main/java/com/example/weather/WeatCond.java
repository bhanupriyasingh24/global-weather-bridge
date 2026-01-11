package com.example.weather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class WeatCond {

    // POJO Classes for JSON Parsing
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherResponse {
        public String name;
        public Main main;
        public List<Weather> weather;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        public double temp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        public String description;
    }

    public static void main(String[] args) {
        try {
            String apiKey = "069a0eb757c030eccb5e9fe27fa8b8fd";
            String city = "Bengaluru";

            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city + "&appid=" + apiKey + "&units=metric";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if API returned an error (like 404 or 401)
            if (response.statusCode() != 200) {
                System.out.println("API Error: " + response.statusCode());
                System.out.println("Response: " + response.body());
                return;
            }

            // Parsing logic using Jackson
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            WeatherResponse data = mapper.readValue(json, WeatherResponse.class);

            System.out.println(
                    "City: " + data.name +
                            " | Temp: " + data.main.temp + "°C");

            if (data.weather != null && !data.weather.isEmpty()) {
                System.out.println("Description: " + data.weather.get(0).description);
            }

        } catch (Exception e) {
            System.out.println("Invalid City or API Error");
            e.printStackTrace();
        }
    }
}

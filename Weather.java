import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Weather {
    public static void main(String[] args) throws Exception {

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

        System.out.println(response.body());
    }
}

public class WeatherList {
    public String name;
    public double temp;

}

ObjectMapper mapper = new ObjectMapper();
WeatherList wr =
    mapper.readValue(response.body(), WeatherList.class);

System.out.println(
  "City: " + wr.name +
  " | Temp: " + wr.temp + "°C"
);

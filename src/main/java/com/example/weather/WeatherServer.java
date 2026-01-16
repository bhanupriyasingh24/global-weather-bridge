package com.example.weather;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WeatherServer {

    private static final String API_KEY = "069a0eb757c030eccb5e9fe27fa8b8fd";

    public static void main(String[] args) {
        try {
            String portStr = System.getenv("PORT");
            int port = 8080;
            if (portStr != null && !portStr.isEmpty()) {
                port = Integer.parseInt(portStr);
            }

            System.out.println("Attempting to start server on port: " + port);

            // Explicitly bind to 0.0.0.0 to ensure external access
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

            // Context for the HTML Frontend
            server.createContext("/", new HtmlHandler());

            // Context for the JSON API
            server.createContext("/weather", new WeatherHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server successfully started on port " + port);
        } catch (Exception e) {
            System.err.println("Server failed to start!");
            e.printStackTrace();
        }
    }

    // Serves the full HTML page
    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"/".equals(exchange.getRequestURI().getPath())) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            // Embedding HTML here to make the app self-contained for easy cloud deployment
            // Changed fetch URL to relative '/weather?city=' so it works on any domain
            String htmlResponse = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Global Weather Bridge</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: sans-serif; padding: 20px; background-color: #f4f4f9; }\n" +
                    "        .container { max-width: 400px; margin: 50px auto; text-align: center; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }\n"
                    +
                    "        input, select { padding: 12px; width: 70%; margin-bottom: 15px; border: 1px solid #ddd; border-radius: 5px; }\n"
                    +
                    "        button { padding: 12px 25px; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 5px; font-size: 16px; transition: background 0.3s; }\n"
                    +
                    "        button:hover { background: #0056b3; }\n" +
                    "        #result { margin-top: 20px; font-weight: bold; font-size: 1.2em; color: #333; }\n" +
                    "        .error { color: red; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div class=\"container\">\n" +
                    "        <h2>Global Weather Bridge</h2>\n" +
                    "        <input type=\"text\" id=\"cityInput\" placeholder=\"Enter city name (e.g. Bengaluru)\" />\n"
                    +
                    "        <br>\n" +
                    "        <select id=\"citySelect\" onchange=\"handleCitySelect()\">\n" +
                    "            <option value=\"\">or select a popular city...</option>\n" +
                    "            <option value=\"Bengaluru\">Bengaluru</option>\n" +
                    "            <option value=\"London\">London</option>\n" +
                    "            <option value=\"New York\">New York</option>\n" +
                    "            <option value=\"Tokyo\">Tokyo</option>\n" +
                    "            <option value=\"Paris\">Paris</option>\n" +
                    "            <option value=\"Berlin\">Berlin</option>\n" +
                    "            <option value=\"Sydney\">Sydney</option>\n" +
                    "            <option value=\"Mumbai\">Mumbai</option>\n" +
                    "            <option value=\"Dubai\">Dubai</option>\n" +
                    "            <option value=\"Singapore\">Singapore</option>\n" +
                    "            <option value=\"Moscow\">Moscow</option>\n" +
                    "            <option value=\"Los Angeles\">Los Angeles</option>\n" +
                    "            <option value=\"Rio de Janeiro\">Rio de Janeiro</option>\n" +
                    "        </select>\n"
                    +
                    "        <br>\n" +
                    "        <button onclick=\"getWeather()\">Check Weather</button>\n" +
                    "\n" +
                    "        <div id=\"result\"></div>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        function handleCitySelect() {\n" +
                    "            const select = document.getElementById(\"citySelect\");\n" +
                    "            const input = document.getElementById(\"cityInput\");\n" +
                    "            if (select.value) {\n" +
                    "                input.value = select.value;\n" +
                    "                getWeather();\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        function getWeather() {\n" +
                    "            const city = document.getElementById(\"cityInput\").value;\n" +
                    "            const resultElement = document.getElementById(\"result\");\n" +
                    "\n" +
                    "            if (!city) {\n" +
                    "                resultElement.innerHTML = \"<span class='error'>Please enter a city name.</span>\";\n"
                    +
                    "                return;\n" +
                    "            }\n" +
                    "\n" +
                    "            resultElement.innerText = \"Loading...\";\n" +
                    "\n" +
                    "            // Use relative path for cloud compatibility\n" +
                    "            fetch(`/weather?city=${city}`)\n" +
                    "                .then(response => {\n" +
                    "                    if (!response.ok) {\n" +
                    "                        throw new Error(\"Network response was not ok\");\n" +
                    "                    }\n" +
                    "                    return response.json();\n" +
                    "                })\n" +
                    "                .then(data => {\n" +
                    "                    if (data.cod && data.cod != 200) {\n" +
                    "                        resultElement.innerHTML = \"<span class='error'>\" + (data.message || \"City not found\") + \"</span>\";\n"
                    +
                    "                        return;\n" +
                    "                    }\n" +
                    "                    \n" +
                    "                    const temp = data.main.temp;\n" +
                    "                    const desc = data.weather[0].description;\n" +
                    "                    const cityName = data.name;\n" +
                    "                    \n" +
                    "                    resultElement.innerHTML = `\n" +
                    "                        <div style=\"font-size: 2em;\">${temp}°C</div>\n" +
                    "                        <div>${cityName}</div>\n" +
                    "                        <div style=\"text-transform: capitalize; color: #666;\">${desc}</div>\n" +
                    "                    `;\n" +
                    "                })\n" +
                    "                .catch(error => {\n" +
                    "                    console.error(\"Error:\", error);\n" +
                    "                    resultElement.innerHTML = \"<span class='error'>Failed to connect.</span>\";\n"
                    +
                    "                });\n" +
                    "        }\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

            byte[] response = htmlResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            // Add a shorter timeout for options
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class WeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String city = "Bengaluru"; // Default

                if (query != null && query.contains("city=")) {
                    city = query.split("city=")[1].split("&")[0];
                }

                try {
                    String weatherJson = fetchWeather(city);
                    byte[] response = weatherJson.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } catch (Exception e) {
                    String error = "{\"error\": \"Failed to fetch weather\"}";
                    exchange.sendResponseHeaders(500, error.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(error.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    private static String fetchWeather(String city) throws Exception {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + API_KEY + "&units=metric";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}

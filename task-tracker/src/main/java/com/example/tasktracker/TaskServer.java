package com.example.tasktracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskServer {

    // Default to a local postgres URL for testing if not set, but intent is Cloud
    // SQL
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/tasktracker";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // Initialize Database
        try {
            initDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return;
        }

        try {
            String portStr = System.getenv("PORT");
            int port = 8080;
            if (portStr != null && !portStr.isEmpty()) {
                port = Integer.parseInt(portStr);
            }

            System.out.println("Attempting to start server on port: " + port);

            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

            // Context for the HTML Frontend
            server.createContext("/", new HtmlHandler());

            // Context for the JSON API
            server.createContext("/tasks", new ApiHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server successfully started on port " + port);
        } catch (Exception e) {
            System.err.println("Server failed to start!");
            e.printStackTrace();
        }
    }

    private static void initDatabase() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = DEFAULT_DB_URL;
        }
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        int maxRetries = 10;
        int count = 0;

        while (true) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                        "id SERIAL PRIMARY KEY," +
                        "description TEXT NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                stmt.execute(sql);
                System.out.println("Database initialized successfully.");
                break;
            } catch (SQLException e) {
                count++;
                if (count >= maxRetries) {
                    throw e;
                }
                System.out
                        .println("Database not ready yet, retrying in 3 seconds... (" + count + "/" + maxRetries + ")");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for database", ie);
                }
            }
        }
    }

    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"/".equals(exchange.getRequestURI().getPath())) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String htmlResponse = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Task Tracker</title>\n" +
                    "    <link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600&display=swap\" rel=\"stylesheet\">\n"
                    +
                    "    <style>\n" +
                    "        :root {\n" +
                    "            --primary: #6366f1;\n" +
                    "            --primary-hover: #4f46e5;\n" +
                    "            --bg-color: #0f172a;\n" +
                    "            --card-bg: #1e293b;\n" +
                    "            --text: #f8fafc;\n" +
                    "            --text-secondary: #94a3b8;\n" +
                    "            --border: #334155;\n" +
                    "        }\n" +
                    "        body { \n" +
                    "            font-family: 'Outfit', sans-serif; \n" +
                    "            background-color: var(--bg-color); \n" +
                    "            color: var(--text);\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "            min-height: 100vh;\n" +
                    "            margin: 0;\n" +
                    "        }\n" +
                    "        .container { \n" +
                    "            width: 100%;\n" +
                    "            max-width: 500px; \n" +
                    "            background: var(--card-bg); \n" +
                    "            padding: 40px; \n" +
                    "            border-radius: 20px; \n" +
                    "            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5); \n" +
                    "            animation: fadeIn 0.5s ease-out;\n" +
                    "        }\n" +
                    "        @keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }\n"
                    +
                    "        h2 { text-align: center; margin-bottom: 30px; font-weight: 600; color: var(--primary); }\n"
                    +
                    "        .input-group {\n" +
                    "            display: flex;\n" +
                    "            gap: 10px;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        input[type=\"text\"] {\n" +
                    "            flex-grow: 1;\n" +
                    "            padding: 14px;\n" +
                    "            background: #0f172a;\n" +
                    "            border: 1px solid var(--border);\n" +
                    "            border-radius: 10px;\n" +
                    "            color: white;\n" +
                    "            font-family: inherit;\n" +
                    "            font-size: 16px;\n" +
                    "            outline: none;\n" +
                    "            transition: border-color 0.3s;\n" +
                    "        }\n" +
                    "        input[type=\"text\"]:focus {\n" +
                    "            border-color: var(--primary);\n" +
                    "        }\n" +
                    "        button {\n" +
                    "            padding: 14px 28px;\n" +
                    "            background: var(--primary);\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 10px;\n" +
                    "            font-weight: 600;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.2s;\n" +
                    "        }\n" +
                    "        button:hover {\n" +
                    "            background: var(--primary-hover);\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        ul {\n" +
                    "            list-style: none;\n" +
                    "            padding: 0;\n" +
                    "            margin: 0;\n" +
                    "        }\n" +
                    "        li {\n" +
                    "            background: rgba(255, 255, 255, 0.03);\n" +
                    "            padding: 15px;\n" +
                    "            border-radius: 10px;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            border-left: 4px solid var(--primary);\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            animation: slideIn 0.3s ease-out;\n" +
                    "        }\n" +
                    "        @keyframes slideIn { from { opacity: 0; transform: translateX(-10px); } to { opacity: 1; transform: translateX(0); } }\n"
                    +
                    "        .empty-state {\n" +
                    "            text-align: center;\n" +
                    "            color: var(--text-secondary);\n" +
                    "            font-style: italic;\n" +
                    "            margin-top: 20px;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <h2>Task Tracker</h2>\n" +
                    "        <div class=\"input-group\">\n" +
                    "            <input type=\"text\" id=\"taskInput\" placeholder=\"What did you do? (e.g., 'I brushed my teeth')\" />\n"
                    +
                    "            <button onclick=\"addTask()\">Add</button>\n" +
                    "        </div>\n" +
                    "        <ul id=\"taskList\"></ul>\n" +
                    "        <div id=\"emptyState\" class=\"empty-state\" style=\"display:none;\">No tasks yet. Start tracking!</div>\n"
                    +
                    "    </div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        const taskList = document.getElementById('taskList');\n" +
                    "        const emptyState = document.getElementById('emptyState');\n" +
                    "\n" +
                    "        function renderTasks(tasks) {\n" +
                    "            taskList.innerHTML = '';\n" +
                    "            if (tasks.length === 0) {\n" +
                    "                emptyState.style.display = 'block';\n" +
                    "            } else {\n" +
                    "                emptyState.style.display = 'none';\n" +
                    "                tasks.forEach(task => {\n" +
                    "                    const li = document.createElement('li');\n" +
                    "                    li.textContent = task.description;\n" +
                    "                    taskList.appendChild(li);\n" +
                    "                });\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        function fetchTasks() {\n" +
                    "            fetch('/tasks')\n" +
                    "                .then(res => res.json())\n" +
                    "                .then(data => renderTasks(data))\n" +
                    "                .catch(err => console.error('Error fetching tasks:', err));\n" +
                    "        }\n" +
                    "\n" +
                    "        function addTask() {\n" +
                    "            const input = document.getElementById('taskInput');\n" +
                    "            const description = input.value.trim();\n" +
                    "            if (!description) return;\n" +
                    "\n" +
                    "            fetch('/tasks', {\n" +
                    "                method: 'POST',\n" +
                    "                headers: { 'Content-Type': 'text/plain' },\n" +
                    "                body: description\n" +
                    "            })\n" +
                    "            .then(res => {\n" +
                    "                if (res.ok) {\n" +
                    "                    input.value = '';\n" +
                    "                    fetchTasks();\n" +
                    "                } else {\n" +
                    "                    alert('Failed to add task');\n" +
                    "                }\n" +
                    "            });\n" +
                    "        }\n" +
                    "\n" +
                    "        function deleteTask(id) {\n" +
                    "            if (!confirm('Are you sure you want to delete this task?')) return;\n" +
                    "            fetch('/tasks?id=' + id, { method: 'DELETE' })\n" +
                    "                .then(res => {\n" +
                    "                    if (res.ok) {\n" +
                    "                        fetchTasks();\n" +
                    "                    } else {\n" +
                    "                        alert('Failed to delete task');\n" +
                    "                    }\n" +
                    "                });\n" +
                    "        }\n" +
                    "\n" +
                    "        // Initial Load\n" +
                    "        fetchTasks();\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

            byte[] response = htmlResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                try {
                    List<Task> tasks = getAllTasks();
                    String json = objectMapper.writeValueAsString(tasks);
                    byte[] response = json.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database error");
                }
            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String description = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (description == null || description.trim().isEmpty()) {
                    sendError(exchange, 400, "Description cannot be empty");
                    return;
                }
                try {
                    insertTask(description.trim());
                    exchange.sendResponseHeaders(201, -1);
                } catch (SQLException e) {
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database error");
                }
            } else if (exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("id=")) {
                    try {
                        int id = Integer.parseInt(query.split("id=")[1].split("&")[0]);
                        deleteTask(id);
                        exchange.sendResponseHeaders(200, -1);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "Invalid ID format");
                    } catch (SQLException e) {
                        sendError(exchange, 500, "Database error: " + e.getMessage());
                    }
                } else {
                    sendError(exchange, 400, "Missing ID");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] response = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

    }

    // Database Actions
    private static List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = DEFAULT_DB_URL;
        }
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM tasks ORDER BY created_at DESC")) {
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getString("created_at")));
            }
        }
        return tasks;
    }

    private static void insertTask(String description) throws SQLException {
        String sql = "INSERT INTO tasks(description) VALUES(?)";
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = DEFAULT_DB_URL;
        }
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.executeUpdate();
        }
    }

    private static void deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = DEFAULT_DB_URL;
        }
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // POJO
    static class Task {
        public int id;
        public String description;
        public String created_at;

        public Task(int id, String description, String created_at) {
            this.id = id;
            this.description = description;
            this.created_at = created_at;
        }
    }
}

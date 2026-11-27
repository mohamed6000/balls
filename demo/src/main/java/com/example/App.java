package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App 
{
    static Map<String, String> LoadEnv(String filename) throws IOException {
        Map<String, String> env = new HashMap<>();
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            return env;
        }

        List<String> lines = Files.readAllLines(path);
        for (String raw_line : lines) {
            String line = raw_line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int eq = line.indexOf('=');
            if (eq <= 0) continue;

            String key   = line.substring(0, eq).trim();
            String value = line.substring(eq+1).trim();

            if (value.length() >= 2 &&
                    ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }

            env.put(key, value);
        }

        return env;
    }

    public static final class LiveDataRow {
        public final long id;
        public final double temp;
        public final double humidity;

        public LiveDataRow(long id, double temp, double humidity) {
            this.id       = id;
            this.temp     = temp;
            this.humidity = humidity;
        }
    }

    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^{}]*\\}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(-?\\d+)");
    private static final Pattern TEMP_PATTERN = Pattern.compile("\"temp\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern HUMIDITY_PATTERN = Pattern.compile("\"humidity\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private static List<LiveDataRow> ParseLiveDataRows(String json) {
        List<LiveDataRow> rows = new ArrayList<>();
        Matcher object_matcher = OBJECT_PATTERN.matcher(json);

        while (object_matcher.find()) {
            String obj = object_matcher.group();
            Matcher id_matcher = ID_PATTERN.matcher(obj);
            Matcher temp_matcher = TEMP_PATTERN.matcher(obj);
            Matcher humidity_matcher = HUMIDITY_PATTERN.matcher(obj);

            if (id_matcher.find() && temp_matcher.find() && humidity_matcher.find()) {
                long id = Long.parseLong(id_matcher.group(1));
                double temp = Double.parseDouble(temp_matcher.group(1));
                double humidity = Double.parseDouble(humidity_matcher.group(1));
                rows.add(new LiveDataRow(id, temp, humidity));
            }
        }
        return rows;
    }

    public static void main( String[] args )
    {
        try {
            Map<String, String> env = LoadEnv(".env");
            String project_url = env.get("SUPABASE_URL");
            String anon_key = env.get("SUPABASE_ANON_KEY");

            if (project_url == null || anon_key == null) {
                throw new IllegalStateException("Missing SUPABASE_URL or SUPABASE_ANON_KEY in .env or environment.");
            }

            String end_point = project_url + "/rest/v1/live_data?select=id,created_at,temp,humidity&order=id.asc";
            long poll_interval_ms = 2000;

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            HttpRequest request = HttpRequest.newBuilder(URI.create(end_point))
                                    .header("apikey", anon_key)
                                    .header("Authorization", "Bearer " + anon_key)
                                    .header("Accept", "Application/json")
                                    .GET()
                                    .build();

            AtomicReference<List<LiveDataRow>> latest_data = new AtomicReference<>(List.of());

            Thread fetcher_thread = new Thread(() -> {
                while (true) {
                    try {
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            latest_data.set(ParseLiveDataRows(response.body()));
                        } else {
                            System.out.println("HTTP " + response.statusCode() + ": " + response.body());
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to fetch data: " + e.getMessage());
                    }

                    try {
                        Thread.sleep(poll_interval_ms);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            });
            fetcher_thread.setDaemon(true); // Do not prevent the JVM from exiting when main program finishes.
            fetcher_thread.start(); // Start fetching in the background.

            while (true) {
                List<LiveDataRow> current_data = latest_data.get();

                for (LiveDataRow row : current_data) {
                    float x = (float)row.id;
                    float y = (float)row.temp;
                    float z = (float)row.humidity;
                    System.out.printf("[FRAME] id=%d x=%.1f y(temp)=%.1f z(humidity)=%.1f%n", row.id, x, y, z);
                }
                Thread.sleep(500);
            }

            // new Game().Run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

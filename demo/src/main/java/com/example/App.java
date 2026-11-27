package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

            AtomicReference<String> latest_data = new AtomicReference<>("[]");

            Thread fetcher_thread = new Thread(() -> {
                while (true) {
                    try {
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            latest_data.set(response.body());
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
                String current_data = latest_data.get();

                System.out.println("[FRAME DATA] " + current_data);
                Thread.sleep(500);
            }

            // new Game().Run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

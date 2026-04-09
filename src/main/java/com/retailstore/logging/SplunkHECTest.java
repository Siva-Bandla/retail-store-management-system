package com.retailstore.logging;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SplunkHECTest {

    private static final String SPLUNK_TOKEN = "cfc37a96-d5c0-4b43-9629-7957f43a8247";
    private static final String SPLUNK_URL = "http://localhost:8088/services/collector/event";

    public static void main(String[] args) {
        try {
            sendLog("Hello from Java! This goes to retail_store_logs index.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendLog(String message) throws Exception {
        String payload = "{"
                + "\"event\":\"" + message + "\","
                + "\"sourcetype\":\"_json\","
                + "\"index\":\"retail_store_logs\""
                + "}";

        HttpURLConnection con = (HttpURLConnection) new URL(SPLUNK_URL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Splunk " + SPLUNK_TOKEN);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        // Read response
        int responseCode = con.getResponseCode();
        InputStream responseStream = (responseCode >= 200 && responseCode < 300) ?
                con.getInputStream() : con.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (InputStream is = responseStream) {
            int ch;
            while ((ch = is.read()) != -1) {
                response.append((char) ch);
            }
        }

        System.out.println("Splunk Response Code: " + responseCode);
        System.out.println("Splunk Response Body: " + response.toString());
    }
}
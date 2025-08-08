package com.example.utils;

import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class GeminiApiClient {
    private static final String GEMINI_API_URL;

    static {
        Properties props = loadProperties();
        GEMINI_API_URL = props.getProperty("gemini.api.url");
    }

    public static String getResponse(String command, String assistantName) {
        try {
            String prompt = String.format(
                    """
                    You are a smart, voice-enabled virtual assistant named %s, created by "Avinash Soni".
                    You are **not** Google, Siri, or Alexa. Your job is to analyze user commands and return only a clean JSON object with the following structure:
                    {
                      "type": "general | google_search | youtube_search | youtube_play | get_time | get_date | get_day | get_month | calculator_open | instagram_open | facebook_open | weather_show",
                      "userInput": "<original user input, without assistant name if mentioned>",
                      "response": "<short voice-friendly response, e.g. 'Sure, opening now', 'Here’s what I found', 'Today is Sunday'>"
                    }
                    🧠 Rules for interpreting "type":
                    - "general": Use this for factual or conversational questions. If the user asks something that you already know the answer to, classify it as "general" and just give a short answer.
                    - "google_search": if the user asks to search on Google.
                    - "youtube_search": if the user asks to search something on YouTube.
                    - "youtube_play": if the user asks to directly play a video or song.
                    - "calculator_open": if the user wants to use a calculator.
                    - "instagram_open": for opening Instagram.
                    - "facebook_open": for opening Facebook.
                    - "weather_show": for weather-related questions.
                    - "get_time", "get_date", "get_day", "get_month": for time, date, day, and month queries.
                    📌 Additional Instructions:
                    - Only return the JSON object — no extra commentary or formatting.
                    - If the user asks “who made you”, respond with "Avinash Soni" in the response.
                    - Talk in a romantic mode, but do not use words like 'love'.
                    - If the user asks 'who is Avinash Soni', reply with:
                     'He is a student currently pursuing B.Tech with specialization in 
                     Artificial Intelligence and Machine Learning from
                      Shri Shankaracharya Technical Campus, Bhilai.' 
                      Frame this line as you wish.
                    - If the input includes your name (e.g., "Hey %s"), remove it from "userinput".
                    - If it’s a Google or YouTube search/play command, extract only the query text for "userinput".
                    Now analyze and respond to this input: "%s"
                    """.trim(),
                    assistantName, assistantName, command
            );

            JSONObject requestBody = new JSONObject()
                    .put("contents", new JSONObject()
                            .put("parts", new JSONObject().put("text", prompt)));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject()
                    .put("type", "error")
                    .put("userInput", command)
                    .put("response", "Sorry, something went wrong while processing your request.")
                    .toString();
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = GeminiApiClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}
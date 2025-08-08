package com.example.handlers;

import com.example.db.MongoDBConnection;
import com.example.models.User;
import com.example.utils.FileUploadUtils;
import com.example.utils.GeminiApiClient;
import com.example.utils.JsonUtils;
import com.example.utils.CorsUtils;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class AssistantHandler implements HttpHandler {
    private final String action;

    public AssistantHandler(String action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("AssistantHandler: Handling " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

        if (CorsUtils.handlePreflight(exchange)) return;

        MongoDBConnection db = MongoDBConnection.getInstance();

        switch (action) {
            case "update":
                handleUpdate(exchange, db);
                return;
            case "asktoassistant":
                handleAskToAssistant(exchange, db);
                return;
            default:
                String response = new JSONObject().put("message", "Invalid action").toString();
                sendResponse(exchange, 400, response);
                return;
        }
    }

    private void handleUpdate(HttpExchange exchange, MongoDBConnection db) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new JSONObject().put("message", "Method not allowed").toString());
            return;
        }

        String userId = (String) exchange.getAttribute("userId");
        String boundary = FileUploadUtils.extractBoundary(exchange);
        if (boundary == null) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Invalid multipart request").toString());
            return;
        }

        JSONObject body = FileUploadUtils.parseMultipartRequest(exchange, boundary);
        String assistantName = body.optString("assistantName");
        String imageUrl = body.optString("imageUrl");
        String assistantImage = body.optString("filePath", imageUrl);

        if (assistantImage == null || assistantImage.isEmpty()) {
            sendResponse(exchange, 500, new JSONObject().put("message", "Image upload failed").toString());
            return;
        }

        db.getUserCollection().updateOne(
                Filters.eq("_id", new ObjectId(userId)),
                Updates.combine(
                        Updates.set("assistantName", assistantName),
                        Updates.set("assistantImage", assistantImage)
                )
        );

        User user = User.fromDocument(db.getUserCollection().find(Filters.eq("_id", new ObjectId(userId))).first());
        JSONObject userJson = user != null ? user.toJson().put("password", JSONObject.NULL)
                : new JSONObject().put("message", "User not found");
        sendResponse(exchange, 200, userJson.toString());
    }

    private void handleAskToAssistant(HttpExchange exchange, MongoDBConnection db) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new JSONObject().put("message", "Method not allowed").toString());
            return;
        }

        String userId = (String) exchange.getAttribute("userId");
        JSONObject body = JsonUtils.parseRequestBody(exchange);
        String command = body.optString("command");

        if (command.isEmpty()) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Command is required").toString());
            return;
        }

        User user = User.fromDocument(db.getUserCollection().find(Filters.eq("_id", new ObjectId(userId))).first());
        String assistantName = user != null && user.getAssistantName() != null ? user.getAssistantName() : "Assistant";

        db.getUserCollection().updateOne(Filters.eq("_id", new ObjectId(userId)), Updates.push("history", command));

        String geminiResponse = GeminiApiClient.getResponse(command, assistantName);
        JSONObject gemResult;

        try {
            String cleanedResponse = geminiResponse.trim();

            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("\\s*```$", "");
            }

            gemResult = new JSONObject(cleanedResponse);

        } catch (Exception e) {
            JSONObject fallback = new JSONObject()
                    .put("type", "general")
                    .put("userInput", command)
                    .put("response", geminiResponse != null ? geminiResponse : "Sorry, I can't understand");

            sendResponse(exchange, 200, fallback.toString());
            return;
        }

        String type = gemResult.optString("type");
        String userInput = gemResult.optString("userInput");
        String gemResponse = gemResult.optString("response");

        JSONObject responseJson = new JSONObject().put("type", type).put("userInput", userInput);

        ZoneId zoneId = ZoneId.of("Asia/Kolkata"); // Change timezone as needed
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        switch (type) {
            case "get_date":
                responseJson.put("response", "Current date is " + now.toLocalDate());
                break;
            case "get_time":
                String timeFormatted = now.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                responseJson.put("response", "Current time is " + timeFormatted);
                break;
            case "get_day":
                String day = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                responseJson.put("response", "Today is " + day);
                break;
            case "get_month":
                String month = now.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
                responseJson.put("response", "Month is " + month);
                break;
            case "google_search":
            case "youtube_search":
            case "youtube_play":
            case "general":
            case "calculator_open":
            case "instagram_open":
            case "facebook_open":
            case "weather_show":
                responseJson.put("response", gemResponse);
                break;
            default:
                responseJson.put("response", "Sorry, I can't understand.");
                break;
        }

        sendResponse(exchange, 200, responseJson.toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        CorsUtils.addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

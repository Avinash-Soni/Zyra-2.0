package com.example.handlers;

import com.example.db.MongoDBConnection;
import com.example.models.User;
import com.example.utils.CorsUtils;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final String action;

    public UserHandler(String action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("UserHandler: Handling " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

        if (CorsUtils.handlePreflight(exchange)) return;
        CorsUtils.addCorsHeaders(exchange);

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, Map.of("message", "Method not allowed"));
            return;
        }

        if ("current".equals(action)) {
            String userId = (String) exchange.getAttribute("userId");
            if (userId == null) {
                sendJsonResponse(exchange, 401, Map.of("message", "Unauthorized"));
                return;
            }

            MongoDBConnection db = MongoDBConnection.getInstance();
            User user = User.fromDocument(
                    db.getUserCollection().find(Filters.eq("_id", new ObjectId(userId))).first()
            );

            if (user == null) {
                sendJsonResponse(exchange, 404, Map.of("message", "User not found"));
            } else {
                JSONObject userJson = user.toJson().put("password", JSONObject.NULL);
                sendJsonResponse(exchange, 200, userJson.toMap());
            }
        } else {
            sendJsonResponse(exchange, 400, Map.of("message", "Invalid action"));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
        CorsUtils.addCorsHeaders(exchange);
        JSONObject json = new JSONObject(data);
        byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

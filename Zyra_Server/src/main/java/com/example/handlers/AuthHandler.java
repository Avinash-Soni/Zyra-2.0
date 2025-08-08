package com.example.handlers;

import com.example.db.MongoDBConnection;
import com.example.models.User;
import com.example.utils.JsonUtils;
import com.example.utils.JwtUtils;
import com.example.utils.PasswordUtils;
import com.example.utils.CorsUtils;

import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {
    private final String action;

    public AuthHandler(String action) {
        this.action = action;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("AuthHandler: Handling " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

        if (CorsUtils.handlePreflight(exchange)) return;
        CorsUtils.addCorsHeaders(exchange);

        MongoDBConnection db = MongoDBConnection.getInstance();

        switch (action) {
            case "signup":
                handleSignup(exchange, db);
                return;
            case "signin":
                handleSignin(exchange, db);
                return;
            case "logout":
                handleLogout(exchange);
                return;
            default:
                sendResponse(exchange, 400, new JSONObject().put("message", "Invalid action").toString());
        }
    }

    private void handleSignup(HttpExchange exchange, MongoDBConnection db) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new JSONObject().put("message", "Method not allowed").toString());
            return;
        }

        JSONObject body = JsonUtils.parseRequestBody(exchange);
        String name = body.optString("name");
        String email = body.optString("email");
        String password = body.optString("password");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Missing required fields").toString());
            return;
        }

        if (db.getUserCollection().find(Filters.eq("email", email)).first() != null) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Email already exists!").toString());
            return;
        }

        if (password.length() < 6) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Password must be at least 6 characters!").toString());
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User(name, email, hashedPassword, null, null);
        db.getUserCollection().insertOne(user.toDocument());

        String token = JwtUtils.generateToken(user.getId());

        // Include cookie with SameSite=None and Secure for cross-origin
        exchange.getResponseHeaders().add(
                "Set-Cookie", "token=" + token + "; Path=/; HttpOnly; SameSite=None; Secure"
        );

        JSONObject userJson = user.toJson()
                .put("password", JSONObject.NULL)
                .put("token", token); // also return token in JSON

        sendResponse(exchange, 201, userJson.toString());
    }

    private void handleSignin(HttpExchange exchange, MongoDBConnection db) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new JSONObject().put("message", "Method not allowed").toString());
            return;
        }

        JSONObject body = JsonUtils.parseRequestBody(exchange);
        String email = body.optString("email");
        String password = body.optString("password");

        if (email.isEmpty() || password.isEmpty()) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Email and password are required").toString());
            return;
        }

        User user = User.fromDocument(db.getUserCollection().find(Filters.eq("email", email)).first());
        if (user == null) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Email does not exist!").toString());
            return;
        }

        if (!PasswordUtils.checkPassword(password, user.getPassword())) {
            sendResponse(exchange, 400, new JSONObject().put("message", "Incorrect password").toString());
            return;
        }

        String token = JwtUtils.generateToken(user.getId());

        exchange.getResponseHeaders().add(
                "Set-Cookie", "token=" + token + "; Path=/; HttpOnly; SameSite=None; Secure"
        );

        JSONObject userJson = user.toJson()
                .put("password", JSONObject.NULL)
                .put("token", token);

        sendResponse(exchange, 200, userJson.toString());
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, new JSONObject().put("message", "Method not allowed").toString());
            return;
        }

        exchange.getResponseHeaders().add(
                "Set-Cookie", "token=; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=0"
        );

        sendResponse(exchange, 200, new JSONObject().put("message", "Log out successfully").toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        CorsUtils.addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

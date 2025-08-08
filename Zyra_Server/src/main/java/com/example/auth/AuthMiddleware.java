package com.example.auth;

import com.example.utils.CorsUtils;
import com.example.utils.JwtUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthMiddleware implements HttpHandler {
    private final HttpHandler nextHandler;

    public AuthMiddleware(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // ✅ Bypass CORS preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            CorsUtils.handlePreflight(exchange);
            return;
        }

        String token = null;

        // ✅ Try from Cookie
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        System.out.println("Cookie Header: " + cookieHeader);
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                if (cookie.trim().startsWith("token=")) {
                    token = cookie.trim().substring("token=".length());
                    break;
                }
            }
        }

        // ✅ Try from Authorization header if token still not found
        if (token == null || token.isBlank()) {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            System.out.println("Authorization Header: " + authHeader);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring("Bearer ".length());
            }
        }

        System.out.println("Extracted token: " + token);

        if (token == null || token.isBlank()) {
            sendJsonResponse(exchange, 401, "Token not found");
            return;
        }

        String userId = JwtUtils.verifyToken(token);
        System.out.println("Verified userId: " + userId);

        if (userId == null) {
            sendJsonResponse(exchange, 401, "Invalid token");
            return;
        }

        exchange.setAttribute("userId", userId);
        nextHandler.handle(exchange);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject json = new JSONObject().put("message", message);
        byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

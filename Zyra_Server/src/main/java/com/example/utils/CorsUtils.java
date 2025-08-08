package com.example.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.IOException;

public class CorsUtils {
    public static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:5173");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.set("Access-Control-Allow-Credentials", "true");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return true;
        }
        return false;
    }
}

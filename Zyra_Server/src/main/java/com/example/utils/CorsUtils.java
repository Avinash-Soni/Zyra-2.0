package com.example.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.IOException;

public class CorsUtils {

    // Update with your actual Vercel frontend URL
    private static final String LOCAL_ORIGIN = "http://localhost:5173";
    private static final String PROD_ORIGIN = "https://zyra-2-0.vercel.app";

    public static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        // Allow only whitelisted domains
        if (LOCAL_ORIGIN.equals(origin) || PROD_ORIGIN.equals(origin)) {
            headers.set("Access-Control-Allow-Origin", origin);
        }

        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie");
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

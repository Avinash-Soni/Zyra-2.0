package com.example.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.IOException;

public class CorsUtils {
<<<<<<< HEAD
    public static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        // Allow only whitelisted domains
        if ("http://localhost:5173".equals(origin) || "https://your-vercel-app.vercel.app".equals(origin)) {
            headers.set("Access-Control-Allow-Origin", origin);
        }

        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.set("Access-Control-Allow-Credentials", "true");
    }

=======
   public static void addCorsHeaders(HttpExchange exchange) {
    Headers headers = exchange.getResponseHeaders();
    String origin = exchange.getRequestHeaders().getFirst("Origin");

    // Allow only whitelisted domains
    if ("http://localhost:5173".equals(origin) || "https://your-vercel-app.vercel.app".equals(origin)) {
        headers.set("Access-Control-Allow-Origin", origin);
    }

    headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    headers.set("Access-Control-Allow-Credentials", "true");
}

>>>>>>> 801e263814200c1d3d5f327ec0094db9e96d4e3c

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

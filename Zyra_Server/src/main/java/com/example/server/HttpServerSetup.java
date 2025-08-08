package com.example.server;

import com.example.auth.AuthMiddleware;
import com.example.handlers.AssistantHandler;
import com.example.handlers.AuthHandler;
import com.example.handlers.UserHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class HttpServerSetup {
    private HttpServer server;

    // Change this to your actual deployed frontend URL
    private static final String FRONTEND_ORIGIN = "https://zyra-2-0.vercel.app";

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", FRONTEND_ORIGIN);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    public void startServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Global OPTIONS handler for CORS preflight
        server.createContext("/", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });

        // Serve static files from /public
        server.createContext("/public", exchange -> {
            addCorsHeaders(exchange);
            URI uri = exchange.getRequestURI();
            String filePath = uri.getPath().replaceFirst("/public", "public");
            File file = new File(filePath);

            if (file.exists() && !file.isDirectory()) {
                String mime = Files.probeContentType(file.toPath());
                if (mime == null) mime = "application/octet-stream";

                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().add("Content-Type", mime);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
            exchange.close();
        });

        // Auth routes
        server.createContext("/api/auth/signup", new AuthHandler("signup"));
        server.createContext("/api/auth/signin", new AuthHandler("signin"));
        server.createContext("/api/auth/logout", new AuthHandler("logout"));

        // Protected user routes
        server.createContext("/api/user/current", new AuthMiddleware(new UserHandler("current")));
        server.createContext("/api/user/update", new AuthMiddleware(new AssistantHandler("update")));
        server.createContext("/api/user/asktoassistant", new AuthMiddleware(new AssistantHandler("asktoassistant")));

        server.start();
        System.out.println("Server started on port " + port);
    }
}
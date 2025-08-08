package com.example.server;

import com.example.auth.AuthMiddleware;
import com.example.handlers.AssistantHandler;
import com.example.handlers.AuthHandler;
import com.example.handlers.UserHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class HttpServerSetup {
    private HttpServer server;

    public void startServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Serve static files from /public
        server.createContext("/public", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                URI uri = exchange.getRequestURI();
                String filePath = uri.getPath().replaceFirst("/public", "public");
                File file = new File(filePath);

                if (file.exists() && !file.isDirectory()) {
                    String mime = Files.probeContentType(file.toPath());
                    if (mime == null) mime = "application/octet-stream";

                    byte[] bytes = Files.readAllBytes(file.toPath());
                    exchange.getResponseHeaders().add("Content-Type", mime);
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                }
            }
        });

        // OPTIONS fallback for CORS preflight
        server.createContext("/api/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Cookie");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
                    exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
                exchange.close();
            }
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

package com.example;

import com.example.server.HttpServerSetup;

public class Main {
    public static void main(String[] args) {
        try {
            // Read PORT from environment variable, fallback to 8080
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

            HttpServerSetup server = new HttpServerSetup();
            server.startServer(port);
            System.out.println("Server running on port " + port + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

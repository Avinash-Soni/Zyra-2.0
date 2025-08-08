package com.example.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

public class JwtUtils {
    private static final String JWT_SECRET;
    private static final Key SECRET_KEY;

    static {
        Properties props = loadProperties();
        JWT_SECRET = props.getProperty("jwt.secret");

        if (JWT_SECRET == null || JWT_SECRET.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long for HS256.");
        }

        SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000)) // 10 days
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null; // Invalid, expired, or malformed token
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = JwtUtils.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}

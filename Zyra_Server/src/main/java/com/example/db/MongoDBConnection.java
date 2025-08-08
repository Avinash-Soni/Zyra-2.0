package com.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MongoDBConnection {
    private static MongoDBConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;

    private MongoDBConnection() {
        Properties props = loadProperties();
        String mongoUrl = props.getProperty("mongodb.url");
        mongoClient = MongoClients.create(mongoUrl);
        database = mongoClient.getDatabase("Zyra2_0");
        userCollection = database.getCollection("user");
        System.out.println("MongoDB connected");
    }

    public static synchronized MongoDBConnection getInstance() {
        if (instance == null) {
            instance = new MongoDBConnection();
        }
        return instance;
    }

    public MongoCollection<Document> getUserCollection() {
        return userCollection;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
package com.example.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String assistantName;
    private String assistantImage;
    private List<String> history;

    public User(String name, String email, String password, String assistantName, String assistantImage) {
        this.id = null;
        this.name = name;
        this.email = email;
        this.password = password;
        this.assistantName = assistantName;
        this.assistantImage = assistantImage;
        this.history = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAssistantName() {
        return assistantName;
    }

    public String getAssistantImage() {
        return assistantImage;
    }

    public List<String> getHistory() {
        return history;
    }

    public Document toDocument() {
        Document doc = new Document("name", name)
                .append("email", email)
                .append("password", password)
                .append("assistantName", assistantName)
                .append("assistantImage", assistantImage)
                .append("history", history);
        if (id != null) {
            doc.append("_id", new ObjectId(id)); // Store as ObjectId if needed
        }
        return doc;
    }

    public static User fromDocument(Document doc) {
        if (doc == null) return null;

        User user = new User(
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("password"),
                doc.getString("assistantName"),
                doc.getString("assistantImage")
        );

        ObjectId objectId = doc.getObjectId("_id");
        if (objectId != null) {
            user.id = objectId.toHexString(); // ✅ CORRECTLY convert ObjectId to String
        }

        user.history = doc.getList("history", String.class, new ArrayList<>());
        return user;
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("_id", id)
                .put("name", name)
                .put("email", email)
                .put("assistantName", assistantName)
                .put("assistantImage", assistantImage)
                .put("history", history);
    }
}

package com.example.utils;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FileUploadUtils {
    public static String extractBoundary(HttpExchange exchange) {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.contains("multipart/form-data")) {
            return contentType.split("boundary=")[1];
        }
        return null;
    }

    public static JSONObject parseMultipartRequest(HttpExchange exchange, String boundary) throws IOException {
        JSONObject result = new JSONObject();
        String boundaryStr = "--" + boundary;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFile = false;
            String fieldName = null;
            String fileName = null;
            StringBuilder fieldValue = new StringBuilder();

            while ((line = br.readLine()) != null) {
                if (line.equals(boundaryStr) || line.equals(boundaryStr + "--")) {
                    if (isFile && fileName != null) {
                        String path = saveFile(exchange.getRequestBody(), fileName);
                        result.put("filePath", path);
                        isFile = false;
                    } else if (fieldName != null) {
                        result.put(fieldName, fieldValue.toString().trim());
                        fieldName = null;
                        fieldValue.setLength(0);
                    }
                    continue;
                }

                if (line.contains("Content-Disposition")) {
                    if (line.contains("filename")) {
                        isFile = true;
                        fileName = line.split("filename=\"")[1].split("\"")[0];
                    } else {
                        fieldName = line.split("name=\"")[1].split("\"")[0];
                    }
                } else if (!isFile && fieldName != null && !line.isEmpty()) {
                    fieldValue.append(line).append("\n");
                }
            }
        }
        return result;
    }

    private static String saveFile(InputStream inputStream, String fileName) throws IOException {
        File dir = new File("public");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String newFileName = UUID.randomUUID() + "_" + fileName;
        File file = new File(dir, newFileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return "public/" + newFileName;
    }
}
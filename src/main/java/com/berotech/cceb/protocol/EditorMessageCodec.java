package com.berotech.cceb.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class EditorMessageCodec {
    private static final Gson GSON = new Gson();

    private EditorMessageCodec() {}

    public static EditorMessage decode(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        JsonObject object = parseObject(json);

        if (!object.has("type") || !object.get("type").isJsonPrimitive()) {
            throw new IllegalArgumentException("Message must include a string 'type' field");
        }

        MessageType type = MessageType.fromWireName(object.get("type").getAsString());
        String message = readOptionalString(object, "message");
        String token = readOptionalString(object, "token");
        String computerId = readOptionalString(object, "computerId");
        String path = readOptionalString(object, "path");
        String content = readOptionalString(object, "content");
        List<String> files = readOptionalStringList(object, "files");

        return new EditorMessage(type, message, token, computerId, path, content, files);
    }

    public static String encode(EditorMessage message) {
        JsonObject object = new JsonObject();
        object.addProperty("type", message.type().wireName());
        if (message.message() != null) {
            object.addProperty("message", message.message());
        }
        if (message.token() != null) {
            object.addProperty("token", message.token());
        }
        if (message.computerId() != null) {
            object.addProperty("computerId", message.computerId());
        }
        if (message.path() != null) {
            object.addProperty("path", message.path());
        }
        if (message.content() != null) {
            object.addProperty("content", message.content());
        }
        if (message.files() != null) {
            JsonArray array = new JsonArray();
            for (String file : message.files()) {
                array.add(file);
            }
            object.add("files", array);
        }
        return GSON.toJson(object);
    }

    private static JsonObject parseObject(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Message must be a JSON object");
            }
            return element.getAsJsonObject();
        } catch (JsonSyntaxException exception) {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }

    private static String readOptionalString(JsonObject object, String field) {
        if (!object.has(field) || object.get(field).isJsonNull()) {
            return null;
        }

        if (!object.get(field).isJsonPrimitive()) {
            throw new IllegalArgumentException("'" + field + "' must be a string");
        }

        return object.get(field).getAsString();
    }

    private static List<String> readOptionalStringList(JsonObject object, String field) {
        if (!object.has(field) || object.get(field).isJsonNull()) {
            return null;
        }

        if (!object.get(field).isJsonArray()) {
            throw new IllegalArgumentException("'" + field + "' must be an array");
        }

        JsonArray array = object.getAsJsonArray(field);
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
                throw new IllegalArgumentException("'" + field + "' must contain strings");
            }
            values.add(element.getAsString());
        }
        return Collections.unmodifiableList(values);
    }
}

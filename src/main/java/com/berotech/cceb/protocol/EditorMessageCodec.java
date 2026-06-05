package com.berotech.cceb.protocol;

import com.google.gson.Gson;
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

        return new EditorMessage(type, message);
    }

    public static String encode(EditorMessage message) {
        JsonObject object = new JsonObject();
        object.addProperty("type", message.type().wireName());
        if (message.message() != null) {
            object.addProperty("message", message.message());
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
}

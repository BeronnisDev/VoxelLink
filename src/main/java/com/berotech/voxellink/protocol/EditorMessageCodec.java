package com.berotech.voxellink.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.berotech.voxellink.target.TargetSummary;
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
        String targetId = readOptionalString(object, "targetId");
        String path = readOptionalString(object, "path");
        String content = readOptionalString(object, "content");
        List<String> files = readOptionalStringList(object, "files");
        List<TargetSummary> targets = readOptionalTargetList(object, "targets");
        List<String> backends = readOptionalStringList(object, "backends");

        return new EditorMessage(type, message, token, targetId, path, content, files, targets, backends);
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
        if (message.targetId() != null) {
            object.addProperty("targetId", message.targetId());
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
        if (message.targets() != null) {
            JsonArray array = new JsonArray();
            for (TargetSummary target : message.targets()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", target.id());
                if (target.label() != null && !target.label().isBlank()) {
                    entry.addProperty("label", target.label());
                }
                entry.addProperty("backend", target.backend());
                entry.addProperty("kind", target.kind());
                array.add(entry);
            }
            object.add("targets", array);
        }
        if (message.backends() != null) {
            JsonArray array = new JsonArray();
            for (String backend : message.backends()) {
                array.add(backend);
            }
            object.add("backends", array);
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

    private static List<TargetSummary> readOptionalTargetList(JsonObject object, String field) {
        if (!object.has(field) || object.get(field).isJsonNull()) {
            return null;
        }

        if (!object.get(field).isJsonArray()) {
            throw new IllegalArgumentException("'" + field + "' must be an array");
        }

        JsonArray array = object.getAsJsonArray(field);
        List<TargetSummary> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("'" + field + "' must contain objects");
            }
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("id") || !entry.get("id").isJsonPrimitive()) {
                throw new IllegalArgumentException("Target entries must include string 'id'");
            }
            String id = entry.get("id").getAsString();
            String label = readOptionalString(entry, "label");
            String backend = readOptionalString(entry, "backend");
            String kind = readOptionalString(entry, "kind");
            if (backend == null || backend.isBlank()) {
                throw new IllegalArgumentException("Target entries must include string 'backend'");
            }
            if (kind == null || kind.isBlank()) {
                throw new IllegalArgumentException("Target entries must include string 'kind'");
            }
            values.add(TargetSummary.of(id, label, backend, kind));
        }
        return Collections.unmodifiableList(values);
    }
}

package club.mondaylunch.gatos.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DataBoxParser {

    public static DataBox<?> parse(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return parsePrimitiveDatabox(json.getAsJsonPrimitive());
        } else if (json.isJsonObject()) {
            return new DataBox<>(json.getAsJsonObject(), DataType.JSON_OBJECT);
        } else if (json.isJsonArray()) {
            return parseListDatabox(json.getAsJsonArray());
        } else {
            throw new IllegalArgumentException("Invalid JSON:\n" + json);
        }
    }

    private static DataBox<?> parsePrimitiveDatabox(JsonPrimitive json) {
        if (json.isString()) {
            return new DataBox<>(json.getAsString(), DataType.STRING);
        } else if (json.isNumber()) {
            return new DataBox<>(json.getAsDouble(), DataType.NUMBER);
        } else if (json.isBoolean()) {
            return new DataBox<>(json.getAsBoolean(), DataType.BOOLEAN);
        } else {
            throw new IllegalArgumentException("Invalid JSON:\n" + json);
        }
    }

    private static DataBox<?> parseListDatabox(JsonArray json) {
        DataType<?> listElementType = DataType.ANY;
        var firstElement = true;
        List<Object> elements = new ArrayList<>();
        for (var element : json) {
            var dataBox = parse(element);
            var type = dataBox.type();
            var value = dataBox.value();
            if (firstElement) {
                listElementType = type;
                firstElement = false;
            } else {
                listElementType = union(listElementType, type);
            }
            elements.add(value);
        }
        @SuppressWarnings("unchecked")
        var listType = (DataType<List<Object>>) (Object) listElementType.listOf();
        return new DataBox<>(elements, listType);
    }

    private static DataType<?> union(DataType<?> type1, DataType<?> type2) {
        if (type1.equals(type2)) {
            return type1;
        } else if (type1.clazz() == List.class && type2.clazz() == List.class) {
            return listUnion(type1, type2);
        } else {
            return DataType.ANY;
        }
    }

    private static DataType<?> listUnion(DataType<?> listType1, DataType<?> listType2) {
        var type1Generic = listType1.name().split(Pattern.quote("$"));
        var type2Generic = listType2.name().split(Pattern.quote("$"));
        var genericDepth = Math.min(type1Generic.length, type2Generic.length) - 1;
        if (genericDepth < 1) {
            throw new IllegalArgumentException("Invalid list type: " + listType1 + " or " + listType2);
        }
        DataType<?> unionType = DataType.ANY;
        for (int i = 0; i < genericDepth; i++) {
            unionType = unionType.listOf();
        }
        return unionType;
    }
}

package club.mondaylunch.gatos.core.models;

import java.util.Objects;

import com.google.gson.JsonObject;

public class JsonObjectReference {

    private JsonObject value;

    public JsonObjectReference() {
        this.value = new JsonObject();
    }

    public JsonObjectReference(JsonObject value) {
        this.value = value;
    }

    public JsonObject getValue() {
        return this.value;
    }

    public void setValue(JsonObject value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            JsonObjectReference that = (JsonObjectReference) obj;
            return Objects.equals(value, that.value);
        }
    }

    @Override
    public String toString() {
        return "JsonObjectReference{"
            + "value=" + value
            + '}';
    }
}

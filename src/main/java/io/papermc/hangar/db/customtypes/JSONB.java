package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.lang.reflect.Type;
import java.util.Map;

public class JSONB extends PGobject {

    private static final String TYPE_STRING = "jsonb";

    private transient JsonNode json;
    private transient Map<String, String> map;

    public JSONB(String value) {
        setType(TYPE_STRING);
        this.value = value;
        parseJson();
    }

    public JSONB(Object value) {
        setType(TYPE_STRING);
        try {
            this.value = new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        parseJson();
    }

    @JsonCreator
    public JSONB(JsonNode json) {
        setType(TYPE_STRING);
        this.value = json.toString();
        this.json = json;
    }

    public JSONB() {
        setType(TYPE_STRING);
    }

    @JsonValue
    public JsonNode getJson() {
        return json;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
        parseJson();
    }

    private void parseJson() {
        try {
            this.json = new ObjectMapper().readTree(value);
            this.map = new ObjectMapper().readValue(value, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException | ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return this.json.toPrettyString();
    }
}

package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.postgresql.util.PGobject;

import java.util.Map;

public class JSONB extends PGobject {

    private static final String TYPE_STRING = "jsonb";

    private transient JsonNode json;
    private transient Map<String, String> map;

    public JSONB(final String value) {
        this.setType(TYPE_STRING);
        this.value = value;
        this.parseJson();
    }

    public JSONB(final Object value) {
        this.setType(TYPE_STRING);
        try {
            this.value = new ObjectMapper().writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }
        this.parseJson();
    }

    @JsonCreator
    public JSONB(final JsonNode json) {
        this.setType(TYPE_STRING);
        this.value = json.toString();
        this.json = json;
    }

    public JSONB() {
        this.setType(TYPE_STRING);
    }

    @JsonValue
    public JsonNode getJson() {
        return this.json;
    }

    public Map<String, String> getMap() {
        if (this.map == null) {
            try {
                this.map = new ObjectMapper().readValue(this.value, new TypeReference<>() {
                });
            } catch (final JsonProcessingException | ClassCastException e) {
                e.printStackTrace();
            }
        }
        return this.map;
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
        this.parseJson();
    }

    private void parseJson() {
        try {
            this.json = new ObjectMapper().readTree(this.value);
        } catch (final JsonProcessingException | ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return this.json.toPrettyString();
    }
}

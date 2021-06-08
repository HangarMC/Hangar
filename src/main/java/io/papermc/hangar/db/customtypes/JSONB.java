package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

public class JSONB extends PGobject {

    private static final String TYPE_STRING = "jsonb";

    private transient JsonNode json;

    public JSONB(String value) {
        setType(TYPE_STRING);
        this.value = value;
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

    @Override
    public void setValue(String value) {
        this.value = value;
        parseJson();
    }

    private void parseJson() {
        try {
            this.json = new ObjectMapper().readTree(value);
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

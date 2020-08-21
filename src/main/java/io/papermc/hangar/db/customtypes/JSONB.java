package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.postgresql.util.PGobject;

public class JSONB extends PGobject {

    private transient JsonNode json;

    public JSONB(String value) {
        setType("jsonb");
        this.value = value;
        parseJson();
    }

    @JsonCreator
    public JSONB(JsonNode json) {
        setType("jsonb");
        this.value = json.toString();
        this.json = json;
    }

    public JSONB() {
        setType("jsonb");
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

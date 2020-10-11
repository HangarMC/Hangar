package io.papermc.hangar.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.papermc.hangar.util.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StringSanitizerModule extends SimpleModule {
    public StringSanitizerModule() {
        addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return StringUtils.stringOrNull(p.getValueAsString().trim());
            }
        });
    }
}

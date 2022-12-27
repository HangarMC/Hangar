package io.papermc.hangar.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StringSanitizerModule extends SimpleModule {
    public StringSanitizerModule() {
        this.addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {
            @Override
            public String deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
                return StringUtils.trimToNull(p.getValueAsString());
            }
        });
    }
}

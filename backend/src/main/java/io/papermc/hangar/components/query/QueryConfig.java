package io.papermc.hangar.components.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.sql.SQLException;
import org.postgresql.jdbc.PgArray;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryConfig {

    @Bean
    public SimpleModule queryPostgresSerializer() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(new StdSerializer<>(PgArray.class) {

            @Override
            public void serialize(final PgArray value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
                gen.writeStartArray();
                final Object array;
                try {
                    array = value.getArray();
                } catch (final SQLException e) {
                    throw new RuntimeException(e);
                }
                if (array instanceof final Object[] arr) {
                    for (final Object o : arr) {
                        gen.writeObject(o);
                    }
                } else if (array instanceof final Iterable<?> it) {
                    for (final Object o : it) {
                        gen.writeObject(o);
                    }
                } else {
                    throw new RuntimeException("Unknown array type: " + array.getClass());
                }
                gen.writeEndArray();
            }
        });
        return module;
    }
}

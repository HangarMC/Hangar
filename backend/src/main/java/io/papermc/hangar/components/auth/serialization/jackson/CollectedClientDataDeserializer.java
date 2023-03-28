package io.papermc.hangar.components.auth.serialization.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToCollectedClientDataConverter;
import io.papermc.hangar.components.auth.model.dto.webauthn.CollectedClientDataForm;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class CollectedClientDataDeserializer extends StdDeserializer<CollectedClientDataForm> {

    private final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter;

    public CollectedClientDataDeserializer(final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter) {
        super(CollectedClientDataForm.class);
        this.base64UrlStringToCollectedClientDataConverter = base64UrlStringToCollectedClientDataConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectedClientDataForm deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        final String value = p.getValueAsString();
        return new CollectedClientDataForm(this.base64UrlStringToCollectedClientDataConverter.convert(value), value);
    }
}

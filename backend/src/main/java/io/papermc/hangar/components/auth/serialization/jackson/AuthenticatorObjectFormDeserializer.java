package io.papermc.hangar.components.auth.serialization.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToAttestationObjectConverter;
import io.papermc.hangar.components.auth.model.dto.webauthn.AttestationObjectForm;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class AuthenticatorObjectFormDeserializer extends StdDeserializer<AttestationObjectForm> {

    private final Base64UrlStringToAttestationObjectConverter base64UrlStringToAttestationObjectConverter;

    public AuthenticatorObjectFormDeserializer(final Base64UrlStringToAttestationObjectConverter base64UrlStringToAttestationObjectConverter) {
        super(AttestationObjectForm.class);
        this.base64UrlStringToAttestationObjectConverter = base64UrlStringToAttestationObjectConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttestationObjectForm deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        final String value = p.getValueAsString();
        return new AttestationObjectForm(this.base64UrlStringToAttestationObjectConverter.convert(value), value);
    }
}

package io.papermc.hangar.components.auth.serialization.formatter;

import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToAttestationObjectConverter;
import io.papermc.hangar.components.auth.model.dto.webauthn.AttestationObjectForm;
import java.text.ParseException;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;

public class AttestationObjectFormFormatter implements Formatter<AttestationObjectForm> {

    @Autowired
    private Base64UrlStringToAttestationObjectConverter base64UrlStringToAttestationObjectConverter;

    public AttestationObjectFormFormatter(final Base64UrlStringToAttestationObjectConverter base64UrlStringToAttestationObjectConverter) {
        this.base64UrlStringToAttestationObjectConverter = base64UrlStringToAttestationObjectConverter;
    }

    @Override
    public @NotNull AttestationObjectForm parse(final @NotNull String text, final @NotNull Locale locale) throws ParseException {
        final AttestationObject attestationObject = this.base64UrlStringToAttestationObjectConverter.convert(text);
        return new AttestationObjectForm(attestationObject, text);
    }

    @Override
    public @NotNull String print(final AttestationObjectForm object, final @NotNull Locale locale) {
        return object.attestationObjectBase64();
    }
}

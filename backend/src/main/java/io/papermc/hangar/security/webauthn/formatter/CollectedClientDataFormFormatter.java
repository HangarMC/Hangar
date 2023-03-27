package io.papermc.hangar.security.webauthn.formatter;

import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToCollectedClientDataConverter;
import io.papermc.hangar.security.webauthn.model.CollectedClientDataForm;
import java.text.ParseException;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.Formatter;

public class CollectedClientDataFormFormatter implements Formatter<CollectedClientDataForm> {

    private final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter;

    public CollectedClientDataFormFormatter(final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter) {
        this.base64UrlStringToCollectedClientDataConverter = base64UrlStringToCollectedClientDataConverter;
    }

    @Override
    public @NotNull CollectedClientDataForm parse(final @NotNull String text, final @NotNull Locale locale) throws ParseException {
        final CollectedClientData collectedClientData = this.base64UrlStringToCollectedClientDataConverter.convert(text);
        return new CollectedClientDataForm(collectedClientData, text);
    }

    @Override
    public @NotNull String print(final CollectedClientDataForm object, final @NotNull Locale locale) {
        return object.clientDataBase64();
    }
}

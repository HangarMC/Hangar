package io.papermc.hangar.components.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.metadata.converter.jackson.WebAuthnMetadataJSONModule;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToAttestationObjectConverter;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToCollectedClientDataConverter;
import com.webauthn4j.springframework.security.converter.jackson.WebAuthn4JSpringSecurityJSONModule;
import io.papermc.hangar.components.auth.serialization.formatter.AttestationObjectFormFormatter;
import io.papermc.hangar.components.auth.serialization.formatter.CollectedClientDataFormFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean
    public ObjectConverter objectConverter() {
        final ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new WebAuthnMetadataJSONModule());
        jsonMapper.registerModule(new WebAuthn4JSpringSecurityJSONModule());
        final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
        return new ObjectConverter(jsonMapper, cborMapper);
    }

    @Bean
    public Base64UrlStringToCollectedClientDataConverter base64StringToCollectedClientDataConverter(final ObjectConverter objectConverter) {
        return new Base64UrlStringToCollectedClientDataConverter(objectConverter);
    }

    @Bean
    public Base64UrlStringToAttestationObjectConverter base64StringToWebAuthnAttestationObjectConverter(final ObjectConverter objectConverter) {
        return new Base64UrlStringToAttestationObjectConverter(objectConverter);
    }

    @Bean
    public CollectedClientDataFormFormatter collectedClientDataFromToBase64StringConverter(
        final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter) {
        return new CollectedClientDataFormFormatter(base64UrlStringToCollectedClientDataConverter);
    }

    @Bean
    public AttestationObjectFormFormatter attestationObjectFormFormatter(
        final Base64UrlStringToAttestationObjectConverter base64UrlStringToAttestationObjectConverter) {
        return new AttestationObjectFormFormatter(base64UrlStringToAttestationObjectConverter);
    }
}

package io.papermc.hangar.components.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.util.RequestUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class TurnstileService extends HangarComponent {

    private static final Logger logger = LoggerFactory.getLogger(TurnstileService.class);
    private static final String URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestTemplate restTemplate;

    public TurnstileService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validate(final String token) {
        if (this.config.security().turnstileSecret() == null || this.config.security().turnstileSecret().isBlank()) {
            return;
        }

        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", this.config.security().turnstileSecret());
        formData.add("response", token);
        formData.add("remoteip", RequestUtil.getRemoteAddress(this.request));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Hangar/1.0");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        final TurnstileResponse body;
        try {
            body = this.restTemplate.postForEntity(URL, entity, TurnstileResponse.class).getBody();
        } catch (final RestClientException ex) {
            logger.error("Failed to validate captcha", ex);
            throw new HangarApiException("error.captcha", List.of("internal-error"));
        }

        if (body == null || !body.success()) {
            throw new HangarApiException("error.captcha", body == null ? List.of("internal-error") : body.errorCodes());
        }
    }

    record TurnstileResponse(boolean success, @JsonProperty("error-codes") List<String> errorCodes) {}
}

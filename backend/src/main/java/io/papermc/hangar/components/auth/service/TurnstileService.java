package io.papermc.hangar.components.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.util.RequestUtil;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TurnstileService extends HangarComponent {

    private final RestTemplate restTemplate;

    public TurnstileService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validate(String token) {
        if (this.config.security().turnstileSecret() != null && !this.config.security().turnstileSecret().isBlank()) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("secret", this.config.security().turnstileSecret());
            formData.add("response", token);
            formData.add("remoteip", RequestUtil.getRemoteAddress(this.request));

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.set("User-Agent", "Hangar/1.0");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
            var response = this.restTemplate.postForEntity(url, entity, TurnstileResponse.class);

            if (response.getBody() != null && !response.getBody().success()) {
                throw new HangarApiException("error.captcha", response.getBody().errorCodes());
            }
        }
    }

    record TurnstileResponse(boolean success, @JsonProperty("error-codes") List<String> errorCodes){}
}

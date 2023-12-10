package io.papermc.hangar.components.auth.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthCodeRequest(@JsonProperty("client_id") String clientId, @JsonProperty("client_secret") String clientSecret, String code) {
}

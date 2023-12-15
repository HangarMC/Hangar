package io.papermc.hangar.components.auth.model.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthCodeResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("id_token") String idToken, String scope) {
}

package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.sso")
public record SSOConfig(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("http://localhost:4444") String oauthUrl,
    @DefaultValue("http://localhost:4444") String backendOauthUrl,
    @DefaultValue("/oauth2/auth/") String loginUrl,
    @DefaultValue("/oauth2/token") String tokenUrl,
    @DefaultValue("/oauth2/sessions/logout") String logoutUrl,
    @DefaultValue("my-client") String clientId,
    @DefaultValue("http://localhost:3001") String authUrl,
    @DefaultValue("/account/signup") String signupUrl,
    @DefaultValue("secret") String apiKey,
    @DefaultValue("") String kratosApiKey
) {
}

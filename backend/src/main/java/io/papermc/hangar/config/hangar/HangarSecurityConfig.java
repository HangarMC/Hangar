package io.papermc.hangar.config.hangar;

import io.papermc.hangar.components.auth.model.oauth.OAuthProvider;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.security")
public record HangarSecurityConfig(
    @DefaultValue("true") boolean secure,
    @DefaultValue("600000") long unsafeDownloadMaxAge, // TODO implement or remove
    @DefaultValue List<String> safeDownloadHosts,
    String tokenIssuer,
    String tokenSecret,
    @DurationUnit(ChronoUnit.SECONDS) Duration tokenExpiry,
    @DurationUnit(ChronoUnit.DAYS) Duration refreshTokenExpiry,
    String rpName,
    String rpId,
    List<OAuthProvider> oAuthProviders,
    @DefaultValue("false") boolean oAuthEnabled,
    String turnstileSecret
) {

    public boolean checkSafe(final String url) {
        return this.safeDownloadHosts.contains(URI.create(url).getHost());
    }
}

package io.papermc.hangar.config.hangar;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "hangar.security")
public record HangarSecurityConfig(
    @DefaultValue("true") boolean secure,
    @DefaultValue("600000") long unsafeDownloadMaxAge, // TODO implement or remove
    @DefaultValue List<String> safeDownloadHosts,
    @DefaultValue("http://localhost:3001/image/%s") String imageProxyUrl,
    String tokenIssuer,
    String tokenSecret,
    @DurationUnit(ChronoUnit.SECONDS) Duration tokenExpiry,
    @DurationUnit(ChronoUnit.DAYS) Duration refreshTokenExpiry,
    @NestedConfigurationProperty SecurityApiConfig api
) {
    @ConfigurationProperties(prefix = "hangar.security.api")
    public record SecurityApiConfig(
        @DefaultValue("http://localhost:8081") String url,
        @DefaultValue("http://localhost:8081/avatar/%s") String avatarUrl,
        @DefaultValue("10000") long timeout // TODO implement or remove
    ) {
    }

    public boolean checkSafe(final String url) {
        return this.safeDownloadHosts.contains(URI.create(url).getHost());
    }

    public boolean isSafeHost(final String host) {
        for (final String safeHost : this.safeDownloadHosts) {
            // Make sure it's the full host or a subdomain
            if (host.equals(safeHost) || host.endsWith("." + safeHost)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSafe(final String urlString) {
        try {
            final URI uri = new URI(urlString);
            final String host = uri.getHost();
            if (uri.getScheme() != null && host == null) {
                if (uri.getScheme().equals("mailto")) {
                    return true;
                }
            } else if (host == null || this.isSafeHost(host)) {
                return true;
            }
        } catch (final URISyntaxException ignored) {
        }
        return false;
    }

    public String makeSafe(final String urlString) {
        if (this.isSafe(urlString)) {
            return urlString;
        }
        return "/linkout?remoteUrl=" + urlString;
    }

    public String proxyImage(final String urlString) {
        if (this.isSafe(urlString)) {
            return urlString;
        }
        return String.format(this.imageProxyUrl(), urlString);
    }
}

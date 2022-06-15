package io.papermc.hangar.config.hangar;

import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "hangar.security")
public class HangarSecurityConfig {

    private boolean secure = false;
    private long unsafeDownloadMaxAge = 600000;
    private List<String> safeDownloadHosts = List.of();
    private String tokenIssuer;
    private String tokenSecret;
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration tokenExpiry;
    @DurationUnit(ChronoUnit.DAYS)
    private Duration refreshTokenExpiry;
    @NestedConfigurationProperty
    public SecurityApiConfig api;

    @Autowired
    public HangarSecurityConfig(SecurityApiConfig api) {
        this.api = api;
    }

    @Component
    @ConfigurationProperties(prefix = "hangar.security.api")
    public static class SecurityApiConfig {

        private String url = "http://localhost:8081";
        private String avatarUrl = "http://localhost:8081/avatar/%s";
        private long timeout = 10000;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }

    public Duration getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Duration tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public Duration getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(Duration refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String getTokenIssuer() {
        return tokenIssuer;
    }

    public void setTokenIssuer(String tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public long getUnsafeDownloadMaxAge() {
        return unsafeDownloadMaxAge;
    }

    public void setUnsafeDownloadMaxAge(long unsafeDownloadMaxAge) {
        this.unsafeDownloadMaxAge = unsafeDownloadMaxAge;
    }

    public List<String> getSafeDownloadHosts() {
        return safeDownloadHosts;
    }

    public void setSafeDownloadHosts(List<String> safeDownloadHosts) {
        this.safeDownloadHosts = safeDownloadHosts;
    }

    public SecurityApiConfig getApi() {
        return api;
    }

    public void setApi(SecurityApiConfig api) {
        this.api = api;
    }

    public boolean checkSafe(String url) {
        return safeDownloadHosts.contains(URI.create(url).getHost());
    }

    public String makeSafe(String urlString) {
        try {
            URI uri  = new URI(urlString);
            String host = uri.getHost();
            if (uri.getScheme() != null && host == null) {
                if (uri.getScheme().equals("mailto")) {
                    return urlString;
                } else {
                    return Routes.LINK_OUT.getRouteUrl(urlString);
                }
            } else {
                if (host == null || this.safeDownloadHosts.contains(host) || this.safeDownloadHosts.stream().anyMatch(host::endsWith)) {
                    return urlString;
                } else {
                    return Routes.LINK_OUT.getRouteUrl(urlString);
                }
            }
        } catch (URISyntaxException ex) {
            return Routes.LINK_OUT.getRouteUrl(urlString);
        }
    }
}

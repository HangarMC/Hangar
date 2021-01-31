package io.papermc.hangar.config.hangar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import io.papermc.hangar.util.Routes;

@Component
@ConfigurationProperties(prefix = "hangar.security")
public class HangarSecurityConfig {

    private boolean secure = false;
    private long unsafeDownloadMaxAge = 600000;
    private List<String> safeDownloadHosts = List.of();
    private String tokenIssuer;
    private String tokenSecret;
    private int tokenExpiry;
    @NestedConfigurationProperty
    public SecurityApiConfig api;

    @Autowired
    public HangarSecurityConfig(SecurityApiConfig api) {
        this.api = api;
    }

    @Component
    @ConfigurationProperties(prefix = "hangar.security.api")
    public static class SecurityApiConfig {

        private String url = "http://localhost:8000";
        private String avatarUrl = "http://localhost:8000/avatar/%s?size=120x120";
        private String key = "changeme";
        private long timeout = 10000;
        @NestedConfigurationProperty
        public SecurityApiConfig.ApiBreakerConfig breaker;

        @Autowired
        public SecurityApiConfig(SecurityApiConfig.ApiBreakerConfig breaker) {
            this.breaker = breaker;
        }

        @Component
        @ConfigurationProperties(prefix = "hangar.security.api.breaker")
        public static class ApiBreakerConfig {

            private int maxFailures = 5;
            private String timeout = "10s";
            private String reset = "5m";

            public int getMaxFailures() {
                return maxFailures;
            }

            public void setMaxFailures(int maxFailures) {
                this.maxFailures = maxFailures;
            }

            public String getTimeout() {
                return timeout;
            }

            public void setTimeout(String timeout) {
                this.timeout = timeout;
            }

            public String getReset() {
                return reset;
            }

            public void setReset(String reset) {
                this.reset = reset;
            }
        }

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

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public SecurityApiConfig.ApiBreakerConfig getBreaker() {
            return breaker;
        }

        public void setBreaker(SecurityApiConfig.ApiBreakerConfig breaker) {
            this.breaker = breaker;
        }
    }

    public int getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(int tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
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

package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.sso")
public class SSOConfig {

    private boolean enabled = true;
    private String oauthUrl = "http://localhost:4444";
    private String loginUrl = "/oauth2/auth/";
    private String tokenUrl = "/oauth2/token";
    private String logoutUrl = "/oauth2/sessions/logout";
    private String clientId = "my-client";

    private String authUrl = "http://localhost:3001";
    private String signupUrl = "/account/signup";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getSignupUrl() {
        return signupUrl;
    }

    public void setSignupUrl(String signupUrl) {
        this.signupUrl = signupUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getOauthUrl() {
        return oauthUrl;
    }

    public void setOauthUrl(String oauthUrl) {
        this.oauthUrl = oauthUrl;
    }
}

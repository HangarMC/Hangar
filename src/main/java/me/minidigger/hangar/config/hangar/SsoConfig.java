package me.minidigger.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "hangar.sso")
public class SsoConfig {

    private boolean enabled = true;
    private String loginUrl = "/sso/";
    private String signupUrl = "/sso/signup/";
    private String verifyUrl = "/sso/sudo/";
    private String logoutUrl = "/accounts/logout/";
    private String avatarUrl = "/avatar/%s?size=120x120";
    private String secret = "changeme";
    private String apiKey = "changeme";
    private Duration timeout = Duration.ofSeconds(2);
    private Duration reset = Duration.ofMinutes(10);

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

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getReset() {
        return reset;
    }

    public void setReset(Duration reset) {
        this.reset = reset;
    }
}

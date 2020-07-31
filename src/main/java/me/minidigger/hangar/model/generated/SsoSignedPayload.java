package me.minidigger.hangar.model.generated;

import javax.validation.constraints.NotEmpty;

// TODO: use this (or delete?)
public class SsoSignedPayload {

    @NotEmpty
    private String sso;

    @NotEmpty
    private String sig;

    @NotEmpty
    private String apiKey;

    public String getSso() {
        return sso;
    }

    public void setSso(String sso) {
        this.sso = sso;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}

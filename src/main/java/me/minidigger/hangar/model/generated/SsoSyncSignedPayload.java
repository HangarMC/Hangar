package me.minidigger.hangar.model.generated;

import javax.validation.constraints.NotEmpty;

public class SsoSyncSignedPayload {

    @NotEmpty
    private final String sso;

    @NotEmpty
    private final String sig;

    @NotEmpty
    private final String apiKey;

    public SsoSyncSignedPayload(String sso, String sig, String apiKey) {
        this.sso = sso;
        this.sig = sig;
        this.apiKey = apiKey;
    }

    public String getSso() {
        return sso;
    }

    public String getSig() {
        return sig;
    }

    public String getApiKey() {
        return apiKey;
    }
}

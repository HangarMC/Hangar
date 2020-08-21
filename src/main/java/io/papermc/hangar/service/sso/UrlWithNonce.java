package io.papermc.hangar.service.sso;

public class UrlWithNonce {

    private final String url;
    private final String nonce;

    public UrlWithNonce(String url, String nonce) {
        this.url = url;
        this.nonce = nonce;
    }

    public String getUrl() {
        return url;
    }

    public String getNonce() {
        return nonce;
    }
}

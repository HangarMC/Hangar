package io.papermc.hangar.model.internal.sso;

public class URLWithNonce {

    private final String url;
    private final String nonce;

    public URLWithNonce(String url, String nonce) {
        this.url = url;
        this.nonce = nonce;
    }

    public String getUrl() {
        return url;
    }

    public String getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return "URLWithNonce{" +
                "url='" + url + '\'' +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}

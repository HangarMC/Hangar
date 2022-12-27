package io.papermc.hangar.model.internal.sso;

public class URLWithNonce {

    private final String url;
    private final String nonce;

    public URLWithNonce(final String url, final String nonce) {
        this.url = url;
        this.nonce = nonce;
    }

    public String getUrl() {
        return this.url;
    }

    public String getNonce() {
        return this.nonce;
    }

    @Override
    public String toString() {
        return "URLWithNonce{" +
            "url='" + this.url + '\'' +
            ", nonce='" + this.nonce + '\'' +
            '}';
    }
}

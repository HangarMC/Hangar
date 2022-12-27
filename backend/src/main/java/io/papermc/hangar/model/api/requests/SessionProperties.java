package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SessionProperties {

    private final boolean _fake;
    private final Long expiresIn;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SessionProperties(final boolean _fake, final Long expiresIn) {
        this._fake = _fake;
        this.expiresIn = expiresIn;
    }

    public boolean isFake() {
        return this._fake;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    @Override
    public String toString() {
        return "SessionProperties{" +
            "_fake=" + this._fake +
            ", expiresIn=" + this.expiresIn +
            '}';
    }
}

package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SessionProperties {

    private final boolean _fake;
    private final Long expiresIn;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SessionProperties(boolean _fake, Long expiresIn) {
        this._fake = _fake;
        this.expiresIn = expiresIn;
    }

    public boolean isFake() {
        return _fake;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        return "SessionProperties{" +
                "_fake=" + _fake +
                ", expiresIn=" + expiresIn +
                '}';
    }
}

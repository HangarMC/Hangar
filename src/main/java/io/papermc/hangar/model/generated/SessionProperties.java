package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * SessionProperties
 */
@Validated
public class SessionProperties {
    @JsonProperty("_fake")
    private boolean _fake = false;

    @JsonProperty("expires_in")
    private Long expiresIn = null;

    public SessionProperties _fake(Boolean _fake) {
        this._fake = _fake;
        return this;
    }

    /**
     * Get _fake
     *
     * @return _fake
     **/
    @ApiModelProperty(value = "")

    public boolean isFake() {
        return _fake;
    }

    public void setFake(boolean _fake) {
        this._fake = _fake;
    }

    public SessionProperties expiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    /**
     * Get expiresIn
     *
     * @return expiresIn
     **/
    @ApiModelProperty(value = "")

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionProperties sessionProperties = (SessionProperties) o;
        return Objects.equals(this._fake, sessionProperties._fake) &&
               Objects.equals(this.expiresIn, sessionProperties.expiresIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_fake, expiresIn);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SessionProperties {\n");

        sb.append("    _fake: ").append(toIndentedString(_fake)).append("\n");
        sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

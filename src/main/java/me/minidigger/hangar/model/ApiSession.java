package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;
import org.threeten.bp.OffsetDateTime;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerReturnedApiSession
 */
@Validated
public class ApiSession {
    @JsonProperty("session")
    private String session = null;

    @JsonProperty("expires")
    private OffsetDateTime expires = null;

    @JsonProperty("type")
    private SessionType type = null;

    public ApiSession session(String session) {
        this.session = session;
        return this;
    }

    /**
     * Get session
     *
     * @return session
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public ApiSession expires(OffsetDateTime expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Get expires
     *
     * @return expires
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public OffsetDateTime getExpires() {
        return expires;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

    public ApiSession type(SessionType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiSession apiSession = (ApiSession) o;
        return Objects.equals(this.session, apiSession.session) &&
               Objects.equals(this.expires, apiSession.expires) &&
               Objects.equals(this.type, apiSession.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, expires, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerReturnedApiSession {\n");

        sb.append("    session: ").append(toIndentedString(session)).append("\n");
        sb.append("    expires: ").append(toIndentedString(expires)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

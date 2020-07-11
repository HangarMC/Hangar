package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerReturnedApiSession
 */
@Validated
public class ApiSessionResponse {
    @JsonProperty("session")
    private String session = null;

    @JsonProperty("expires")
    private OffsetDateTime expires = null;

    @JsonProperty("type")
    private SessionType type = null;

    public ApiSessionResponse(String session, OffsetDateTime expires, SessionType type) {
        this.session = session;
        this.expires = expires;
        this.type = type;
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
        ApiSessionResponse apiSessionResponse = (ApiSessionResponse) o;
        return Objects.equals(this.session, apiSessionResponse.session) &&
               Objects.equals(this.expires, apiSessionResponse.expires) &&
               Objects.equals(this.type, apiSessionResponse.type);
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

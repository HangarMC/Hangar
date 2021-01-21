package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Validated
public class Note {
    @JsonProperty("message")
    private String message = null;

    @JsonProperty("user")
    private String user = null;

    @JsonProperty("time")
    private Long time = System.currentTimeMillis();

    /**
     * Get message
     *
     * @return message
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Note message(String message) {
        this.message = message;
        return this;
    }


    /**
     * Get user
     *
     * @return user
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Note user(String user) {
        this.user = user;
        return this;
    }


    /**
     *
     * Get time
     *
     * @return time
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Note slug(Long time) {
        this.time = time;
        return this;
    }

    public OffsetDateTime toDateTime() {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Note note = (Note) o;
        return Objects.equals(this.message, note.message) &&
                Objects.equals(this.user, note.user) &&
                Objects.equals(this.time, note.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, user, time);
    }
}

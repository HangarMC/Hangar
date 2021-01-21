package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2UserActions
 */
@Validated
public class UserActions {
    @JsonProperty("starred")
    private Boolean starred = null;

    @JsonProperty("watching")
    private Boolean watching = null;

    public UserActions starred(Boolean starred) {
        this.starred = starred;
        return this;
    }

    /**
     * Get starred
     *
     * @return starred
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Boolean isStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public UserActions watching(Boolean watching) {
        this.watching = watching;
        return this;
    }

    /**
     * Get watching
     *
     * @return watching
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Boolean isWatching() {
        return watching;
    }

    public void setWatching(Boolean watching) {
        this.watching = watching;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserActions userActions = (UserActions) o;
        return Objects.equals(this.starred, userActions.starred) &&
               Objects.equals(this.watching, userActions.watching);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starred, watching);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2UserActions {\n");

        sb.append("    starred: ").append(toIndentedString(starred)).append("\n");
        sb.append("    watching: ").append(toIndentedString(watching)).append("\n");
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

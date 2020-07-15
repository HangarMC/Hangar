package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import me.minidigger.hangar.model.NamedPermission;

/**
 * ControllersApiv2ApiV2ControllerCreatedApiKey
 */
@Validated
public class ApiKeyResponse {
    @JsonProperty("key")
    private String key = null;

    @JsonProperty("perms")
    @Valid
    private List<NamedPermission> perms = new ArrayList<>();

    public ApiKeyResponse key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get key
     *
     * @return key
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ApiKeyResponse perms(List<NamedPermission> perms) {
        this.perms = perms;
        return this;
    }

    public ApiKeyResponse addPermsItem(NamedPermission permsItem) {
        this.perms.add(permsItem);
        return this;
    }

    /**
     * Get perms
     *
     * @return perms
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public List<NamedPermission> getPerms() {
        return perms;
    }

    public void setPerms(List<NamedPermission> perms) {
        this.perms = perms;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiKeyResponse apiKeyResponse = (ApiKeyResponse) o;
        return Objects.equals(this.key, apiKeyResponse.key) &&
               Objects.equals(this.perms, apiKeyResponse.perms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, perms);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerCreatedApiKey {\n");

        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    perms: ").append(toIndentedString(perms)).append("\n");
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

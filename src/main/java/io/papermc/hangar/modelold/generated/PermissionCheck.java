package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.papermc.hangar.modelold.PermissionType;
import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerPermissionCheck
 */
@Validated
public class PermissionCheck {

    @JsonProperty("type")
    private PermissionType type = null;

    @JsonProperty("result")
    private Boolean result = null;

    public PermissionCheck type(PermissionType type) {
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

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public PermissionCheck result(Boolean result) {
        this.result = result;
        return this;
    }

    /**
     * Get result
     *
     * @return result
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Boolean isResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionCheck controllersApiv2ApiV2ControllerPermissionCheck = (PermissionCheck) o;
        return Objects.equals(this.type, controllersApiv2ApiV2ControllerPermissionCheck.type) &&
               Objects.equals(this.result, controllersApiv2ApiV2ControllerPermissionCheck.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerPermissionCheck {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2FileInfo
 */
@Validated
public class FileInfo {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("size_bytes")
    private Long sizeBytes = null;

    @JsonProperty("md_5_hash")
    private String md5Hash = null;

    public FileInfo name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileInfo sizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
        return this;
    }

    /**
     * Get sizeBytes
     *
     * @return sizeBytes
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public FileInfo md5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
        return this;
    }

    /**
     * Get md5Hash
     *
     * @return md5Hash
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(this.name, fileInfo.name) &&
               Objects.equals(this.sizeBytes, fileInfo.sizeBytes) &&
               Objects.equals(this.md5Hash, fileInfo.md5Hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sizeBytes, md5Hash);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2FileInfo {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    sizeBytes: ").append(toIndentedString(sizeBytes)).append("\n");
        sb.append("    md5Hash: ").append(toIndentedString(md5Hash)).append("\n");
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

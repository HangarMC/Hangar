package io.papermc.hangar.controller.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class RawPage {

    @NotNull
    private final String raw;

    @JsonCreator
    public RawPage(@JsonProperty(value = "raw", required = true) @NotNull String raw) {
        this.raw = raw;
    }

    @NotNull
    public String getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return "PagePreview{" +
                "raw='" + raw + '\'' +
                '}';
    }
}

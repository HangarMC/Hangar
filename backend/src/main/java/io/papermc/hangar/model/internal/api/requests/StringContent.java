package io.papermc.hangar.model.internal.api.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for simple strings
 */
public class StringContent {

    private @NotBlank(message = "general.error.fieldEmpty") @Schema(description = "A non-null, non-empty string") String content;

    public StringContent() {
    }

    public StringContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String contentOrEmpty() {
        return this.content == null ? "" : this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.content;
    }
}

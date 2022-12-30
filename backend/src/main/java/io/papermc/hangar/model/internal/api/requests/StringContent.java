package io.papermc.hangar.model.internal.api.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for simple strings
 */
public class StringContent {

    private @NotBlank(message = "general.error.fieldEmpty") String content;

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.content;
    }
}

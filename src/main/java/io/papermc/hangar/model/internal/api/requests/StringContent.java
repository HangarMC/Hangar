package io.papermc.hangar.model.internal.api.requests;

import javax.validation.constraints.NotBlank;

/**
 * Request body for simple strings
 */
public class StringContent {

    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }
}

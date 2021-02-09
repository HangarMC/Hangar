package io.papermc.hangar.model.internal.api.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request body for simple strings
 */
public class StringContent {

    @NotNull
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
        return "StringContent{" +
                "content='" + content + '\'' +
                '}';
    }
}

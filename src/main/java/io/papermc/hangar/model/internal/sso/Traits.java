package io.papermc.hangar.model.internal.sso;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public record Traits(String email, String github, String discord, String language, String username, String theme) {

    @JsonCreator
    public Traits(@NotNull final String email, final String github, final String discord, final String language, @NotNull final String username, final String theme) {
        this.email = email;
        this.github = StringUtils.trimToNull(github);
        this.discord = StringUtils.trimToNull(discord);
        this.language = StringUtils.trimToNull(language);
        this.username = username;
        this.theme = StringUtils.trimToNull(theme);
    }
}

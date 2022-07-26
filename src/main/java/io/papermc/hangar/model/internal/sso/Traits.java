package io.papermc.hangar.model.internal.sso;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public record Traits(Name name, String email, String github, String discord, String language, String username, String minecraft, String theme) {

    @JsonCreator
    public Traits(final Name name, @NotNull final String email, final String github, final String discord, final String language, @NotNull final String username, final String minecraft, final String theme) {
        this.name = name;
        this.email = email;
        this.github = StringUtils.trimToNull(github);
        this.discord = StringUtils.trimToNull(discord);
        this.language = StringUtils.trimToNull(language);
        this.username = username;
        this.minecraft = StringUtils.trimToNull(minecraft);
        this.theme = StringUtils.trimToNull(theme);
    }

    public record Name(String last, String first) {

        public Name(final String last, final String first) {
            this.last = StringUtils.trimToNull(last);
            this.first = StringUtils.trimToNull(first);
        }
    }
}

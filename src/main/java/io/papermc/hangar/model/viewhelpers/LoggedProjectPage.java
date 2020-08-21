package io.papermc.hangar.model.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedProjectPage {

    private ProjectPage page;
    private String name;
    private String slug;

    public LoggedProjectPage(@Nullable ProjectPage page, @Nullable String name, @Nullable String slug) {
        this.page = page;
        this.name = name;
        this.slug = slug;
    }

    @Nullable
    public ProjectPage getPage() {
        return page;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getSlug() {
        return slug;
    }
}

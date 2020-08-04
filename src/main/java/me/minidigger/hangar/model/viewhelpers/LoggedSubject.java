package me.minidigger.hangar.model.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedSubject {

    private Long id;
    private String username;

    public LoggedSubject(@Nullable Long id, @Nullable String username) {
        this.id = id;
        this.username = username;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public String getUsername() {
        return username;
    }
}



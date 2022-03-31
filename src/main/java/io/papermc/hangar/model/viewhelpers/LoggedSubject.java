package io.papermc.hangar.model.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedSubject {

    private final Long id;
    private final String username;

    public LoggedSubject(@Nullable Long id, @Nullable String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "LoggedSubject{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}



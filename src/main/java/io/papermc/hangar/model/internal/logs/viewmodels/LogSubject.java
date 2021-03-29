package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

/**
 * Users AND Organizations
 */
public class LogSubject {

    private final Long id;
    private final String name;

    public LogSubject(@PropagateNull Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LogSubject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

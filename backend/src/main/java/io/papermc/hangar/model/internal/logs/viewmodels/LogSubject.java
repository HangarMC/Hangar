package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

/**
 * Users AND Organizations
 */
public class LogSubject {

    private final Long id;
    private final String name;

    public LogSubject(@PropagateNull final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "LogSubject{" +
            "id=" + this.id +
            ", name='" + this.name + '\'' +
            '}';
    }
}

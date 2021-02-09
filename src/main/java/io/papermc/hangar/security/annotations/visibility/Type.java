package io.papermc.hangar.security.annotations.visibility;

import java.util.Set;

public enum Type {
    PROJECT(1, 2),
    VERSION(1, 3);

    private final Set<Integer> argCount;

    Type(Integer...argCounts) {
        this.argCount = Set.of(argCounts);
    }

    public Set<Integer> getArgCount() {
        return argCount;
    }
}

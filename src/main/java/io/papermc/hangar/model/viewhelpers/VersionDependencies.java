package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VersionDependencies extends EnumMap<Platform, List<Dependency>> {

    public VersionDependencies(Class<Platform> keyType) {
        super(keyType);
    }

    public VersionDependencies(Map<Platform, ? extends List<Dependency>> m) {
        super(m);
    }
}

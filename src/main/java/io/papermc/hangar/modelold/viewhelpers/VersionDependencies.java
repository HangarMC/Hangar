package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.modelold.generated.Dependency;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VersionDependencies extends EnumMap<Platform, List<Dependency>> {

    public VersionDependencies() {
        super(Platform.class);
    }

    public VersionDependencies(Map<Platform, ? extends List<Dependency>> m) {
        super(m);
    }
}

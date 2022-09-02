package io.papermc.hangar.model;

import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public interface Visible {

    @EnumByOrdinal
    Visibility getVisibility();
}

package io.papermc.hangar.model;

import io.papermc.hangar.model.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public interface Visible {

    @EnumByOrdinal
    Visibility getVisibility();
}

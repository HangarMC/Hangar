package io.papermc.hangar.db.modelold;

import io.papermc.hangar.modelold.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public interface VisibilityModel {

    @EnumByOrdinal
    Visibility getVisibility();

    @EnumByOrdinal
    void setVisibility(Visibility visibility);
}

package io.papermc.hangar.db.model;

import io.papermc.hangar.model.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public interface VisibilityModel {

    @EnumByOrdinal
    Visibility getVisibility();

    @EnumByOrdinal
    void setVisibility(Visibility visibility);
}

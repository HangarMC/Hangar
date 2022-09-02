package io.papermc.hangar.model;

import io.papermc.hangar.model.common.projects.Visibility;

public interface ModelVisible extends Visible {

    void setVisibility(Visibility visibility);
}

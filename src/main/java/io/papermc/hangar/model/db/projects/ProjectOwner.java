package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.identified.UserIdentified;

public interface ProjectOwner extends UserIdentified, Identified, Named {

    default boolean isOrganization() {
        return false;
    }
}

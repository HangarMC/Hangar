package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Named;

public interface ProjectOwner extends Identified, Named {

    long getUserId();
}

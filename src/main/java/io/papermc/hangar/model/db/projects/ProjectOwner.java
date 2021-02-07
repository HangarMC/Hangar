package io.papermc.hangar.model.db.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.Named;

public interface ProjectOwner extends Identified, Named {

    @JsonIgnore
    long getUserId();
}

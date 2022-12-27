package io.papermc.hangar.model.internal.api.requests.admin;

import io.papermc.hangar.model.common.Platform;
import java.util.EnumMap;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChangePlatformVersionsForm extends EnumMap<Platform, @Size(min = 1) List<@NotBlank String>> {

    public ChangePlatformVersionsForm() {
        super(Platform.class);
    }
}

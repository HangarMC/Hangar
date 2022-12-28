package io.papermc.hangar.model.internal.api.requests.admin;

import io.papermc.hangar.model.common.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.EnumMap;
import java.util.List;

public class ChangePlatformVersionsForm extends EnumMap<Platform, @Size(min = 1) List<@NotBlank String>> {

    public ChangePlatformVersionsForm() {
        super(Platform.class);
    }
}
